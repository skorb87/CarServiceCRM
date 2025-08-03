package me.skorb.service;

import me.skorb.entity.Part;
import me.skorb.repository.PartRepository;

import java.util.List;

public class PartService {

    private final PartRepository partRepository = PartRepository.getInstance();

    public List<Part> getAllParts() {
        return partRepository.getAllParts();
    }

    public Part getPartById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid part ID.");
        }
        return partRepository.getPartById(id);
    }

    public List<Part> getPartsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be empty.");
        }
        return partRepository.getPartsByName(name);
    }

    public List<String> getAllPartCategories() {
        return partRepository.getAllPartCategories();
    }

    public int getCategoryIdByCategoryName(String categoryName) {
        return partRepository.getCategoryIdByCategoryName(categoryName);
    }

    public void addPart(Part part) {
        if (part == null || part.getName().isEmpty() || part.getPrice() == null || part.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Invalid part details.");
        }
        partRepository.save(part);
    }

    public void addCategory(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            throw new IllegalArgumentException("Part Category cannot be empty.");
        }
        partRepository.saveCategory(categoryName);
    }

    public void updatePart(Part part) {
        if (part == null || part.getId() <= 0) {
            throw new IllegalArgumentException("Invalid part ID.");
        }
        partRepository.update(part);
    }

    public void updateCategory(int categoryId, String newCategoryName) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Invalid category ID.");
        }

        if (newCategoryName == null || newCategoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Part Category cannot be empty.");
        }

        partRepository.updateCategory(categoryId, newCategoryName);
    }

    public void deletePart(Part part) {
        if (part.getId() <= 0) {
            throw new IllegalArgumentException("Invalid part ID.");
        }
        partRepository.delete(part.getId());
    }

    public void deleteCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Part Category cannot be empty.");
        }
        partRepository.deleteCategory(categoryName);
    }

    public boolean isCategoryUsed(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Service Category cannot be empty.");
        }
        return partRepository.isCategoryUsed(categoryName);
    }
}
