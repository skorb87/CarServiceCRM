package me.skorb.service;

import me.skorb.entity.Vehicle;
import me.skorb.repository.VehicleRepository;

import java.util.List;
import java.util.Optional;

public class VehicleService {

    private final VehicleRepository vehicleRepository = VehicleRepository.getInstance();

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Optional<Vehicle> getVehicleById(int id) {
        return vehicleRepository.findById(id);
    }

    public boolean isVinAlreadyTaken(String vin) {
        return vehicleRepository.existsByVIN(vin);
    }

    public Vehicle saveVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public void updateVehicle(Vehicle vehicle) {
        vehicleRepository.save(vehicle);
    }

    public void deleteVehicle(Vehicle vehicle) {
        vehicleRepository.deleteById(vehicle.getId());
    }
}
