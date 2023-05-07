package com.cqu.mealtime.ui.dashboard;

import com.cqu.mealtime.Stall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DashboardData {
    static int limit_type = 0;
    static int limit_can = 0;
    static int limit_loc = 0;
    static boolean changed = true;
    static final List<String> loc = new ArrayList<>();
    static final List<String> canteens = new ArrayList<>();
    static final List<List<String>> locations = new ArrayList<>();
    static final List<List<Integer>> locId = new ArrayList<>();
    static final List<String> types = new ArrayList<>();
    static final List<Stall> stalls = new ArrayList<>();
    static final List<Integer> backColors = Arrays.asList(0xFFF2994B, 0xFF2DBCA7, 0xFFD13681, 0xFFCC780C, 0xFFDB6B69, 0xFFE27C0F, 0xFFE08F7F, 0xFF064389, 0xFF0A6887, 0xFF129B1B, 0xFFED4523, 0xFF847C2B);
}
