package com.medrdv.servicerendezvous.service;

import com.medrdv.servicerendezvous.client.PatientClient;
import com.medrdv.servicerendezvous.dto.AppointmentEnrichedDTO;
import com.medrdv.servicerendezvous.entity.Appointment;
import com.medrdv.servicerendezvous.exception.ServiceExterneIndisponibleException;
import com.medrdv.servicerendezvous.publisher.NotificationPublisher;
import com.medrdv.servicerendezvous.repository.AppointmentRepository;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // === MÉTHODE MODIFIÉE POUR L'IDEMPOTENCE ===
    public Appointment createAppointment(Appointment appointment, String idempotencyKey) {
        // 1. Vérifier si la clé a déjà été utilisée (Idempotence)
        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            Optional<Appointment> existing = appointmentRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return existing.get(); // Renvoie le rendez-vous existant sans en créer un nouveau
            }
        }

        // 2. Vérifier le patient
        try {
            resilientClientService.getPatientSecurise(appointment.getPatientId());
        } catch (ServiceExterneIndisponibleException e) {
            throw e;
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Patient introuvable avec l'ID " + appointment.getPatientId());
        }

        // 3. Vérifier le docteur
        try {
            resilientClientService.getDoctorSecurise(appointment.getDoctorId());
        } catch (ServiceExterneIndisponibleException e) {
            throw e;
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Docteur introuvable avec l'ID " + appointment.getDoctorId());
        }

        // 4. Vérifier le créneau
        try {
            resilientClientService.getAvailabilitySecurise(appointment.getDoctorId(), appointment.getAvailabilityId());
        } catch (ServiceExterneIndisponibleException e) {
            throw e;
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Créneau introuvable ou n'appartient pas à ce docteur.");
        }

        // 5. Si tout est bon, on enregistre avec la clé
        appointment.setStatut("DEMANDE");
        appointment.setIdempotencyKey(idempotencyKey);
        return appointmentRepository.save(appointment);
    }

    public Appointment updateStatut(Long id, String nouveauStatut) {
        Appointment appointment = getAppointmentById(id);
        appointment.setStatut(nouveauStatut);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        if ("CONFIRME".equalsIgnoreCase(nouveauStatut)) {
            String message = "Le rendez-vous n°" + savedAppointment.getId() + " a été CONFIRMÉ avec succès.";
            notificationPublisher.sendNotification(message);
        }

        return savedAppointment;
    }

    public void deleteAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        appointmentRepository.delete(appointment);
    }

    @Transactional(readOnly = true)
    public AppointmentEnrichedDTO getEnrichedAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);

        AppointmentEnrichedDTO enriched = new AppointmentEnrichedDTO();
        enriched.setAppointment(appointment);

        enriched.setPatient(resilientClientService.getPatientSecurise(appointment.getPatientId()));
        enriched.setDoctor(resilientClientService.getDoctorSecurise(appointment.getDoctorId()));
        enriched.setAvailability(resilientClientService.getAvailabilitySecurise(appointment.getDoctorId(), appointment.getAvailabilityId()));

        try {
            enriched.setMedicalRecord(patientClient.getMedicalRecordByPatientId(appointment.getPatientId()));
        } catch (Exception e) {
            enriched.setMedicalRecord(null);
        }

        return enriched;
    }
}