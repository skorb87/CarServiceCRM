package me.skorb.entity;

import java.util.List;

public class Make {

    private int id;

    private String name;

    private List<Model> models;

    public Make() {}

    public Make(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Make(String name) {
        this.name = name;
    }

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

    public List<Model> getModels() {
        return models;
    }

    public void setModels(List<Model> models) {
        this.models = models;
    }

    @Override
    public String toString() {
        return "Make{" +
                "makeId=" + id +
                ", makeName='" + name + '\'' +
                '}';
    }
}

