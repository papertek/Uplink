package ru.ijo42.uplink.api.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Logger;
import ru.ijo42.uplink.api.UplinkAPI;
import ru.ijo42.uplink.api.config.Config;
import ru.ijo42.uplink.api.config.display.GUIDisplay;
import ru.ijo42.uplink.api.config.display.ServerDisplay;
import ru.ijo42.uplink.api.config.display.SmallDisplay;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

class LoadingUtils {

    private static final Gson gson = new GsonBuilder().create();
    private static Path configPath;
    private static Logger logger;

    public static void init(Path configPath) {
        LoadingUtils.configPath = configPath;
        LoadingUtils.logger = UplinkAPI.getLogger();
    }

    public static ServerDisplay[] load(ServerDisplay ignored, Config config) throws IOException {
        if (isUsingWeb(config.displayUrls.server)) {
            logger.info("[ServerDisplay] Config uses HTTP(S) => Using webChannel");
            return loadFromWeb(new ServerDisplay(), new URL(isUseJSON(config.displayUrls.server) ? config.displayUrls.server :
                    config.displayUrls.server + config.clientId + ".json"));

        } else if (isUsingFile(config.displayUrls.server)) {
            logger.info("[ServerDisplay] Config uses FILE => Using fileChannel");
            return loadFromLocalFile(new ServerDisplay(), config.displayUrls.server);
        } else {
            logger.error("[ServerDisplay] Config dont uses HTTP(S) / FILE => Using default");
            return null;
        }
    }

    public static SmallDisplay[] load(SmallDisplay ignored, Config config) throws IOException {
        if (isUsingWeb(config.displayUrls.small)) {
            logger.info("[SmallDisplay] Config uses HTTP(S) => Using webChannel");
            return loadFromWeb(new SmallDisplay(), new URL(isUseJSON(config.displayUrls.small) ? config.displayUrls.small :
                    config.displayUrls.small + config.clientId + ".json"));

        } else if (isUsingFile(config.displayUrls.small)) {
            logger.info("[SmallDisplay] Config uses FILE => Using fileChannel");
            return loadFromLocalFile(new SmallDisplay(), config.displayUrls.small);
        } else {
            logger.error("[SmallDisplay] Config dont uses HTTP(S) / FILE => Using default");
            return null;
        }
    }

    public static GUIDisplay load(GUIDisplay ignored, Config config) throws IOException {
        if (isUsingWeb(config.displayUrls.gui) && isUseJSON(config.displayUrls.gui)) {
            logger.info("[GUIDisplay] Config uses HTTP(S) => Using webChannel");
            return loadFromWeb(new GUIDisplay(), new URL(config.displayUrls.gui));

        } else if (isUsingFile(config.displayUrls.gui)) {
            logger.info("[GUIDisplay] Config uses FILE => Using fileChannel");
            return loadFromLocalFile(new GUIDisplay(), config.displayUrls.gui);
        } else {
            logger.error("[GUIDisplay] Config dont uses HTTP(S) / FILE => Using default");
            return null;
        }
    }

    public static SmallDisplay[] loadFromWeb(SmallDisplay ignored, URL url) throws IOException {
        return gson.fromJson(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8), SmallDisplay[].class);
    }

    public static ServerDisplay[] loadFromWeb(ServerDisplay ignored, URL url) throws IOException {
        return gson.fromJson(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8), ServerDisplay[].class);
    }

    public static GUIDisplay loadFromWeb(GUIDisplay ignored, URL url) throws IOException {
        return gson.fromJson(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8), GUIDisplay.class);
    }

    public static SmallDisplay[] loadFromDefault(SmallDisplay ignored) {
        return gson.fromJson(new InputStreamReader(UplinkAPI.getResource("Smalls.json")), SmallDisplay[].class);
    }

    public static ServerDisplay[] loadFromDefault(ServerDisplay ignored) {
        return gson.fromJson(new InputStreamReader(UplinkAPI.getResource("Servers.json")), ServerDisplay[].class);
    }

    public static GUIDisplay loadFromDefault(GUIDisplay ignored) {
        return gson.fromJson(new InputStreamReader(UplinkAPI.getResource("GUI.json")), GUIDisplay.class);
    }

    public static SmallDisplay[] loadFromLocalFile(SmallDisplay ignored, String configName) throws IOException {
        // 'file://' : 7
        Path configPath = LoadingUtils.configPath.resolve(configName.substring(7));
        return gson.fromJson(Files.newBufferedReader(configPath), SmallDisplay[].class);
    }

    public static ServerDisplay[] loadFromLocalFile(ServerDisplay ignored, String configName) throws IOException {
        Path configPath = LoadingUtils.configPath.resolve(configName.substring(7));
        return gson.fromJson(Files.newBufferedReader(configPath), ServerDisplay[].class);
    }

    public static GUIDisplay loadFromLocalFile(GUIDisplay ignored, String configName) throws IOException {
        Path configPath = LoadingUtils.configPath.resolve(configName.substring(7));
        return gson.fromJson(Files.newBufferedReader(configPath), GUIDisplay.class);
    }

    private static boolean isUseJSON(String str) {
        return str.endsWith(".json");
    }

    public static boolean isUsingFile(String str) {
        return str.startsWith("file://");
    }

    public static boolean isUsingWeb(String str) {
        return str.startsWith("https") || str.startsWith("http");
    }

}
