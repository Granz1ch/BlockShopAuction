package me.blockshop.auction;

import org.bukkit.plugin.java.JavaPlugin;

public class AuctionPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Создаем конфиг-файл, если его еще нет
        saveDefaultConfig();

        // Регистрируем команду /auction и её алиасы (ah, ac)
        AuctionCommand auctionCommand = new AuctionCommand(this);
        getCommand("auction").setExecutor(auctionCommand);

        // Регистрируем слушатель событий для работы GUI
        getServer().getPluginManager().registerEvents(new AuctionListener(this), this);

        getLogger().info("========================================");
        getLogger().info("BlockShop Auction v1.0 успешно включен!");
        getLogger().info("Версия Minecraft: 1.21.1 (Paper)");
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        // Сохраняем конфиг при выключении плагина
        saveConfig();
        getLogger().info("BlockShop Auction выключен. Данные сохранены.");
    }
}
