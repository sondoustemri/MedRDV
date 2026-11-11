package com.medrdv.servicerendezvous.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AppointmentDTO {
    @NotNull(message = "L'identifiant du patient est obligatoire")
    private Long patientId;

    @NotNull(message = "L'identifiant du docteur est obligatoire")
    private Long doctorId;

    @NotNull(message = "L'identifiant du créneau est obligatoire")
    private Long availabilityId;

    @NotBlank(message = "Le motif est obligatoire")
    private String motif;

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Long getAvailabilityId() { return availabilityId; }
    public void setAvailabilityId(Long availabilityId) { this.availabilityId = availabilityId; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
}