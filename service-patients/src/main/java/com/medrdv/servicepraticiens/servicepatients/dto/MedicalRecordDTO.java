package com.medrdv.servicepatients.dto;

import jakarta.validation.constraints.NotNull;

public class MedicalRecordDTO {
    @NotNull(message = "L'identifiant du patient est obligatoire")
    private Long patientId;

    private String allergies;
    private String antecedents;

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
    public String getAntecedents() { return antecedents; }
    public void setAntecedents(String antecedents) { this.antecedents = antecedents; }
}