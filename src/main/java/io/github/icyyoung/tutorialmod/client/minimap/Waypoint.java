package io.github.icyyoung.tutorialmod.client.minimap;

public class Waypoint {
    public String name;
    public int x;
    public int z;
    public int color; // ARGB

    public Waypoint(String name, int x, int z, int color) {
        this.name = name;
        this.x = x;
        this.z = z;
        this.color = color;
    }
}