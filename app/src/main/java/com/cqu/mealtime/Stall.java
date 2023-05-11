package com.cqu.mealtime;

public class Stall {
    private String name;
    private int type;
    private int id;
    private int location1;
    private int location2;
    private int time = 0;
    private int peopleCount = 0;

    public Stall(String name, int type, int id, int location1, int location2, int peopleCount) {
        this.name = name;
        this.type = type;
        this.id = id;
        this.location1 = location1;
        this.location2 = location2;
        this.peopleCount = peopleCount;
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

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getColor() {
        if (peopleCount < 10)
            return 0xFF4CAF50;
        else if (peopleCount < 20)
            return 0xffFCCC64;
        else
            return 0xFFE4192B;
    }

    public String getState() {
        if (peopleCount < 10)
            return "空闲";
        else if (peopleCount < 20)
            return "一般";
        else
            return "拥挤";
    }

    public int getWaitTime() {
        return peopleCount / 2;
    }
}
