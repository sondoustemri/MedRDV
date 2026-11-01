package com.medrdv.servicerendezvous.client;

import com.medrdv.servicerendezvous.dto.MedicalRecordDTO;
import com.medrdv.servicerendezvous.dto.PatientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-patients")
public interface PatientClient {
    @GetMapping("/patients/{id}")
    PatientDTO getPatientById(@PathVariable("id") Long id);

    @GetMapping("/patients/{id}/medical-record")
    MedicalRecordDTO getMedicalRecordByPatientId(@PathVariable("id") Long id);
}