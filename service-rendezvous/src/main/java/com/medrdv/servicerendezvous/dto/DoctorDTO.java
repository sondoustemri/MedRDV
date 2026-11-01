package com.medrdv.servicerendezvous.dto;

public class DoctorDTO {
    private Long id;
    private String nom;
    private String specialite;

    public DoctorDTO() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }
}