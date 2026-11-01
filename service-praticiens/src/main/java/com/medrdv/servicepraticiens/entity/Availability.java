package com.medrdv.servicepraticiens.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "availabilities")
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_id")
    private Long doctorId;

    @NotNull(message = "La date et l'heure sont obligatoires")
    @Column(name = "date_heure")
    private LocalDateTime dateHeure;

    @NotNull(message = "La durée est obligatoire")
    @Column(name = "duree_minutes")
    private Integer dureeMinutes;

    @NotBlank(message = "Le statut est obligatoire")
    @Column(length = 50)
    private String statut; // Ex: "LIBRE", "RESERVE", "ANNULE"

    public Availability() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }
    public Integer getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(Integer dureeMinutes) { this.dureeMinutes = dureeMinutes; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}