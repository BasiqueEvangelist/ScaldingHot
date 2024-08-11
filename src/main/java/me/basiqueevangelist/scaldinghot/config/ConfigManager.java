package me.basiqueevangelist.scaldinghot.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();
    private static final Logger LOGGER = LoggerFactory.getLogger("Scalding Hot!/ConfigManager");

    private ScaldingHotConfig config = new ScaldingHotConfig();

    public ConfigManager() {
        load();
    }

    public ScaldingHotConfig get() {
        return config;
    }

    public void load() {
        Path confPath = FabricLoader.getInstance().getConfigDir().resolve("scaldinghot.json");
        if (Files.exists(confPath)) {
            try (var reader = Files.newBufferedReader(confPath)) {
                config = GSON.fromJson(reader, ScaldingHotConfig.class);
            } catch (IOException | JsonParseException e) {
                LOGGER.error("Could not load config file!", e);
            }
        } else {
            save();
        }
    }

    public void save() {
        Path confPath = FabricLoader.getInstance().getConfigDir().resolve("scaldinghot.json");
        try {
            try (BufferedWriter bw = Files.newBufferedWriter(confPath)) {
                GSON.toJson(config, ScaldingHotConfig.class, bw);
            }
        } catch (IOException e) {
            LOGGER.error("Could not save config file!", e);
        }
    }
}