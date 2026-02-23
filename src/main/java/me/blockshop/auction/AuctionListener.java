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
    private String lastClickedId = ""; 

    public AuctionListener(AuctionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (title.contains("Аукцион BlockShop")) {
            event.setCancelled(true);
            if (event.getRawSlot() >= 10 && event.getRawSlot() <= 43) {
                handleMainClick(player, event.getRawSlot());
            }
        } else if (title.contains("Подтверждение")) {
            event.setCancelled(true);
            if (event.getRawSlot() == 11) confirmPurchase(player);
            if (event.getRawSlot() == 15) player.closeInventory();
        }
    }

    private void handleMainClick(Player player, int slot) {
        if (plugin.getConfig().getConfigurationSection("items") == null) return;
        int currentSlot = 10;
        for (String key : plugin.getConfig().getConfigurationSection("items").getKeys(false)) {
            while (currentSlot % 9 == 8 || currentSlot % 9 == 0) currentSlot++;
            if (currentSlot == slot) {
                lastClickedId = key;
                openConfirmMenu(player, key);
                return;
            }
            currentSlot++;
        }
    }

    private void openConfirmMenu(Player player, String key) {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text("Подтверждение"));
        double price = plugin.getConfig().getDouble("items." + key + ".price");

        ItemStack buy = new ItemStack(Material.GREEN_WOOL);
        ItemMeta bMeta = buy.getItemMeta();
        if (bMeta != null) {
            bMeta.displayName(Component.text("КУПИТЬ", NamedTextColor.GREEN));
            bMeta.lore(List.of(Component.text("Цена: " + price + "$", NamedTextColor.GOLD)));
            buy.setItemMeta(bMeta);
        }

        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta cMeta = cancel.getItemMeta();
        if (cMeta != null) {
            cMeta.displayName(Component.text("ОТМЕНА", NamedTextColor.RED));
            cancel.setItemMeta(cMeta);
        }

        gui.setItem(11, buy);
        gui.setItem(15, cancel);
        player.openInventory(gui);
    }

    private void confirmPurchase(Player player) {
        if (lastClickedId == null || lastClickedId.isEmpty()) return;
        
        if (plugin.getConfig().get("items." + lastClickedId) == null) {
            player.sendMessage(Component.text("Товар уже продан или снят!", NamedTextColor.RED));
            player.closeInventory();
            return;
        }

        double price = plugin.getConfig().getDouble("items." + lastClickedId + ".price");
        String ownerName = plugin.getConfig().getString("items." + lastClickedId + ".owner");

        String balanceStr = PlaceholderAPI.setPlaceholders(player, "%vault_eco_balance%");
        balanceStr = balanceStr.replaceAll("[^0-9.]", ""); 
        
        try {
            double balance = Double.parseDouble(balanceStr);
            if (balance >= price) {
                // Списываем у покупателя
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco take " + player.getName() + " " + price);
                
                // Начисляем продавцу
                if (ownerName != null) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + ownerName + " " + price);
                    Player owner = Bukkit.getPlayer(ownerName);
                    if (owner != null && owner.isOnline()) {
                        owner.sendMessage(Component.text("Ваш лот куплен! Вы получили " + price + "$", NamedTextColor.GREEN));
                    }
                }

                // Выдача предмета
                ItemStack item = plugin.getConfig().getItemStack("items." + lastClickedId + ".item");
                if (item != null) player.getInventory().addItem(item);
                
                plugin.getConfig().set("items." + lastClickedId, null);
                plugin.saveConfig();
                
                player.sendMessage(Component.text("Покупка завершена!", NamedTextColor.GREEN));
                player.closeInventory();
            } else {
                player.sendMessage(Component.text("Недостаточно денег!", NamedTextColor.RED));
                player.closeInventory();
            }
        } catch (Exception e) {
            player.sendMessage(Component.text("Ошибка транзакции!", NamedTextColor.RED));
        }
    }
}
