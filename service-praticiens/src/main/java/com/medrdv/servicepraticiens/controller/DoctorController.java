package com.medrdv.servicepraticiens.controller;

import com.medrdv.servicepraticiens.dto.DoctorDTO;
import com.medrdv.servicepraticiens.entity.Doctor;
import com.medrdv.servicepraticiens.exception.ResourceNotFoundException;
import com.medrdv.servicepraticiens.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(doctorService.getDoctorById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public Doctor createDoctor(@Valid @RequestBody DoctorDTO dto) {
        Doctor doctor = new Doctor();
        doctor.setNom(dto.getNom());
        doctor.setSpecialite(dto.getSpecialite());
        doctor.setTelephone(dto.getTelephone());
        return doctorService.createDoctor(doctor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable Long id, @Valid @RequestBody DoctorDTO dto) {
        try {
            Doctor doctorToUpdate = new Doctor();
            doctorToUpdate.setNom(dto.getNom());
            doctorToUpdate.setSpecialite(dto.getSpecialite());
            doctorToUpdate.setTelephone(dto.getTelephone());

            return ResponseEntity.ok(doctorService.updateDoctor(id, doctorToUpdate));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        try {
            doctorService.deleteDoctor(id);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}