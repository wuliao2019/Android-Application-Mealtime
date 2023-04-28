package com.cqu.mealtime;

import com.amap.api.maps.model.LatLng;

public class Canteen {
    private String name;
    private int id;
    private int flow = 0;
    private String time = "06:00-22:00";

    private LatLng location;

    private String v_url;

    public String getV_url() {
        return v_url;
    }

    public void setV_url(String v_url) {
        this.v_url = v_url;
    }

    public Canteen(String name, int id, double x, double y, String time, String v_url) {
        this.name = name;
        this.id = id;
        this.location = new LatLng(x, y);
        this.time = time;
        this.v_url = v_url;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getFlow() {
        return flow;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFlow(int flow) {
        this.flow = flow;
    }

    public LatLng getLocation() {
        return location;
    }

    public int getColor() {
        if (flow < 250)
            return 0xff206420;
        else if (flow < 500)
            return 0xff20a020;
        else if (flow < 750)
            return 0xffd2d200;
        else
            return 0xffc82020;
    }

    public String getState() {
        if (flow < 250)
            return "空闲";
        else if (flow < 500)
            return "流量正常";
        else if (flow < 750)
            return "流量较大";
        else
            return "拥挤";
    }
}
