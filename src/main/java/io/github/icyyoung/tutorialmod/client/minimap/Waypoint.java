package io.github.icyyoung.tutorialmod.client.minimap;

public class Waypoint {
    public String name;
    public int x;
    public int z;
    public int color; // ARGB
    public String dimension;

    public Waypoint(String name, int x, int z, int color) {
        this.name = name;
        this.x = x;
        this.z = z;
        this.color = color;
        this.dimension = "minecraft:overworld"; // default fallback
    }

    public Waypoint(String name, int x, int z, int color, String dimension) {
        this.name = name;
        this.x = x;
        this.z = z;
        this.color = color;
        this.dimension = dimension;
    }
}
