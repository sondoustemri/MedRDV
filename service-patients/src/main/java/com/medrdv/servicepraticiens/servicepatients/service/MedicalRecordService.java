package com.medrdv.servicepraticiens.servicepatients.service;

import com.medrdv.servicepraticiens.servicepatients.entity.MedicalRecord;
import com.medrdv.servicepraticiens.servicepatients.exception.DuplicateResourceException;
import com.medrdv.servicepraticiens.servicepatients.exception.ResourceNotFoundException;
import com.medrdv.servicepraticiens.servicepatients.repository.MedicalRecordRepository;
import com.medrdv.servicepraticiens.servicepatients.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                                PatientRepository patientRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Récupère tous les dossiers médicaux (optionnel, utile pour l'admin).
     */
    @Transactional(readOnly = true)
    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordRepository.findAll();
    }

    /**
     * Récupère le dossier d'un patient.
     */
    @Transactional(readOnly = true)
    public MedicalRecord getMedicalRecordByPatientId(Long patientId) {
        return medicalRecordRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun dossier médical trouvé pour le patient avec l'ID " + patientId));
    }

    /**
     * Crée un dossier médical pour un patient.
     */
    public MedicalRecord createMedicalRecord(Long patientId, MedicalRecord medicalRecord) {
        // 1. Vérifier que le patient existe
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient introuvable avec l'ID " + patientId);
        }

        // 2. Vérifier qu'il n'a pas déjà un dossier
        if (medicalRecordRepository.existsByPatientId(patientId)) {
            throw new DuplicateResourceException(
                    "Un dossier médical existe déjà pour le patient avec l'ID " + patientId);
        }

        // 3. Forcer l'ID du patient sur l'entité (ignorer ce que le client envoie)
        medicalRecord.setId(null); // On s'assure qu'on crée et non qu'on met à jour
        medicalRecord.setPatientId(patientId);

        // 4. Sauvegarder
        return medicalRecordRepository.save(medicalRecord);
    }

    /**
     * Met à jour le dossier médical d'un patient.
     */
    public MedicalRecord updateMedicalRecord(Long patientId, MedicalRecord updatedRecord) {
        // 1. Récupérer le dossier existant (lève une 404 s'il n'existe pas)
        MedicalRecord existingRecord = getMedicalRecordByPatientId(patientId);

        // 2. Mettre à jour les champs modifiables
        existingRecord.setAllergies(updatedRecord.getAllergies());
        existingRecord.setAntecedents(updatedRecord.getAntecedents());
        existingRecord.setDerniereConsultation(updatedRecord.getDerniereConsultation());
        existingRecord.setDernierMotif(updatedRecord.getDernierMotif());

        // 3. Sauvegarder
        return medicalRecordRepository.save(existingRecord);
    }

    /**
     * Supprime le dossier médical d'un patient.
     */
    public void deleteMedicalRecord(Long patientId) {
        MedicalRecord record = getMedicalRecordByPatientId(patientId);
        medicalRecordRepository.delete(record);
    }
}