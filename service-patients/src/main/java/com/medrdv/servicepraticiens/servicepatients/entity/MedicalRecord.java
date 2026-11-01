package com.medrdv.servicepraticiens.servicepatients.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "medical_records",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_medical_record_patient",
                columnNames = "patient_id"
        )
)
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * FK logique vers Patient (pas de relation JPA @OneToOne volontairement).
     * Lien applicatif uniquement : respecte le principe Database per Service.
     */
    @NotNull(message = "L'identifiant du patient est obligatoire")
    @Positive(message = "L'identifiant du patient doit être positif")
    @Column(name = "patient_id", nullable = false, unique = true)
    private Long patientId;

    @Column(length = 1000)
    private String allergies;

    @Column(length = 4000)
    private String antecedents;

    @Column(name = "derniere_consultation")
    private LocalDateTime derniereConsultation;

    @Column(name = "dernier_motif", length = 500)
    private String dernierMotif;

    // ===== Constructeurs =====

    public MedicalRecord() {
    }

    public MedicalRecord(Long patientId, String allergies, String antecedents) {
        this.patientId = patientId;
        this.allergies = allergies;
        this.antecedents = antecedents;
    }

    // ===== Getters / Setters =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getAntecedents() {
        return antecedents;
    }

    public void setAntecedents(String antecedents) {
        this.antecedents = antecedents;
    }

    public LocalDateTime getDerniereConsultation() {
        return derniereConsultation;
    }

    public void setDerniereConsultation(LocalDateTime derniereConsultation) {
        this.derniereConsultation = derniereConsultation;
    }

    public String getDernierMotif() {
        return dernierMotif;
    }

    public void setDernierMotif(String dernierMotif) {
        this.dernierMotif = dernierMotif;
    }

    // ===== equals / hashCode / toString =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicalRecord other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "MedicalRecord{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", allergies='" + allergies + '\'' +
                ", antecedents='" + antecedents + '\'' +
                ", derniereConsultation=" + derniereConsultation +
                ", dernierMotif='" + dernierMotif + '\'' +
                '}';
    }
}