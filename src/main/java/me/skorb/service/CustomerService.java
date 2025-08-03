package me.skorb.service;

import me.skorb.entity.Customer;
import me.skorb.entity.Vehicle;
import me.skorb.repository.CustomerRepository;
import me.skorb.repository.VehicleRepository;

import java.util.List;
import java.util.Optional;

public class CustomerService {

    private final CustomerRepository customerRepository = CustomerRepository.getInstance();
    private final VehicleRepository vehicleRepository = VehicleRepository.getInstance();

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(int id) {
        return customerRepository.findById(id);
    }

    public List<Customer> findByName(String firstName, String lastName) {
        return customerRepository.findByName(firstName, lastName);
    }

    public Optional<Customer> findByPhone(String phone) {
        return customerRepository.findByPhone(phone);
    }

    public Optional<Customer> findByVehicleVin(String vin) {
        return customerRepository.findByVehicleVin(vin);
    }

    public List<Vehicle> getVehiclesByCustomerId(int customerId) {
        return customerRepository.getVehiclesByCustomerId(customerId);
    }

    public boolean existsByPhone(String phone) {
        return customerRepository.existsByPhone(phone);
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public void updateCustomer(Customer customer) {
        customerRepository.save(customer);
    }

    public void deleteCustomer(Customer customer) {
        customerRepository.deleteById(customer.getId());
    }

    public boolean updateFirstName(int id, String firstName) {
        return customerRepository.updateFirstName(id, firstName) > 0;
    }

    public boolean updateLastName(int id, String lastName) {
        return customerRepository.updateLastName(id, lastName) > 0;
    }

    public boolean updateAddress(int id, String address) {
        return customerRepository.updateAddress(id, address) > 0;
    }

    public boolean updateCity(int id, String city) {
        return customerRepository.updateCity(id, city) > 0;
    }

    public boolean updateState(int id, String state) {
        return customerRepository.updateState(id, state) > 0;
    }

    public boolean updatePhone(int id, String phone) {
        return customerRepository.updatePhone(id, phone) > 0;
    }

    public boolean updateEmail(int id, String email) {
        return customerRepository.updateEmail(id, email) > 0;
    }

    public void addVehicleToCustomer(int customerId, Vehicle vehicle) {
        if (vehicle != null) {
            vehicle.setCustomerId(customerId);
            vehicleRepository.save(vehicle);
        }
    }


}
