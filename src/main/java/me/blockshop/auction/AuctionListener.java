package me.blockshop.auction;

import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class AuctionListener implements Listener {

    private final AuctionPlugin plugin;

    public AuctionListener(AuctionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        // Проверка по заголовку (используем plainText для надежности)
        if (event.getView().title().toString().contains("Аукцион BlockShop")) {
            event.setCancelled(true); // Запрещаем доставать предметы
        }
    }
}
