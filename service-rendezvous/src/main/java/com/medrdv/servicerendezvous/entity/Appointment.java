package com.medrdv.servicerendezvous.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "L'ID du patient est obligatoire")
    @Column(name = "patient_id")
    private Long patientId;

    @NotNull(message = "L'ID du docteur est obligatoire")
    @Column(name = "doctor_id")
    private Long doctorId;

    @NotNull(message = "L'ID du créneau est obligatoire")
    @Column(name = "availability_id")
    private Long availabilityId;

    // MODIFICATION ICI : Ajout de nullable = false et length = 255
    @Column(unique = true, nullable = false, length = 255)
    private String idempotencyKey;

    @NotBlank(message = "Le motif est obligatoire")
    private String motif;

    @Column(nullable = false)
    private String statut; // DEMANDE, CONFIRME, REALISE, ANNULE

    public Appointment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Long getAvailabilityId() { return availabilityId; }
    public void setAvailabilityId(Long availabilityId) { this.availabilityId = availabilityId; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}