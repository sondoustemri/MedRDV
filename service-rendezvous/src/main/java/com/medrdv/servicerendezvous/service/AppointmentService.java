package com.medrdv.servicerendezvous.service;

import com.medrdv.servicerendezvous.client.PatientClient;
import com.medrdv.servicerendezvous.dto.AppointmentEnrichedDTO;
import com.medrdv.servicerendezvous.dto.AvailabilityDTO;
import com.medrdv.servicerendezvous.entity.Appointment;
import com.medrdv.servicerendezvous.publisher.NotificationPublisher;
import com.medrdv.servicerendezvous.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    // === MÉTHODE CORRIGÉE POUR LE TP V4 ===
    public Appointment createAppointment(Appointment appointment, String idempotencyKey) {

        // 1. Vérification stricte de l'idempotence (selon le TP)
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 255) {
            throw new IllegalArgumentException("Clé d'idempotence invalide.");
        }
        if (appointmentRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            throw new IllegalArgumentException("Opération déjà traitée.");
        }

        // 2. Vérifier le patient (pas de try/catch, le GlobalExceptionHandler gère)
        resilientClientService.getPatientSecurise(appointment.getPatientId());

        // 3. Vérifier le docteur
        resilientClientService.getDoctorSecurise(appointment.getDoctorId());

        // 4. Vérifier le créneau ET la règle métier
        AvailabilityDTO availability = resilientClientService.getAvailabilitySecurise(
                appointment.getDoctorId(),
                appointment.getAvailabilityId()
        );

        // RÈGLE MÉTIER DU TP : Le créneau doit être libre
        if (!"LIBRE".equals(availability.getStatut())) {
            throw new IllegalArgumentException("Refus métier : Ce créneau n'est plus disponible.");
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