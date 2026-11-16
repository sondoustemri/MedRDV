package com.medrdv.servicerendezvous.service;

import com.medrdv.servicerendezvous.client.PatientClient;
import com.medrdv.servicerendezvous.client.PraticienClient;
import com.medrdv.servicerendezvous.dto.AvailabilityDTO;
import com.medrdv.servicerendezvous.dto.DoctorDTO;
import com.medrdv.servicerendezvous.dto.PatientDTO;
import com.medrdv.servicerendezvous.exception.ServiceExterneIndisponibleException;
import feign.FeignException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class ResilientClientService {

    private final PatientClient patientClient;
    private final PraticienClient praticienClient;
    private final CacheManager cacheManager;

    public ResilientClientService(PatientClient patientClient, PraticienClient praticienClient, CacheManager cacheManager) {
        this.patientClient = patientClient;
        this.praticienClient = praticienClient;
        this.cacheManager = cacheManager;
    }

    // ==========================================
    // MÉTHODES PROTÉGÉES PAR RESILIENCE4J (Création)
    // ==========================================

    @Bulkhead(name = "service-patients")
    @CircuitBreaker(name = "service-patients", fallbackMethod = "fallbackPatient")
    @Retry(name = "service-patients")
    public PatientDTO getPatientSecurise(Long patientId) {
        return patientClient.getPatientById(patientId);
    }

    @Bulkhead(name = "service-praticiens")
    @CircuitBreaker(name = "service-praticiens", fallbackMethod = "fallbackDoctor")
    @Retry(name = "service-praticiens")
    public DoctorDTO getDoctorSecurise(Long doctorId) {
        return praticienClient.getDoctorById(doctorId);
    }

    @Bulkhead(name = "service-praticiens")
    @CircuitBreaker(name = "service-praticiens", fallbackMethod = "fallbackAvailability")
    @Retry(name = "service-praticiens")
    public AvailabilityDTO getAvailabilitySecurise(Long doctorId, Long availabilityId) {
        return praticienClient.getAvailabilityById(doctorId, availabilityId);
    }

    // ==========================================
    // MÉTHODES PROTÉGÉES POUR LA CONSULTATION (Avec Cache)
    // ==========================================

    @Bulkhead(name = "service-patients")
    @CircuitBreaker(name = "service-patients", fallbackMethod = "fallbackPatientTolerant")
    @Retry(name = "service-patients")
    public PatientDTO getPatientSecuriseTolerant(Long patientId) {
        PatientDTO patient = patientClient.getPatientById(patientId);
        mettreEnCache("patients", patientId, patient);
        return patient;
    }

    @Bulkhead(name = "service-praticiens")
    @CircuitBreaker(name = "service-praticiens", fallbackMethod = "fallbackDoctorTolerant")
    @Retry(name = "service-praticiens")
    public DoctorDTO getDoctorSecuriseTolerant(Long doctorId) {
        DoctorDTO doctor = praticienClient.getDoctorById(doctorId);
        mettreEnCache("doctors", doctorId, doctor);
        return doctor;
    }

    // ==========================================
    // MÉTHODES FALLBACK (Création)
    // ==========================================

    public PatientDTO fallbackPatient(Long patientId, Throwable t) {
        FeignException feignException = extraireFeignException(t);
        if (feignException != null && feignException.status() == 404) {
            throw feignException;
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

    // ==========================================
    // MÉTHODES FALLBACK (Consultation - TOLÉRANTES)
    // ==========================================

    public PatientDTO fallbackPatientTolerant(Long patientId, Throwable t) {
        PatientDTO cached = lireCache("patients", patientId, PatientDTO.class);
        if (cached != null) return cached;

        // Au lieu du constructeur, on utilise le setter
        PatientDTO emptyPatient = new PatientDTO();
        emptyPatient.setId(patientId);
        return emptyPatient;
    }

    public DoctorDTO fallbackDoctorTolerant(Long doctorId, Throwable t) {
        DoctorDTO cached = lireCache("doctors", doctorId, DoctorDTO.class);
        if (cached != null) return cached;

        DoctorDTO emptyDoctor = new DoctorDTO();
        emptyDoctor.setId(doctorId);
        return emptyDoctor;
    }

    // ==========================================
    // MÉTHODES UTILITAIRES
    // ==========================================

    private FeignException extraireFeignException(Throwable t) {
        if (t instanceof FeignException) {
            return (FeignException) t;
        }
        if (t.getCause() != null) {
            return extraireFeignException(t.getCause());
        }
        return null;
    }

    private void mettreEnCache(String nomCache, Object cle, Object valeur) {
        Cache cache = cacheManager.getCache(nomCache);
        if (cache != null) {
            cache.put(cle, valeur);
        }
    }

    private <T> T lireCache(String nomCache, Object cle, Class<T> type) {
        Cache cache = cacheManager.getCache(nomCache);
        return cache != null ? cache.get(cle, type) : null;
    }
}