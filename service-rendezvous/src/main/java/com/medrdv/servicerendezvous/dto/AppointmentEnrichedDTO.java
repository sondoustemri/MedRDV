package com.medrdv.servicerendezvous.dto;

import com.medrdv.servicerendezvous.entity.Appointment;

public class AppointmentEnrichedDTO {
    private Appointment appointment;
    private PatientDTO patient;
    private MedicalRecordDTO medicalRecord;
    private DoctorDTO doctor;
    private AvailabilityDTO availability;

    public AppointmentEnrichedDTO() {}
    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }
    public PatientDTO getPatient() { return patient; }
    public void setPatient(PatientDTO patient) { this.patient = patient; }
    public MedicalRecordDTO getMedicalRecord() { return medicalRecord; }
    public void setMedicalRecord(MedicalRecordDTO medicalRecord) { this.medicalRecord = medicalRecord; }
    public DoctorDTO getDoctor() { return doctor; }
    public void setDoctor(DoctorDTO doctor) { this.doctor = doctor; }
    public AvailabilityDTO getAvailability() { return availability; }
    public void setAvailability(AvailabilityDTO availability) { this.availability = availability; }
}