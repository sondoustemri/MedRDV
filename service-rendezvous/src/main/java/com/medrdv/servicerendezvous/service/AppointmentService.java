package com.medrdv.servicerendezvous.service;

import com.medrdv.servicerendezvous.client.PatientClient;
import com.medrdv.servicerendezvous.client.PraticienClient;
import com.medrdv.servicerendezvous.dto.AppointmentEnrichedDTO;
import com.medrdv.servicerendezvous.entity.Appointment;
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

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientClient patientClient,
                              PraticienClient praticienClient) {
        this.appointmentRepository = appointmentRepository;
        this.patientClient = patientClient;
        this.praticienClient = praticienClient;
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
        // 1. Vérifier le patient
        try {
            patientClient.getPatientById(appointment.getPatientId());
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Patient introuvable avec l'ID " + appointment.getPatientId());
        }

        // 2. Vérifier le docteur
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

    public Appointment updateStatut(Long id, String nouveauStatut) {
        Appointment appointment = getAppointmentById(id);
        appointment.setStatut(nouveauStatut);
        return appointmentRepository.save(appointment);
    }

    public void deleteAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        appointmentRepository.delete(appointment);
    }

    // === NOUVELLE MÉTHODE ENRICHIE ===
    @Transactional(readOnly = true)
    public AppointmentEnrichedDTO getEnrichedAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);

        AppointmentEnrichedDTO enriched = new AppointmentEnrichedDTO();
        enriched.setAppointment(appointment);

        // Appels Feign pour récupérer les infos des autres microservices
        enriched.setPatient(patientClient.getPatientById(appointment.getPatientId()));
        enriched.setDoctor(praticienClient.getDoctorById(appointment.getDoctorId()));
        enriched.setAvailability(praticienClient.getAvailabilityById(appointment.getDoctorId(), appointment.getAvailabilityId()));

        // Le dossier médical est optionnel (le patient peut ne pas en avoir encore)
        try {
            enriched.setMedicalRecord(patientClient.getMedicalRecordByPatientId(appointment.getPatientId()));
        } catch (Exception e) {
            enriched.setMedicalRecord(null);
        }

        return enriched;
    }
}