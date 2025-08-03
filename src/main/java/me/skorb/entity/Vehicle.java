package me.skorb.entity;

import java.util.Objects;

public class Vehicle {

    private int id;
    private int customerId;
    private Make make;
    private Model model;
    private Integer year;
    private String vin;
    private String licensePlate;

    public Vehicle() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Make getMake() {
        return make;
    }

    public void setMake(Make make) {
        this.make = make;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return customerId == vehicle.customerId && Objects.equals(make, vehicle.make) && Objects.equals(model, vehicle.model) && Objects.equals(year, vehicle.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, make, model, year);
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "vehicleId=" + id +
                ", customer=" + customerId +
                ", make=" + make +
                ", model=" + model +
                ", year=" + year +
                ", vin='" + vin + '\'' +
                ", licensePlate='" + licensePlate + '\'' +
                '}';
    }

}
