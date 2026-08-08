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

import java.util.ArrayList;
import java.util.List;

public final class VenturaRTP extends JavaPlugin implements Listener {

    private final MiniMessage mm = MiniMessage.miniMessage();
    private NamespacedKey destinationKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        destinationKey = new NamespacedKey(this, "destination");
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("VenturaRTP enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("VenturaRTP disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("rtp")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        openMenu(player);
        return true;
    }

    private void openMenu(Player player) {
        int rows = Math.max(1, Math.min(6, getConfig().getInt("menu.rows", 3)));
        Component title = mm.deserialize(getConfig().getString("menu.title", "<dark_gray>ʀᴀɴᴅᴏᴍ ᴛᴇʟᴇᴘᴏʀᴛ"));
        Inventory inventory = Bukkit.createInventory(null, rows * 9, title);

        ConfigurationSection buttons = getConfig().getConfigurationSection("buttons");
        if (buttons != null) {
            for (String key : buttons.getKeys(false)) {
                ConfigurationSection section = buttons.getConfigurationSection(key);
                if (section == null) continue;

                int slot = section.getInt("slot", -1);
                if (slot < 0 || slot >= inventory.getSize()) continue;

                inventory.setItem(slot, createButton(key, section));
            }
        }

        player.openInventory(inventory);
    }

    private ItemStack createButton(String destination, ConfigurationSection section) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        float customModelData = (float) section.getDouble("custom-model-data", 0.0D);
        CustomModelDataComponent modelData = meta.getCustomModelDataComponent();
        modelData.setFloats(List.of(customModelData));
        meta.setCustomModelDataComponent(modelData);

        String name = section.getString("name", "<white>" + destination);
        meta.displayName(mm.deserialize(name));

        List<Component> lore = new ArrayList<>();
        for (String line : section.getStringList("lore")) {
            lore.add(mm.deserialize(line));
        }
        meta.lore(lore);

        meta.getPersistentDataContainer().set(destinationKey, PersistentDataType.STRING, destination);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) return;
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        String destination = meta.getPersistentDataContainer().get(destinationKey, PersistentDataType.STRING);
        if (destination == null) return;

        event.setCancelled(true);
        player.closeInventory();

        String command = getConfig().getString("buttons." + destination + ".command", "");
        if (command.isBlank()) {
            player.sendMessage(mm.deserialize("<red>Für dieses Ziel ist kein RTP-Befehl konfiguriert.</red>"));
            return;
        }

        String resolved = command.replace("%player%", player.getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolved);
    }
}
