package net.mat0u5.lifeseries.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.mat0u5.lifeseries.Main;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class UpdateChecker {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    public static boolean updateAvailable = false;
    public static String versionName;

    public static void checkForUpdates() {
        executor.submit(() -> {
            HttpURLConnection connection = null;
            try {
                // Connect to the GitHub API
                connection = (HttpURLConnection) new URL(Main.GITHUB_API_URL).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                if (connection.getResponseCode() == 200) {
                    // Parse the JSON response
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                    versionName = json.get("tag_name").getAsString();

                    // Compare the current version with the latest version
                    if (!Main.MOD_VERSION.equalsIgnoreCase(versionName)) {
                        Main.LOGGER.info("New version found: "+versionName);
                        updateAvailable = true;
                    }
                } else {
                    Main.LOGGER.error("Failed to fetch update info: " + connection.getResponseCode());
                }
            } catch (Exception e) {
                Main.LOGGER.error("Error while checking for updates: " + e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    public static void onPlayerJoin(ServerPlayerEntity player) {
        if (!updateAvailable || versionName == null) {
            return;
        }
        Text updateText =
                Text.literal("A new version of the mod is available ("+versionName+"). \n")
                .append(
                    Text.literal("Click to download on Modrinth")
                        .styled(style -> style
                            .withColor(Formatting.BLUE)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/life-series"))
                            .withUnderline(true)
                        )
                );
        player.sendMessage(updateText);
    }
    public static void shutdownExecutor() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
