package com.medrdv.servicerendezvous.service;

import com.medrdv.servicerendezvous.client.PatientClient;
import com.medrdv.servicerendezvous.dto.AppointmentEnrichedDTO;
import com.medrdv.servicerendezvous.dto.NotificationEmailDto;
import com.medrdv.servicerendezvous.entity.Appointment;
import com.medrdv.servicerendezvous.exception.ServiceExterneIndisponibleException;
import com.medrdv.servicerendezvous.publisher.NotificationPublisher;
import com.medrdv.servicerendezvous.repository.AppointmentRepository;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ResilientClientService resilientClientService;
    private final PatientClient patientClient;
    private final NotificationPublisher notificationPublisher;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              ResilientClientService resilientClientService,
                              PatientClient patientClient,
                              NotificationPublisher notificationPublisher) {
        this.appointmentRepository = appointmentRepository;
        this.resilientClientService = resilientClientService;
        this.patientClient = patientClient;
        this.notificationPublisher = notificationPublisher;
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rendez-vous introuvable avec l'ID " + id));
    }

    public Appointment createAppointment(Appointment appointment, String idempotencyKey) {
        // 1. Vérification de l'idempotence : on LÈVE une exception si la clé existe
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 255) {
            throw new IllegalArgumentException("Clé d'idempotence invalide.");
        }
        if (appointmentRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            throw new IllegalArgumentException("Opération déjà traitée."); // ✅ 400 Bad Request
        }

        // 2. Vérification du patient
        try {
            resilientClientService.getPatientSecurise(appointment.getPatientId());
        } catch (ServiceExterneIndisponibleException e) {
            throw e;
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Patient introuvable avec l'ID " + appointment.getPatientId());
        }

        // 3. Vérification du docteur
        try {
            resilientClientService.getDoctorSecurise(appointment.getDoctorId());
        } catch (ServiceExterneIndisponibleException e) {
            throw e;
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Docteur introuvable avec l'ID " + appointment.getDoctorId());
        }

        // 4. Vérification du créneau
        try {
            resilientClientService.getAvailabilitySecurise(appointment.getDoctorId(), appointment.getAvailabilityId());
        } catch (ServiceExterneIndisponibleException e) {
            throw e;
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Créneau introuvable ou n'appartient pas à ce docteur.");
        }

        // 5. Sauvegarde
        appointment.setStatut("DEMANDE");
        appointment.setIdempotencyKey(idempotencyKey);
        return appointmentRepository.save(appointment);
    }

    // === MÉTHODE MODIFIÉE POUR PUBLIER APRÈS LE COMMIT AVEC DESIGN HTML ===
    public Appointment updateStatut(Long id, String nouveauStatut) {
        Appointment appointment = getAppointmentById(id);
        appointment.setStatut(nouveauStatut);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        if ("CONFIRME".equalsIgnoreCase(nouveauStatut)) {

            // Construction du contenu HTML de l'email
            String htmlContent =
                    "Bonjour,<br><br>" +
                            "Nous avons le plaisir de vous confirmer votre rendez-vous médical.<br><br>" +
                            "<strong>Détails du rendez-vous :</strong><br>" +
                            "<ul>" +
                            "<li><strong>Numéro :</strong> " + savedAppointment.getId() + "</li>" +
                            "<li><strong>Motif :</strong> " + savedAppointment.getMotif() + "</li>" +
                            "</ul>" +
                            "Merci de vous présenter 10 minutes avant l'heure prévue.<br><br>" +
                            "Cordialement,<br>" +
                            "<strong>L'équipe Med-RDV</strong>";

            NotificationEmailDto emailDto = new NotificationEmailDto(
                    "patient@email.com",
                    "Confirmation de votre rendez-vous n°" + savedAppointment.getId(),
                    htmlContent
            );

            // Déclenchement APRÈS le commit
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notificationPublisher.sendNotification(emailDto);
                }
            });
        }

        return savedAppointment;
    }

    public void deleteAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        appointmentRepository.delete(appointment);
    }

    // === MÉTHODE MODIFIÉE POUR UTILISER LES APPELS TOLÉRANTS ===
    @Transactional(readOnly = true)
    public AppointmentEnrichedDTO getEnrichedAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);

        AppointmentEnrichedDTO enriched = new AppointmentEnrichedDTO();
        enriched.setAppointment(appointment);

        // ⚠️ Utilisation des méthodes TOLÉRANTES (cache + mode dégradé)
        enriched.setPatient(resilientClientService.getPatientSecuriseTolerant(appointment.getPatientId()));
        enriched.setDoctor(resilientClientService.getDoctorSecuriseTolerant(appointment.getDoctorId()));

        // Pour le créneau, on garde la méthode stricte (ou on peut la rendre tolérante aussi)
        enriched.setAvailability(resilientClientService.getAvailabilitySecurise(appointment.getDoctorId(), appointment.getAvailabilityId()));

        try {
            enriched.setMedicalRecord(patientClient.getMedicalRecordByPatientId(appointment.getPatientId()));
        } catch (Exception e) {
            enriched.setMedicalRecord(null);
        }

        return enriched;
    }
}