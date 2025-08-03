package me.skorb.entity;

import java.math.BigDecimal;
import java.util.Objects;

public class Service {

    private int id;
    private String name;
    private String description;
    private BigDecimal price = new BigDecimal(0); // Default value = 0
    private int categoryId;  // New field
    private String categoryName; // New field (for display purposes)

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return id == service.id; // Compare only by ID for now
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Hash only the ID
    }

    @Override
    public String toString() {
        return name;
    }
}
