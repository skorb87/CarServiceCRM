package me.skorb.service;

import me.skorb.entity.Employee;
import me.skorb.repository.EmployeeRepository;
import java.util.List;

public class EmployeeService {

    private final EmployeeRepository employeeRepository = EmployeeRepository.getInstance();

    public List<Employee> getAllEmployees() {
        return employeeRepository.getAllEmployees();
    }

    public void saveEmployee(Employee employee) {
        if (employee == null || employee.getFirstName().isEmpty() || employee.getLastName().isEmpty()) {
            throw new IllegalArgumentException("Employee details cannot be empty");
        }
        employeeRepository.save(employee);
    }

    public void updateEmployee(Employee employee) {
        if (employee == null || employee.getId() <= 0) {
            throw new IllegalArgumentException("Invalid employee ID");
        }
        employeeRepository.update(employee);
    }

    public void deleteEmployee(Employee employee) {
        if (employee.getId() <= 0) {
            throw new IllegalArgumentException("Invalid employee ID");
        }
        employeeRepository.delete(employee.getId());
    }

}
