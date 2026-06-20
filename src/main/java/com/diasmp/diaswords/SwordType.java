package com.diasmp.diaswords;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

/**
 * Identifies each custom sword type, the base Material used for the item,
 * and the persistent-data key used to tag/identify it.
 */
public enum SwordType {

    DASH("dash-sword", Material.NETHERITE_SWORD, "dash_sword"),
    LIGHTNING("lightning-sword", Material.NETHERITE_SWORD, "lightning_sword"),
    VAMPIRE("vampire-sword", Material.NETHERITE_SWORD, "vampire_sword"),
    FROST("frost-sword", Material.NETHERITE_SWORD, "frost_sword"),
    VORTEX("vortex-sword", Material.NETHERITE_SWORD, "vortex_sword"),
    EXPLOSIVE("explosive-sword", Material.NETHERITE_SWORD, "explosive_sword");

    private final String configKey;
    private final Material material;
    private final String tag;

    SwordType(String configKey, Material material, String tag) {
        this.configKey = configKey;
        this.material = material;
        this.tag = tag;
    }

    public String getConfigKey() {
        return configKey;
    }

    public Material getMaterial() {
        return material;
    }

    public String getTag() {
        return tag;
    }

    public NamespacedKey getKey(com.diasmp.diaswords.DiaSwords plugin) {
        return new NamespacedKey(plugin, tag);
    }

    /**
     * Match by command-friendly name, e.g. "dash", "lightning", "vampire", "frost", "vortex", "explosive".
     */
    public static SwordType fromName(String name) {
        if (name == null) return null;
        String n = name.trim().toLowerCase();
        for (SwordType type : values()) {
            if (type.name().equalsIgnoreCase(n) || type.tag.toLowerCase().startsWith(n)) {
                return type;
            }
        }
        return null;
    }
}
