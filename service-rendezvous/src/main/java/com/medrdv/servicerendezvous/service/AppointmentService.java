package com.medrdv.servicerendezvous.service;

import com.medrdv.servicerendezvous.client.PatientClient;
import com.medrdv.servicerendezvous.client.PraticienClient;
import com.medrdv.servicerendezvous.dto.AppointmentEnrichedDTO;
import com.medrdv.servicerendezvous.entity.Appointment;
import com.medrdv.servicerendezvous.publisher.NotificationPublisher; // <-- NOUVEL IMPORT
import com.medrdv.servicerendezvous.repository.AppointmentRepository;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientClient patientClient;
    private final PraticienClient praticienClient;
    private final NotificationPublisher notificationPublisher; // <-- NOUVEL ATTRIBUT

    // <-- CONSTRUCTEUR MODIFIÉ POUR INJECTER LE PUBLISHER -->
    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientClient patientClient,
                              PraticienClient praticienClient,
                              NotificationPublisher notificationPublisher) {
        this.appointmentRepository = appointmentRepository;
        this.patientClient = patientClient;
        this.praticienClient = praticienClient;
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

    public Appointment createAppointment(Appointment appointment) {
        try {
            patientClient.getPatientById(appointment.getPatientId());
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Patient introuvable avec l'ID " + appointment.getPatientId());
        }

        try {
            praticienClient.getDoctorById(appointment.getDoctorId());
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Docteur introuvable avec l'ID " + appointment.getDoctorId());
        }

        // 3. Vérifier le créneau (et qu'il appartient au docteur)
        try {
            praticienClient.getAvailabilityById(appointment.getDoctorId(), appointment.getAvailabilityId());
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Créneau introuvable ou n'appartient pas à ce docteur.");
        }

        // 4. Si tout est bon, on enregistre
        appointment.setStatut("DEMANDE");
        return appointmentRepository.save(appointment);
    }

    // <-- MÉTHODE MODIFIÉE POUR ENVOYER LE MESSAGE RABBITMQ -->
    public Appointment updateStatut(Long id, String nouveauStatut) {
        Appointment appointment = getAppointmentById(id);
        appointment.setStatut(nouveauStatut);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Si le nouveau statut est "CONFIRME", on envoie la notification !
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

        enriched.setPatient(patientClient.getPatientById(appointment.getPatientId()));
        enriched.setDoctor(praticienClient.getDoctorById(appointment.getDoctorId()));
        enriched.setAvailability(praticienClient.getAvailabilityById(appointment.getDoctorId(), appointment.getAvailabilityId()));

        try {
            enriched.setMedicalRecord(patientClient.getMedicalRecordByPatientId(appointment.getPatientId()));
        } catch (Exception e) {
            enriched.setMedicalRecord(null);
        }

        return enriched;
    }
}