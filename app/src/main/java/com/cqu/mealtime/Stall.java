package com.cqu.mealtime;

public class Stall {
    private String name;
    private int type;
    private int id;
    private int location1;
    private int location2;
    private int flow = 0;
    private int time = 0;

    public Stall(String name, int type, int id, int location1, int location2) {
        this.name = name;
        this.type = type;
        this.id = id;
        this.location1 = location1;
        this.location2 = location2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLocation1() {
        return location1;
    }

    public void setLocation1(int location1) {
        this.location1 = location1;
    }

    public int getLocation2() {
        return location2;
    }

    public void setLocation2(int location2) {
        this.location2 = location2;
    }

    public int getFlow() {
        return flow;
    }

    public void setFlow(int flow) {
        this.flow = flow;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}