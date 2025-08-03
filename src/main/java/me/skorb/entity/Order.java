package me.skorb.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Order {

    private int id;
    private Type type;
    private Customer customer;
    private Vehicle vehicle;
    private LocalDate date;
    private int odometer;
    private Status status = Status.Pending;
    private BigDecimal laborHours = new BigDecimal(1); // default value = 1 hour
    private BigDecimal totalCost = new BigDecimal(0);
    private String notes;
    private Map<Part, Integer> partsWithQuantities;
    private List<Service> servicesProvided;
    private List<Employee> employeesAssigned;

    public enum Status {
        Pending, In_Progress, Completed, Canceled
    }

    public enum Type {
        REPAIR, MAINTENANCE, INSPECTION, INSTALLATION, DETAILING, OTHER
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getOdometer() {
        return odometer;
    }

    public void setOdometer(int odometer) {
        this.odometer = odometer;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public BigDecimal getLaborHours() {
        return laborHours;
    }

    public void setLaborHours(BigDecimal laborHours) {
        this.laborHours = laborHours;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Map<Part, Integer> getPartsWithQuantities() {
        return partsWithQuantities;
    }

    public void setPartsWithQuantities(Map<Part, Integer> partsWithQuantities) {
        this.partsWithQuantities = partsWithQuantities;
        calculateOrderTotalCost();
    }

    public List<Service> getServicesProvided() {
        return servicesProvided;
    }

    public void setServicesProvided(List<Service> servicesProvided) {
        this.servicesProvided = servicesProvided;
        calculateOrderTotalCost();
    }

    public List<Employee> getEmployeesAssigned() {
        return employeesAssigned;
    }

    public void setEmployeesAssigned(List<Employee> employeesAssigned) {
        this.employeesAssigned = employeesAssigned;
        calculateOrderTotalCost();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    private void calculateOrderTotalCost() {
        totalCost = new BigDecimal(0);

        if (partsWithQuantities != null && !partsWithQuantities.isEmpty()) {
            for (Map.Entry<Part, Integer> partWithQuantity : partsWithQuantities.entrySet()) {
                Part part = partWithQuantity.getKey();
                int quantity = partWithQuantity.getValue();
                totalCost = totalCost.add(part.getPrice().multiply(BigDecimal.valueOf(quantity)));
            }
        }

        if (servicesProvided != null && !servicesProvided.isEmpty()) {
            for (Service service : servicesProvided) {
                totalCost = totalCost.add(service.getPrice());
            }
        }

        if (employeesAssigned != null && !employeesAssigned.isEmpty()) {
            for (Employee employee : employeesAssigned) {
                totalCost = totalCost.add(employee.getLaborPrice().multiply(laborHours));
            }
        }
    }

}

