package com.medrdv.servicerendezvous.dto;

public class MedicalRecordDTO {
    private Long id;
    private Long patientId;
    private String allergies;
    private String antecedents;

    public MedicalRecordDTO() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
    public String getAntecedents() { return antecedents; }
    public void setAntecedents(String antecedents) { this.antecedents = antecedents; }
}