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

    // ===== FALLBACKS CORRIGÉS =====

    public PatientDTO fallbackPatient(Long patientId, Throwable t) {
        FeignException feignException = extraireFeignException(t);
        if (feignException != null && feignException.status() == 404) {
            throw feignException; // Laisse le GlobalExceptionHandler gérer le 404
        }
        throw new ServiceExterneIndisponibleException("Le service des patients est momentanément indisponible. Réessayez plus tard.");
    }

    public DoctorDTO fallbackDoctor(Long doctorId, Throwable t) {
        FeignException feignException = extraireFeignException(t);
        if (feignException != null && feignException.status() == 404) {
            throw feignException;
        }
        throw new ServiceExterneIndisponibleException("Le service des médecins est momentanément indisponible. Réessayez plus tard.");
    }

    public AvailabilityDTO fallbackAvailability(Long doctorId, Long availabilityId, Throwable t) {
        FeignException feignException = extraireFeignException(t);
        if (feignException != null && feignException.status() == 404) {
            throw feignException;
        }
        throw new ServiceExterneIndisponibleException("Le service des créneaux est momentanément indisponible. Réessayez plus tard.");
    }

    // Méthode utilitaire pour chercher l'exception Feign cachée dans les causes
    private FeignException extraireFeignException(Throwable t) {
        if (t instanceof FeignException) {
            return (FeignException) t;
        }
        if (t.getCause() != null) {
            return extraireFeignException(t.getCause());
        }
        return null;
    }
}