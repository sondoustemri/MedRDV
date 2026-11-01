package com.medrdv.servicepraticiens.servicepatients.repository;

import com.medrdv.servicepraticiens.servicepatients.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    /**
     * Récupère le dossier médical associé à un patient.
     * Comme la contrainte unique sur patient_id garantit qu'il n'y a qu'un dossier par patient,
     * on peut retourner un Optional.
     */
    Optional<MedicalRecord> findByPatientId(Long patientId);

    /**
     * Vérifie si un dossier médical existe déjà pour un patient (utile pour la création).
     */
    boolean existsByPatientId(Long patientId);
}