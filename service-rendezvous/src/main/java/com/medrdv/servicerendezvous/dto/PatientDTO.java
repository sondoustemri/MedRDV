package com.medrdv.servicerendezvous.dto;

public class PatientDTO {
    private Long id;
    private String nom;
    private String email;

    public PatientDTO() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}