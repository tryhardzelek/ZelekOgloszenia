package pl.zelek.ogloszenia;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private final String defaultConfig = """
        # Konfiguracja pluginu ZelekOgloszenia

        server-name: "MojSerwer"

        announcement:
          fadeIn: 10
          stay: 70
          fadeOut: 20
        """;

    @Override
    public void onEnable() {
        createConfigIfNotExists();
        getLogger().info("ZelekOgloszenia włączony!");
    }

    private void createConfigIfNotExists() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getDataFolder().mkdirs();
            try (PrintWriter writer = new PrintWriter(configFile)) {
                writer.write(defaultConfig);
                getLogger().info("Utworzono domyślny config.yml");
            } catch (IOException e) {
                e.printStackTrace();
                getLogger().severe("Nie udało się utworzyć config.yml!");
            }
        }
        reloadConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("ogloszenie")) {

            if (!sender.hasPermission("ogloszenie.use")) {
                sender.sendMessage(color("&cNie masz permisji do użycia tej komendy!"));
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage(color("&cPodaj treść ogłoszenia!"));
                return true;
            }

            String serverName = getConfig().getString("server-name");
            int fadeIn = getConfig().getInt("announcement.fadeIn", 10);
            int stay = getConfig().getInt("announcement.stay", 70);
            int fadeOut = getConfig().getInt("announcement.fadeOut", 20);

            // Treść ogłoszenia wpisana przez admina
            String userMessage = String.join(" ", args);

            // Tworzymy finalny tytuł: NazwaSerwera | wiadomość od admina
            String message = serverName + " §8| §f" + userMessage;

            // Kolorowanie (HEX i &)
            message = color(message);

            // Wysyłamy TITLE wszystkim graczom
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendTitle(message, "", fadeIn, stay, fadeOut);
            }

            getLogger().info("Ogłoszenie wysłane: " + message);
            return true;
        }

        // Komenda do przeładowania configu
        if (command.getName().equalsIgnoreCase("ogloszeniereload")) {

            if (!sender.hasPermission("ogloszenie.reload")) {
                sender.sendMessage(color("&cNie masz permisji do przeładowania configu!"));
                return true;
            }

            reloadConfig();
            sender.sendMessage(color("&aConfig został przeładowany!"));
            getLogger().info("Config został przeładowany przez " + sender.getName());
            return true;
        }

        return false;
    }

    // Funkcja kolorująca HEX &#RRGGBB i klasyczne &a, &b
    public static String color(String text) {
        Pattern pattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String hex = matcher.group(1);
            text = text.replace("&#" + hex, "§x§" +
                    hex.charAt(0) + "§" + hex.charAt(1) +
                    "§" + hex.charAt(2) + "§" + hex.charAt(3) +
                    "§" + hex.charAt(4) + "§" + hex.charAt(5));
        }

        return text.replace("&", "§");
    }
}
