package io.github.icyyoung.tutorialmod.client.minimap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelResource;
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
    private static Path currentSaveDir;

    public static List<Waypoint> waypoints = new ArrayList<>();

    public static void setSaveDir(Path dir) {
        currentSaveDir = dir;
        MapDataManager.clear();
        waypoints.clear();
        if (currentSaveDir != null) {
            load();
        }
    }

    public static void save() {
        if (currentSaveDir == null) return;
        Path dir = currentSaveDir;
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

        // Save map data per dimension
        Map<String, Map<Long, String>> encodedData = new HashMap<>();

        for (Map.Entry<String, Map<Long, int[]>> dimEntry : MapDataManager.dimensionMapData.entrySet()) {
            Map<Long, String> encodedMap = new HashMap<>();
            for (Map.Entry<Long, int[]> entry : dimEntry.getValue().entrySet()) {
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
            encodedData.put(dimEntry.getKey(), encodedMap);
        }

        File mapDataFile = dir.resolve("mapdata_v2.json").toFile();
        try (FileWriter writer = new FileWriter(mapDataFile)) {
            GSON.toJson(encodedData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        if (currentSaveDir == null) return;
        Path dir = currentSaveDir;

        // Load waypoints
        File waypointsFile = dir.resolve("waypoints.json").toFile();
        if (waypointsFile.exists()) {
            try (FileReader reader = new FileReader(waypointsFile)) {
                Type listType = new TypeToken<ArrayList<Waypoint>>() {}.getType();
                List<Waypoint> loaded = GSON.fromJson(reader, listType);
                if (loaded != null) {
                    for (Waypoint wp : loaded) {
                        if (wp.dimension == null) wp.dimension = "minecraft:overworld";
                    }
                    waypoints.addAll(loaded);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Load map data
        File mapDataFileV2 = dir.resolve("mapdata_v2.json").toFile();
        if (mapDataFileV2.exists()) {
            try (FileReader reader = new FileReader(mapDataFileV2)) {
                Type mapType = new TypeToken<HashMap<String, HashMap<Long, String>>>() {}.getType();
                Map<String, Map<Long, String>> encodedData = GSON.fromJson(reader, mapType);
                if (encodedData != null) {
                    for (Map.Entry<String, Map<Long, String>> dimEntry : encodedData.entrySet()) {
                        String dim = dimEntry.getKey();
                        for (Map.Entry<Long, String> entry : dimEntry.getValue().entrySet()) {
                            decodeAndPut(dim, entry.getKey(), entry.getValue());
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Legacy load
            File mapDataFileV1 = dir.resolve("mapdata.json").toFile();
            if (mapDataFileV1.exists()) {
                try (FileReader reader = new FileReader(mapDataFileV1)) {
                    Type mapType = new TypeToken<HashMap<Long, String>>() {}.getType();
                    Map<Long, String> encodedMap = GSON.fromJson(reader, mapType);
                    if (encodedMap != null) {
                        for (Map.Entry<Long, String> entry : encodedMap.entrySet()) {
                            decodeAndPut("minecraft:overworld", entry.getKey(), entry.getValue());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void decodeAndPut(String dim, long key, String base64Str) {
        byte[] bytes = Base64.getDecoder().decode(base64Str);
        if (bytes.length != 1024) return; // Must be 256 ints (1024 bytes)
        int[] ints = new int[bytes.length / 4];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = ((bytes[i * 4] & 0xFF) << 24) |
                      ((bytes[i * 4 + 1] & 0xFF) << 16) |
                      ((bytes[i * 4 + 2] & 0xFF) << 8) |
                      (bytes[i * 4 + 3] & 0xFF);
        }
        MapDataManager.getChunkDataMap(dim).put(key, ints);
    }
}
