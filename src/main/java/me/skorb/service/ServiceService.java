package me.skorb.service;

import me.skorb.entity.Service;
import me.skorb.repository.ServiceRepository;

import java.util.List;

public class ServiceService {

    private final ServiceRepository serviceRepository = ServiceRepository.getInstance();

    public List<Service> getAllServices() {
        return serviceRepository.getAllServices();
    }

    public Service getServiceById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid service ID.");
        }
        return serviceRepository.getServiceById(id);
    }

    public List<Service> getServicesByCategoryId(int categoryId) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Invalid category ID.");
        }
        return serviceRepository.getServiceByCategoryId(categoryId);
    }

    public List<Service> getServicesByCategoryName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        return serviceRepository.getServiceByCategoryName(categoryName);
    }

    public List<String> getAllServiceCategories() {
        return serviceRepository.getAllServiceCategories();
    }

    public int getCategoryIdByCategoryName(String categoryName) {
        return serviceRepository.getCategoryIdByCategoryName(categoryName);
    }

    public void addService(Service service) {
        if (service == null || service.getName().isEmpty() || service.getPrice() == null) {
            throw new IllegalArgumentException("Service details cannot be empty.");
        }
        serviceRepository.save(service);
    }

    public void addCategory(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            throw new IllegalArgumentException("Service Category cannot be empty.");
        }
        serviceRepository.saveCategory(categoryName);
    }

    public void updateService(Service service) {
        if (service == null || service.getId() <= 0) {
            throw new IllegalArgumentException("Invalid service ID.");
        }
        serviceRepository.update(service);
    }

    public void updateCategory(int categoryId, String newCategoryName) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Invalid category ID.");
        }

        if (newCategoryName == null || newCategoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Service Category cannot be empty.");
        }

        serviceRepository.updateCategory(categoryId, newCategoryName);
    }

    public void deleteService(Service service) {
        if (service.getId() <= 0) {
            throw new IllegalArgumentException("Invalid service ID.");
        }
        serviceRepository.delete(service.getId());
    }

    public void deleteCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Service Category cannot be empty.");
        }
        serviceRepository.deleteCategory(categoryName);
    }

    public boolean isCategoryUsed(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Service Category cannot be empty.");
        }
        return serviceRepository.isCategoryUsed(categoryName);
    }

}
