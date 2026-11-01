package com.medrdv.servicepraticiens.servicepatients.controller;

import com.medrdv.servicepraticiens.servicepatients.entity.MedicalRecord;
import com.medrdv.servicepraticiens.servicepatients.service.MedicalRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/medical-records")
public class MedicalRecordAdminController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordAdminController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    /**
     * Liste tous les dossiers médicaux.
     * GET /medical-records
     */
    @GetMapping
    public ResponseEntity<List<MedicalRecord>> getAllMedicalRecords() {
        return ResponseEntity.ok(medicalRecordService.getAllMedicalRecords());
    }
}