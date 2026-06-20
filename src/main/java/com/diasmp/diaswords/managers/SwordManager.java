package com.diasmp.diaswords.managers;

import com.diasmp.diaswords.DiaSwords;
import com.diasmp.diaswords.SwordType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds and identifies custom ability sword ItemStacks.
 * Sword identity is tagged via PersistentDataContainer so it survives
 * renames, enchants, and inventory moves.
 */
public class SwordManager {

    private final DiaSwords plugin;

    public SwordManager(DiaSwords plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a fresh custom sword ItemStack for the given type, reading its
     * display name from config.
     */
    public ItemStack createSword(SwordType type) {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();

        String rawName = plugin.getConfig().getString(type.getConfigKey() + ".display-name", type.name());
        Component name = LegacyComponentSerializer.legacyAmpersand().deserialize(rawName)
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false);
        meta.displayName(name);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Right-click or hit to trigger ability.", NamedTextColor.GRAY)
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(type.getKey(plugin), PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * @return the SwordType tagged on this item, or null if it's not a DiaSwords item.
     */
    public SwordType getType(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        for (SwordType type : SwordType.values()) {
            if (meta.getPersistentDataContainer().has(type.getKey(plugin), PersistentDataType.BYTE)) {
                return type;
            }
        }
        return null;
    }

    public boolean isCustomSword(ItemStack item) {
        return getType(item) != null;
    }
}
