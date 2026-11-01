package com.medrdv.servicepraticiens.repository;

import com.medrdv.servicepraticiens.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    // Utile pour lister les créneaux d'un docteur spécifique
    List<Availability> findByDoctorId(Long doctorId);
}