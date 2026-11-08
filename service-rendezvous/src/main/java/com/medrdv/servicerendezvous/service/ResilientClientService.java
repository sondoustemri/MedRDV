package com.medrdv.servicerendezvous.service;

import com.medrdv.servicerendezvous.client.PatientClient;
import com.medrdv.servicerendezvous.client.PraticienClient;
import com.medrdv.servicerendezvous.dto.AvailabilityDTO;
import com.medrdv.servicerendezvous.dto.DoctorDTO;
import com.medrdv.servicerendezvous.dto.PatientDTO;
import com.medrdv.servicerendezvous.exception.ServiceExterneIndisponibleException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

@Service
public class ResilientClientService {

    private final PatientClient patientClient;
    private final PraticienClient praticienClient;

    public ResilientClientService(PatientClient patientClient, PraticienClient praticienClient) {
        this.patientClient = patientClient;
        this.praticienClient = praticienClient;
    }

    @CircuitBreaker(name = "service-patients", fallbackMethod = "fallbackPatient")
    @Retry(name = "service-patients")
    public PatientDTO getPatientSecurise(Long patientId) {
        return patientClient.getPatientById(patientId);
    }

    @CircuitBreaker(name = "service-praticiens", fallbackMethod = "fallbackDoctor")
    @Retry(name = "service-praticiens")
    public DoctorDTO getDoctorSecurise(Long doctorId) {
        return praticienClient.getDoctorById(doctorId);
    }

    @CircuitBreaker(name = "service-praticiens", fallbackMethod = "fallbackAvailability")
    @Retry(name = "service-praticiens")
    public AvailabilityDTO getAvailabilitySecurise(Long doctorId, Long availabilityId) {
        return praticienClient.getAvailabilityById(doctorId, availabilityId);
    }

    // ===== FALLBACKS =====
    public PatientDTO fallbackPatient(Long patientId, Throwable t) {
        if (t instanceof FeignException.NotFound) {
            throw (FeignException.NotFound) t;
        }
        throw new ServiceExterneIndisponibleException("Le service des patients est momentanément indisponible. Réessayez plus tard.");
    }

    public DoctorDTO fallbackDoctor(Long doctorId, Throwable t) {
        if (t instanceof FeignException.NotFound) {
            throw (FeignException.NotFound) t;
        }
        throw new ServiceExterneIndisponibleException("Le service des médecins est momentanément indisponible. Réessayez plus tard.");
    }

    public AvailabilityDTO fallbackAvailability(Long doctorId, Long availabilityId, Throwable t) {
        if (t instanceof FeignException.NotFound) {
            throw (FeignException.NotFound) t;
        }
        throw new ServiceExterneIndisponibleException("Le service des créneaux est momentanément indisponible. Réessayez plus tard.");
    }
}