package com.medrdv.servicepraticiens.service;

import com.medrdv.servicepraticiens.entity.Doctor;
import com.medrdv.servicepraticiens.exception.ResourceNotFoundException;
import com.medrdv.servicepraticiens.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Transactional(readOnly = true)
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Docteur introuvable avec l'ID " + id));
    }

    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(Long id, Doctor updatedDoctor) {
        Doctor existingDoctor = getDoctorById(id);
        existingDoctor.setNom(updatedDoctor.getNom());
        existingDoctor.setSpecialite(updatedDoctor.getSpecialite());
        existingDoctor.setTelephone(updatedDoctor.getTelephone());
        return doctorRepository.save(existingDoctor);
    }

    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorById(id);
        doctorRepository.delete(doctor);
    }
}