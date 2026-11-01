package com.medrdv.servicepraticiens.service;

import com.medrdv.servicepraticiens.entity.Availability;
import com.medrdv.servicepraticiens.exception.ResourceNotFoundException;
import com.medrdv.servicepraticiens.repository.AvailabilityRepository;
import com.medrdv.servicepraticiens.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final DoctorRepository doctorRepository;

    public AvailabilityService(AvailabilityRepository availabilityRepository, DoctorRepository doctorRepository) {
        this.availabilityRepository = availabilityRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional(readOnly = true)
    public List<Availability> getAvailabilitiesByDoctorId(Long doctorId) {
        // On vérifie que le docteur existe (optionnel, mais plus propre)
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Docteur introuvable avec l'ID " + doctorId);
        }
        return availabilityRepository.findByDoctorId(doctorId);
    }

    @Transactional(readOnly = true)
    public Availability getAvailabilityById(Long id) {
        return availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Créneau introuvable avec l'ID " + id));
    }

    public Availability createAvailability(Long doctorId, Availability availability) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Docteur introuvable avec l'ID " + doctorId);
        }
        availability.setDoctorId(doctorId);
        // Par défaut, un nouveau créneau est libre
        if (availability.getStatut() == null) {
            availability.setStatut("LIBRE");
        }
        return availabilityRepository.save(availability);
    }

    public Availability updateAvailability(Long id, Availability updatedAvailability) {
        Availability existing = getAvailabilityById(id);
        existing.setDateHeure(updatedAvailability.getDateHeure());
        existing.setDureeMinutes(updatedAvailability.getDureeMinutes());
        existing.setStatut(updatedAvailability.getStatut());
        return availabilityRepository.save(existing);
    }

    public void deleteAvailability(Long id) {
        Availability availability = getAvailabilityById(id);
        availabilityRepository.delete(availability);
    }
}