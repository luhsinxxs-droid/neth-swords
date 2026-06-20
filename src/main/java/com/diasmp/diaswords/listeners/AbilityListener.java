package com.diasmp.diaswords.listeners;

import com.diasmp.diaswords.DiaSwords;
import com.diasmp.diaswords.SwordType;
import com.diasmp.diaswords.swords.*;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.MetadataValue;

import java.util.Iterator;
import java.util.List;

/**
 * Central listener that detects when a player is holding a custom sword and
 * routes right-click activations and melee-hit passives to the right ability class.
 */
public class AbilityListener implements Listener {

    private final DiaSwords plugin;

    private final DashSword dashSword;
    private final LightningSword lightningSword;
    private final VampireSword vampireSword;
    private final FrostSword frostSword;
    private final VortexSword vortexSword;
    private final ExplosiveSword explosiveSword;

    public AbilityListener(DiaSwords plugin) {
        this.plugin = plugin;
        this.dashSword = new DashSword(plugin);
        this.lightningSword = new LightningSword(plugin);
        this.vampireSword = new VampireSword(plugin);
        this.frostSword = new FrostSword(plugin);
        this.vortexSword = new VortexSword(plugin);
        this.explosiveSword = new ExplosiveSword(plugin);
    }

    // ---------------------------------------------------------------
    // Right-click activated abilities: Dash, Frost, Vortex, Explosive
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().isRightClick()) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        SwordType type = plugin.getSwordManager().getType(item);
        if (type == null) return;

        // Only the gap-closer / AoE swords trigger on right-click.
        // Lightning and Vampire are passive on-hit and handled in onDamage().
        switch (type) {
            case DASH, FROST, VORTEX, EXPLOSIVE -> {
                if (!player.hasPermission("diaswords.use")) return;
                if (tryConsumeCooldown(player, type)) {
                    runActivation(player, type);
                }
                event.setCancelled(true);
            }
            default -> { /* no right-click action */ }
        }
    }

    private boolean tryConsumeCooldown(Player player, SwordType type) {
        String configKey = type.getConfigKey() + ".cooldown-seconds";
        long cooldown = plugin.getConfig().getLong(configKey, 8);

        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), type)) {
            long remaining = plugin.getCooldownManager().getRemainingSeconds(player.getUniqueId(), type);
            String msg = plugin.getConfig().getString("general.cooldown-message",
                    "&c{sword} is on cooldown: {seconds}s remaining");
            String swordName = plugin.getConfig().getString(type.getConfigKey() + ".display-name", type.name());
            msg = msg.replace("{sword}", swordName).replace("{seconds}", String.valueOf(remaining));
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(msg));
            return false;
        }

        plugin.getCooldownManager().setCooldown(player.getUniqueId(), type, cooldown);
        return true;
    }

    private void runActivation(Player player, SwordType type) {
        switch (type) {
            case DASH -> dashSword.activate(player);
            case FROST -> frostSword.activate(player);
            case VORTEX -> vortexSword.activate(player);
            case EXPLOSIVE -> explosiveSword.activate(player);
            default -> { }
        }
    }

    // ---------------------------------------------------------------
    // Passive on-hit abilities: Lightning, Vampire
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        ItemStack item = attacker.getInventory().getItemInMainHand();
        SwordType type = plugin.getSwordManager().getType(item);
        if (type == null) return;
        if (!attacker.hasPermission("diaswords.use")) return;

        switch (type) {
            case LIGHTNING -> lightningSword.onHit(attacker, target);
            case VAMPIRE -> vampireSword.onHit(attacker, target, event.getFinalDamage());
            default -> { }
        }
    }

    // ---------------------------------------------------------------
    // Explosive Sword fireball detonation control
    // ---------------------------------------------------------------
    // Note: entity damage/knockback from the fireball explosion is handled by
    // vanilla mechanics and is NOT affected by this listener. This only controls
    // whether terrain blocks are destroyed, so "break-blocks: false" in the config
    // keeps the sword fun and damaging without letting players grief the map.
    @EventHandler(priority = EventPriority.NORMAL)
    public void onFireballExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof Fireball fireball)) return;
        List<MetadataValue> meta = fireball.getMetadata("diaswords-explosive");
        if (meta.isEmpty()) return;

        boolean breakBlocks = meta.get(0).asBoolean();
        if (!breakBlocks) {
            event.blockList().clear();
        }
    }

    // Prevents the explosive sword's shooter from being hurt by their own blast,
    // when general.shooter-immune (per-sword) is enabled. Vanilla explosions damage
    // anyone in range including the shooter, so this is opt-in protection.
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onExplosiveSelfDamage(EntityDamageByEntityEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) return;
        if (!(event.getDamager() instanceof Fireball fireball)) return;
        if (!fireball.hasMetadata("diaswords-explosive")) return;
        if (!plugin.getConfig().getBoolean("explosive-sword.shooter-immune", true)) return;

        if (fireball.getShooter() instanceof Player shooter && shooter.equals(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    // ---------------------------------------------------------------
    // Optional general behaviors: prevent dropping / losing swords on death
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.NORMAL)
    public void onItemDrop(PlayerDropItemEvent event) {
        if (!plugin.getConfig().getBoolean("general.prevent-sword-drop", false)) return;
        if (plugin.getSwordManager().isCustomSword(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfig().getBoolean("general.prevent-sword-loss-on-death", false)) return;

        Iterator<ItemStack> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemStack drop = iterator.next();
            if (plugin.getSwordManager().isCustomSword(drop)) {
                iterator.remove();
                event.getItemsToKeep().add(drop);
            }
        }
    }
}
