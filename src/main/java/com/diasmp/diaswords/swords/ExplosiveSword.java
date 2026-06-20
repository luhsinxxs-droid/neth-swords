package com.diasmp.diaswords.swords;

import com.diasmp.diaswords.DiaSwords;
import com.diasmp.diaswords.utils.ParticleUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/**
 * Explosive Sword: right-click to launch a Fireball projectile in the direction
 * the player is looking. On impact it explodes, dealing AoE damage/knockback.
 * Block breaking and fire-spread are both configurable and default to safe/off
 * so it can't grief the map.
 */
public class ExplosiveSword {

    private final DiaSwords plugin;

    public ExplosiveSword(DiaSwords plugin) {
        this.plugin = plugin;
    }

    public void activate(Player player) {
        double speed = plugin.getConfig().getDouble("explosive-sword.fireball-speed", 1.4);
        float power = (float) plugin.getConfig().getDouble("explosive-sword.explosion-power", 1.6);
        boolean setFire = plugin.getConfig().getBoolean("explosive-sword.set-fire", false);
        boolean breakBlocks = plugin.getConfig().getBoolean("explosive-sword.break-blocks", false);

        Vector direction = player.getEyeLocation().getDirection().normalize();
        Fireball fireball = player.launchProjectile(Fireball.class, direction.multiply(speed));
        fireball.setShooter(player);
        fireball.setYield(power);
        fireball.setIsIncendiary(setFire);
        // isIncendiary controls fire on impact; block damage is controlled separately below.

        // Tag so we can identify this fireball as a DiaSwords explosive in the listener
        // (used to decide whether to cancel block damage on detonation).
        fireball.setMetadata("diaswords-explosive",
                new org.bukkit.metadata.FixedMetadataValue(plugin, breakBlocks));

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);

        // Trail of particles following the fireball until it dies or detonates.
        BukkitTask[] taskHolder = new BukkitTask[1];
        taskHolder[0] = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!fireball.isValid() || fireball.isDead()) {
                taskHolder[0].cancel();
                return;
            }
            ParticleUtils.fireballTrail(fireball.getLocation());
        }, 0L, 1L);
    }
}
