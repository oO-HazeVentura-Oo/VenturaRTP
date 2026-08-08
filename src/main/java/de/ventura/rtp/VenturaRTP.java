package de.ventura.rtp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class VenturaRTP extends JavaPlugin implements Listener {

    private final MiniMessage mm = MiniMessage.miniMessage();
    private NamespacedKey destinationKey;
    private NamespacedKey menuKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        destinationKey = new NamespacedKey(this, "destination");
        menuKey = new NamespacedKey(this, "rtp_menu");
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("VenturaRTP v1.2.0 enabled - 3x3 tile GUI.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("rtp")) return false;
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        openMenu(player);
        return true;
    }

    private void openMenu(Player player) {
        Component title = mm.deserialize(getConfig().getString(
                "menu.title", "<dark_gray>ʀᴀɴᴅᴏᴍ ᴛᴇʟᴇᴘᴏʀᴛ"));

        Inventory inv = Bukkit.createInventory(null, 27, title);

        placeDestination(inv, "overworld", 0);
        placeDestination(inv, "nether", 3);
        placeDestination(inv, "end", 6);

        player.openInventory(inv);
    }

    private void placeDestination(Inventory inv, String destination, int startColumn) {
        ConfigurationSection section =
                getConfig().getConfigurationSection("destinations." + destination);
        if (section == null) return;

        int modelStart = section.getInt("custom-model-data-start");
        String displayName = section.getString("name", "<white>" + destination);

        int tile = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slot = row * 9 + startColumn + col;
                int modelData = modelStart + tile;
                inv.setItem(slot, createTile(destination, displayName, modelData));
                tile++;
            }
        }
    }

    private ItemStack createTile(String destination, String displayName, int modelDataValue) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        cmd.setFloats(List.of((float) modelDataValue));
        meta.setCustomModelDataComponent(cmd);

        meta.displayName(mm.deserialize(displayName));

        meta.getPersistentDataContainer()
                .set(destinationKey, PersistentDataType.STRING, destination);
        meta.getPersistentDataContainer()
                .set(menuKey, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        Byte isMenuTile = meta.getPersistentDataContainer()
                .get(menuKey, PersistentDataType.BYTE);
        if (isMenuTile == null || isMenuTile != (byte) 1) return;

        event.setCancelled(true);

        String destination = meta.getPersistentDataContainer()
                .get(destinationKey, PersistentDataType.STRING);
        if (destination == null) return;

        player.closeInventory();

        String cmd = getConfig().getString(
                "destinations." + destination + ".command", "");
        if (cmd.isBlank()) return;

        Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                cmd.replace("%player%", player.getName())
        );
    }
}
