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
            // Convert int arrays to base64 for easy JSON storage
            Map<Long, String> encodedMap = new HashMap<>();
            for (Map.Entry<Long, int[]> entry : MapDataManager.chunkData.entrySet()) {
                int[] ints = entry.getValue();
                byte[] bytes = new byte[ints.length * 4];
                for (int i = 0; i < ints.length; i++) {
                    bytes[i * 4] = (byte) (ints[i] >> 24);
                    bytes[i * 4 + 1] = (byte) (ints[i] >> 16);
                    bytes[i * 4 + 2] = (byte) (ints[i] >> 8);
                    bytes[i * 4 + 3] = (byte) (ints[i]);
                }
                encodedMap.put(entry.getKey(), Base64.getEncoder().encodeToString(bytes));
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
                        byte[] bytes = Base64.getDecoder().decode(entry.getValue());
                        if (bytes.length != 1024) continue; // Ignore old format data (256 bytes) to prevent crashes
                        int[] ints = new int[bytes.length / 4];
                        for (int i = 0; i < ints.length; i++) {
                            ints[i] = ((bytes[i * 4] & 0xFF) << 24) |
                                      ((bytes[i * 4 + 1] & 0xFF) << 16) |
                                      ((bytes[i * 4 + 2] & 0xFF) << 8) |
                                      (bytes[i * 4 + 3] & 0xFF);
                        }
                        MapDataManager.chunkData.put(entry.getKey(), ints);
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