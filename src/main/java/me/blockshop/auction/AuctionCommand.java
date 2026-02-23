package me.blockshop.auction;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuctionCommand implements CommandExecutor {
    private final AuctionPlugin plugin;

    public AuctionCommand(AuctionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length > 0 && args[0].equalsIgnoreCase("sell")) {
            handleSell(player, args);
            return true;
        }

        openMainGui(player);
        return true;
    }

    public void openMainGui(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("Аукцион BlockShop", NamedTextColor.DARK_GRAY));
        
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fMeta = filler.getItemMeta();
        if (fMeta != null) {
            fMeta.displayName(Component.text(" "));
            filler.setItemMeta(fMeta);
        }

        int[] borders = {0,1,2,3,4,5,6,7,8, 9,17, 18,26, 27,35, 36,44, 45,46,47,48,49,50,51,52,53};
        for (int i : borders) inv.setItem(i, filler);

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("items");
        if (section != null) {
            int slot = 10;
            for (String key : section.getKeys(false)) {
                if (slot > 43) break;
                if (slot % 9 == 0 || slot % 9 == 8) slot++;

                ItemStack item = plugin.getConfig().getItemStack("items." + key + ".item");
                double price = plugin.getConfig().getDouble("items." + key + ".price");
                String owner = plugin.getConfig().getString("items." + key + ".owner", "Неизвестно");
                
                if (item != null) {
                    ItemStack display = item.clone();
                    ItemMeta dMeta = display.getItemMeta();
                    if (dMeta != null) {
                        List<Component> lore = dMeta.hasLore() ? dMeta.lore() : new ArrayList<>();
                        if (lore == null) lore = new ArrayList<>();
                        lore.add(Component.text(" "));
                        lore.add(Component.text("Цена: ", NamedTextColor.GRAY).append(Component.text(price + "$", NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("Продавец: ", NamedTextColor.GRAY).append(Component.text(owner, NamedTextColor.YELLOW)).decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("Нажмите для покупки", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                        dMeta.lore(lore);
                        display.setItemMeta(dMeta);
                    }
                    inv.setItem(slot, display);
                    slot++;
                }
            }
        }
        player.openInventory(inv);
    }

    private void handleSell(Player player, String[] args) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(Component.text("Возьмите предмет в руку!", NamedTextColor.RED));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Component.text("Использование: /ah sell <цена>", NamedTextColor.RED));
            return;
        }

        try {
            double price = Double.parseDouble(args[1]);
            if (price < 0) {
                player.sendMessage(Component.text("Цена не может быть отрицательной!", NamedTextColor.RED));
                return;
            }
            String id = UUID.randomUUID().toString();
            plugin.getConfig().set("items." + id + ".item", item);
            plugin.getConfig().set("items." + id + ".price", price);
            plugin.getConfig().set("items." + id + ".owner", player.getName());
            plugin.saveConfig();

            player.getInventory().setItemInMainHand(null);
            player.sendMessage(Component.text("Предмет выставлен за " + price + "$", NamedTextColor.GREEN));
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Ошибка: укажите число!", NamedTextColor.RED));
        }
    }
}
