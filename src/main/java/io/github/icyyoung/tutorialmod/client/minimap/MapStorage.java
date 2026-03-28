package io.github.icyyoung.tutorialmod.client.minimap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import java.util.HashMap;

public class MapStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static String currentSaveDirId;

    public static List<Waypoint> waypoints = new ArrayList<>();

    public static void setWorld(String id) {
        currentSaveDirId = id.replaceAll("[^a-zA-Z0-9.-]", "_");
        MapDataManager.clear();
        waypoints.clear();
        load();
    }

    public static void save() {
        if (currentSaveDirId == null) return;
        Path dir = getDir();
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        
        // Save waypoints
        File waypointsFile = dir.resolve("waypoints.json").toFile();
        try (FileWriter writer = new FileWriter(waypointsFile)) {
            GSON.toJson(waypoints, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save map data
        File mapDataFile = dir.resolve("mapdata.json").toFile();
        try (FileWriter writer = new FileWriter(mapDataFile)) {
            // Convert byte arrays to base64 for easy JSON storage
            Map<Long, String> encodedMap = new HashMap<>();
            for (Map.Entry<Long, byte[]> entry : MapDataManager.chunkData.entrySet()) {
                encodedMap.put(entry.getKey(), Base64.getEncoder().encodeToString(entry.getValue()));
            }
            GSON.toJson(encodedMap, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        if (currentSaveDirId == null) return;
        Path dir = getDir();

        // Load waypoints
        File waypointsFile = dir.resolve("waypoints.json").toFile();
        if (waypointsFile.exists()) {
            try (FileReader reader = new FileReader(waypointsFile)) {
                Type listType = new TypeToken<ArrayList<Waypoint>>() {}.getType();
                List<Waypoint> loaded = GSON.fromJson(reader, listType);
                if (loaded != null) {
                    waypoints.addAll(loaded);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Load map data
        File mapDataFile = dir.resolve("mapdata.json").toFile();
        if (mapDataFile.exists()) {
            try (FileReader reader = new FileReader(mapDataFile)) {
                Type mapType = new TypeToken<HashMap<Long, String>>() {}.getType();
                Map<Long, String> encodedMap = GSON.fromJson(reader, mapType);
                if (encodedMap != null) {
                    for (Map.Entry<Long, String> entry : encodedMap.entrySet()) {
                        byte[] data = Base64.getDecoder().decode(entry.getValue());
                        MapDataManager.chunkData.put(entry.getKey(), data);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Path getDir() {
        return FMLPaths.GAMEDIR.get().resolve("tutorialmod").resolve("minimap").resolve(currentSaveDirId);
    }
}