package me.skorb.entity;

public class Model {

    private int id;

    private String name;

    private int makeId;

    public Model() {}

    public Model(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Model(String name, int makeId) {
        this.name = name;
        this.makeId = makeId;
    }

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

    public int getMakeId() {
        return makeId;
    }

    public void setMakeId(int makeId) {
        this.makeId = makeId;
    }

//    @Override
//    public String toString() {
//        return "Model{" +
//                "modelId=" + id +
//                ", modelName='" + name + '\'' +
//                ", make=" + make.getName() +
//                '}';
//    }
}

