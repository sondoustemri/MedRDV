package com.medrdv.servicerendezvous.dto;

import java.time.LocalDateTime;

public class AvailabilityDTO {
    private Long id;
    private Long doctorId;
    private LocalDateTime dateHeure;
    private Integer dureeMinutes;
    private String statut;

    public AvailabilityDTO() {}
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