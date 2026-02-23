package me.blockshop.auction;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AuctionListener implements Listener {
    private final AuctionPlugin plugin;

    public AuctionListener(AuctionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(Component.text("Аукцион BlockShop"))) return;
        
        event.setCancelled(true); // Запрещаем перетаскивать иконки
        
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        // Логика кнопки "Обновить"
        if (event.getRawSlot() == 5) {
            player.performCommand("ah");
            return;
        }

        // Логика покупки предмета (упрощенная)
        if (event.getRawSlot() >= 10 && event.getRawSlot() <= 43) {
            // В реальном плагине здесь должна быть проверка денег через Vault
            // и удаление записи из config.yml
            player.getInventory().addItem(clickedItem); 
            player.sendMessage(Component.text("Вы купили предмет!", NamedTextColor.GREEN));
            player.closeInventory();
        }
    }
}
