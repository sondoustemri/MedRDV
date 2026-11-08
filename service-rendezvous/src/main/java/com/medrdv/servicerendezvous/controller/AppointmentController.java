package com.medrdv.servicerendezvous.controller;

import com.medrdv.servicerendezvous.dto.AppointmentEnrichedDTO;
import com.medrdv.servicerendezvous.entity.Appointment;
import com.medrdv.servicerendezvous.service.AppointmentService;
import jakarta.validation.Valid;
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
            @Valid @RequestBody Appointment appointment,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

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

    // === NOUVEL ENDPOINT ENRICHI ===
    @GetMapping("/{id}/enriched")
    public ResponseEntity<AppointmentEnrichedDTO> getEnrichedAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getEnrichedAppointment(id));
    }
}