package com.medrdv.servicepraticiens.servicepatients.controller;

import com.medrdv.servicepraticiens.servicepatients.dto.PatientDTO;
import com.medrdv.servicepraticiens.servicepatients.entity.Patient;
import com.medrdv.servicepraticiens.servicepatients.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public List<Patient> getAllPatients() {
        return patientService.getAllPatients();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return patientService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Patient createPatient(@Valid @RequestBody PatientDTO patientDTO) {
        Patient patient = new Patient();
        patient.setNom(patientDTO.getNom());
        patient.setEmail(patientDTO.getEmail());
        patient.setDateNaissance(patientDTO.getDateNaissance());
        patient.setTelephone(patientDTO.getTelephone());
        patient.setNumeroSecu(patientDTO.getNumeroSecu());
        return patientService.savePatient(patient);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientDTO patientDTO) {
        return patientService.getPatientById(id)
                .map(patient -> {
                    patient.setNom(patientDTO.getNom());
                    patient.setEmail(patientDTO.getEmail());
                    patient.setDateNaissance(patientDTO.getDateNaissance());
                    patient.setTelephone(patientDTO.getTelephone());
                    patient.setNumeroSecu(patientDTO.getNumeroSecu());
                    return ResponseEntity.ok(patientService.savePatient(patient));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        if (patientService.getPatientById(id).isPresent()) {
            patientService.deletePatient(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}