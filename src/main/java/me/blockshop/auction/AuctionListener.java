package me.blockshop.auction;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AuctionListener implements Listener {
    private final AuctionPlugin plugin;
    private String lastClickedId = ""; // Упрощенное хранение для примера

    public AuctionListener(AuctionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (title.contains("Аукцион BlockShop")) {
            event.setCancelled(true);
            if (event.getRawSlot() >= 10 && event.getRawSlot() <= 43) {
                handleMainClick(player, event.getRawSlot());
            }
        } else if (title.contains("Подтверждение покупки")) {
            event.setCancelled(true);
            if (event.getRawSlot() == 11) confirmPurchase(player);
            if (event.getRawSlot() == 15) player.closeInventory();
        }
    }

    private void handleMainClick(Player player, int slot) {
        if (plugin.getConfig().getConfigurationSection("items") == null) return;
        
        // Поиск ID лота по слоту
        int currentSlot = 10;
        for (String key : plugin.getConfig().getConfigurationSection("items").getKeys(false)) {
            if (currentSlot % 9 == 8 || currentSlot % 9 == 0) currentSlot++;
            if (currentSlot == slot) {
                lastClickedId = key;
                openConfirmMenu(player, key);
                break;
            }
            currentSlot++;
        }
    }

    private void openConfirmMenu(Player player, String key) {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text("Подтверждение покупки"));
        double price = plugin.getConfig().getDouble("items." + key + ".price");

        ItemStack buy = new ItemStack(Material.GREEN_WOOL);
        ItemMeta bMeta = buy.getItemMeta();
        bMeta.displayName(Component.text("ПОДТВЕРДИТЬ КУПЛЮ", NamedTextColor.GREEN));
        bMeta.lore(List.of(Component.text("Цена: " + price + "$", NamedTextColor.GOLD)));
        buy.setItemMeta(bMeta);

        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta cMeta = cancel.getItemMeta();
        cMeta.displayName(Component.text("ОТМЕНИТЬ", NamedTextColor.RED));
        cancel.setItemMeta(cMeta);

        gui.setItem(11, buy);
        gui.setItem(15, cancel);
        player.openInventory(gui);
    }

    private void confirmPurchase(Player player) {
        if (lastClickedId.isEmpty() || plugin.getConfig().get("items." + lastClickedId) == null) return;

        double price = plugin.getConfig().getDouble("items." + lastClickedId + ".price");
        String balanceRaw = PlaceholderAPI.setPlaceholders(player, "%vault_eco_balance%");
        
        try {
            double balance = Double.parseDouble(balanceRaw.replace(",", ""));
            if (balance >= price) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco take " + player.getName() + " " + price);
                ItemStack item = plugin.getConfig().getItemStack("items." + lastClickedId + ".item");
                
                if (item != null) player.getInventory().addItem(item);
                
                plugin.getConfig().set("items." + lastClickedId, null);
                plugin.saveConfig();
                
                player.sendMessage(Component.text("Покупка успешна!", NamedTextColor.GREEN));
                player.closeInventory();
            } else {
                player.sendMessage(Component.text("Недостаточно денег!", NamedTextColor.RED));
                player.closeInventory();
            }
        } catch (Exception e) {
            player.sendMessage(Component.text("Ошибка экономики!", NamedTextColor.RED));
        }
    }
}
