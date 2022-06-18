package com.benj4.gcpm.objects;

import java.util.Arrays;
import java.util.List;

public class GCPMGroup {
    private String name;
    private String prefix;
    public int weight;
    private String[] permissions;

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getWeight() {
        return weight;
    }

    public List<String> getPermissions() {
        return Arrays.stream(permissions).toList();
    }
}
