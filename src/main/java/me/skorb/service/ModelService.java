package me.skorb.service;

import me.skorb.entity.Make;
import me.skorb.entity.Model;
import me.skorb.repository.ModelRepository;

public class ModelService {

    private final ModelRepository modelRepository = ModelRepository.getInstance();

    public Model getModelByNameAndMake(String name, Make make) {
        return modelRepository.findByNameAndMake(name, make).orElse(null);
    }

}
