package com.medrdv.servicerendezvous.controller;

import com.medrdv.servicerendezvous.dto.AppointmentDTO;
import com.medrdv.servicerendezvous.dto.AppointmentEnrichedDTO;
import com.medrdv.servicerendezvous.entity.Appointment;
import com.medrdv.servicerendezvous.service.AppointmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(
            @Valid @RequestBody AppointmentDTO dto,
            // AJOUT DES VALIDATIONS SUR LE HEADER
            @NotNull(message = "La clé d'idempotence est obligatoire")
            @NotBlank(message = "La clé d'idempotence ne peut pas être vide")
            @Size(max = 255, message = "La clé d'idempotence est trop longue")
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey) {

        // Conversion du DTO vers l'entité Appointment
        Appointment appointment = new Appointment();
        appointment.setPatientId(dto.getPatientId());
        appointment.setDoctorId(dto.getDoctorId());
        appointment.setAvailabilityId(dto.getAvailabilityId());
        appointment.setMotif(dto.getMotif());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createAppointment(appointment, idempotencyKey));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<Appointment> updateStatut(@PathVariable Long id, @RequestParam String statut) {
        return ResponseEntity.ok(appointmentService.updateStatut(id, statut));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    // === ENDPOINT ENRICHI ===
    @GetMapping("/{id}/enriched")
    public ResponseEntity<AppointmentEnrichedDTO> getEnrichedAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getEnrichedAppointment(id));
    }
}