package me.skorb.service;

import me.skorb.entity.Make;
import me.skorb.repository.MakeRepository;

import java.util.List;

public class MakeService {

    private final MakeRepository makeRepository = MakeRepository.getInstance();

    public List<Make> getAllMakes() {
        return makeRepository.findAll();
    }

    public List<Make> getAllMakesWithModels() {
        List<Make> makes = makeRepository.findAll();
        makes.forEach(make -> make.getModels().size()); // Force initialization
        return makes;
    }

    public Make getMakeByName(String name) {
        return makeRepository.findByName(name).orElse(null);
    }

}
