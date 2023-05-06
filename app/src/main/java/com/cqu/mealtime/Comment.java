package com.cqu.mealtime;

public class Comment {
    String title;
    String remark;
    String time;
    String username;
    int can_id;
    int stall_id;

    public Comment(String title, String remark, String time, String username, int can_id, int stall_id) {
        this.title = title;
        this.remark = remark;
        this.time = time;
        this.username = username;
        this.can_id = can_id;
        this.stall_id = stall_id;
    }

    public String getTitle() {
        return title;
    }

    public String getRemark() {
        return remark;
    }

    public String getTime() {
        return time;
    }

    public String getUsername() {
        return username;
    }

    public int getCan_id() {
        return can_id;
    }

    public int getStall_id() {
        return stall_id;
    }
}
