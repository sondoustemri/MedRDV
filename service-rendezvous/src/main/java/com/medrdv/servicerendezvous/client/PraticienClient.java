package com.medrdv.servicerendezvous.client;

import com.medrdv.servicerendezvous.dto.AvailabilityDTO;
import com.medrdv.servicerendezvous.dto.DoctorDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-praticiens")
public interface PraticienClient {
    @GetMapping("/doctors/{id}")
    DoctorDTO getDoctorById(@PathVariable("id") Long id);

    @GetMapping("/doctors/{doctorId}/availabilities/{id}")
    AvailabilityDTO getAvailabilityById(@PathVariable("doctorId") Long doctorId, @PathVariable("id") Long id);
}