package me.skorb.entity;

import java.util.List;
import java.util.Objects;

public class Customer {

    private int id;

    private String firstName;

    private String lastName;

    private String phone;

    private String email;

    private String address;

    private String city;

    private State state;

    private String postalCode;

    private List<Vehicle> vehicles;

    public Customer() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(firstName, customer.firstName) && Objects.equals(lastName, customer.lastName) && Objects.equals(phone, customer.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, phone);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", state=" + state +
                ", postalCode='" + postalCode + '\'' +
                '}';
    }

    public enum State {
        ALABAMA("AL"),
        ALASKA("AK"),
        ARIZONA("AZ"),
        ARKANSAS("AR"),
        CALIFORNIA("CA"),
        COLORADO("CO"),
        CONNECTICUT("CT"),
        DELAWARE("DE"),
        FLORIDA("FL"),
        GEORGIA("GA"),
        HAWAII("HI"),
        IDAHO("ID"),
        ILLINOIS("IL"),
        INDIANA("IN"),
        IOWA("IA"),
        KANSAS("KS"),
        KENTUCKY("KY"),
        LOUISIANA("LA"),
        MAINE("ME"),
        MARYLAND("MD"),
        MASSACHUSETTS("MA"),
        MICHIGAN("MI"),
        MINNESOTA("MN"),
        MISSISSIPPI("MS"),
        MISSOURI("MO"),
        MONTANA("MT"),
        NEBRASKA("NE"),
        NEVADA("NV"),
        NEW_HAMPSHIRE("NH"),
        NEW_JERSEY("NJ"),
        NEW_MEXICO("NM"),
        NEW_YORK("NY"),
        NORTH_CAROLINA("NC"),
        NORTH_DAKOTA("ND"),
        OHIO("OH"),
        OKLAHOMA("OK"),
        OREGON("OR"),
        PENNSYLVANIA("PA"),
        RHODE_ISLAND("RI"),
        SOUTH_CAROLINA("SC"),
        SOUTH_DAKOTA("SD"),
        TENNESSEE("TN"),
        TEXAS("TX"),
        UTAH("UT"),
        VERMONT("VT"),
        VIRGINIA("VA"),
        WASHINGTON("WA"),
        WEST_VIRGINIA("WV"),
        WISCONSIN("WI"),
        WYOMING("WY");

        private final String abbreviation;

        State(String abbreviation) {
            this.abbreviation = abbreviation;
        }

        public String getAbbreviation() {
            return abbreviation;
        }

        @Override
        public String toString() {
            // Преобразует название в более читаемый вид: NEW_YORK → New York
            String name = name().toLowerCase().replace('_', ' ');
            String[] words = name.split(" ");
            StringBuilder sb = new StringBuilder();
            for (String word : words) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1)).append(" ");
            }
            return sb.toString().trim();
        }
    }

}