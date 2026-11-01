package com.medrdv.servicepraticiens.servicepatients.controller;

import com.medrdv.servicepraticiens.servicepatients.entity.MedicalRecord;
import com.medrdv.servicepraticiens.servicepatients.service.MedicalRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patients/{patientId}/medical-record")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    /**
     * Récupère le dossier médical d'un patient.
     * GET /patients/{patientId}/medical-record
     */
    @GetMapping
    public ResponseEntity<MedicalRecord> getMedicalRecord(@PathVariable Long patientId) {
        MedicalRecord record = medicalRecordService.getMedicalRecordByPatientId(patientId);
        return ResponseEntity.ok(record);
    }

    /**
     * Crée le dossier médical d'un patient.
     * POST /patients/{patientId}/medical-record
     */
    @PostMapping
    public ResponseEntity<MedicalRecord> createMedicalRecord(
            @PathVariable Long patientId,
            @Valid @RequestBody MedicalRecord medicalRecord) {

        MedicalRecord createdRecord = medicalRecordService.createMedicalRecord(patientId, medicalRecord);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRecord);
    }

    /**
     * Met à jour le dossier médical d'un patient.
     * PUT /patients/{patientId}/medical-record
     */
    @PutMapping
    public ResponseEntity<MedicalRecord> updateMedicalRecord(
            @PathVariable Long patientId,
            @Valid @RequestBody MedicalRecord medicalRecord) {

        MedicalRecord updatedRecord = medicalRecordService.updateMedicalRecord(patientId, medicalRecord);
        return ResponseEntity.ok(updatedRecord);
    }

    /**
     * Supprime le dossier médical d'un patient.
     * DELETE /patients/{patientId}/medical-record
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteMedicalRecord(@PathVariable Long patientId) {
        medicalRecordService.deleteMedicalRecord(patientId);
        return ResponseEntity.noContent().build();
    }
}