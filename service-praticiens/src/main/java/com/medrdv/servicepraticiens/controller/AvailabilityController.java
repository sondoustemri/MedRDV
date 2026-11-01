package com.medrdv.servicepraticiens.controller;

import com.medrdv.servicepraticiens.entity.Availability;
import com.medrdv.servicepraticiens.service.AvailabilityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors/{doctorId}/availabilities")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public ResponseEntity<List<Availability>> getAvailabilities(@PathVariable Long doctorId) {
        return ResponseEntity.ok(availabilityService.getAvailabilitiesByDoctorId(doctorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Availability> getAvailabilityById(@PathVariable Long doctorId, @PathVariable Long id) {
        return ResponseEntity.ok(availabilityService.getAvailabilityById(id));
    }

    @PostMapping
    public ResponseEntity<Availability> createAvailability(@PathVariable Long doctorId, @Valid @RequestBody Availability availability) {
        return ResponseEntity.status(HttpStatus.CREATED).body(availabilityService.createAvailability(doctorId, availability));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Availability> updateAvailability(@PathVariable Long doctorId, @PathVariable Long id, @Valid @RequestBody Availability availability) {
        return ResponseEntity.ok(availabilityService.updateAvailability(id, availability));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable Long doctorId, @PathVariable Long id) {
        availabilityService.deleteAvailability(id);
        return ResponseEntity.noContent().build();
    }
}