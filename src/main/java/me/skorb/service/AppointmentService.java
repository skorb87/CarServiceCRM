package me.skorb.service;

import me.skorb.entity.Appointment;
import me.skorb.repository.AppointmentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AppointmentService {

    private final AppointmentRepository appointmentRepository = AppointmentRepository.getInstance();

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> getAppointmentById(int id) {
        return appointmentRepository.findById(id);
    }

    public List<Appointment> getAppointmentsByCustomer(int customerId) {
        return appointmentRepository.findByCustomerId(customerId);
    }

    public List<Appointment> getAppointmentsByVehicle(int vehicleId) {
        return appointmentRepository.findByVehicleId(vehicleId);
    }

    public List<Appointment> getAppointmentsByStatus(Appointment.Status status) {
        return appointmentRepository.findByStatus(status);
    }

    public List<Appointment> getAppointmentsBetweenDates(LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByDateTimeBetween(start, end);
    }

    public int getAppointmentsCountByStatus(String status) {
        return appointmentRepository.getAppointmentsCountByStatus(status);
    }

    public Appointment createAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public Appointment updateAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public void deleteAppointment(Appointment appointment) {
        appointmentRepository.deleteById(appointment.getId());
    }

}
