package com.medrdv.servicepraticiens.servicepatients.repository;

import com.medrdv.servicepraticiens.servicepatients.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
}