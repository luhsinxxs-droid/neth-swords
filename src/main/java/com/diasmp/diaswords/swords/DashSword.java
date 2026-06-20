package com.diasmp.diaswords.swords;

import com.diasmp.diaswords.DiaSwords;
import com.diasmp.diaswords.SwordType;
import com.diasmp.diaswords.utils.ParticleUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Dash Sword: right-click to dash forward in the direction the player is looking.
 * Pure mobility tool — good for engaging or disengaging in FFA fights.
 */
public class DashSword {

    private final DiaSwords plugin;

    public DashSword(DiaSwords plugin) {
        this.plugin = plugin;
    }

    public void activate(Player player) {
        double strength = plugin.getConfig().getDouble("dash-sword.dash-strength", 1.8);
        double upBoost = plugin.getConfig().getDouble("dash-sword.dash-upward-boost", 0.15);
        boolean trail = plugin.getConfig().getBoolean("dash-sword.particle-trail", true);
        double immunitySeconds = plugin.getConfig().getDouble("dash-sword.fall-damage-immunity-seconds", 1.0);

        Vector direction = player.getLocation().getDirection().normalize();
        Vector velocity = direction.multiply(strength).setY(direction.getY() * strength + upBoost);
        player.setVelocity(velocity);

        ParticleUtils.dashBurst(player.getLocation());
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.6f, 1.6f);

        if (immunitySeconds > 0) {
            player.setFallDistance(0f);
            int ticks = (int) (immunitySeconds * 20);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) player.setFallDistance(0f);
            }, ticks);
        }

        if (trail) {
            new Runnable() {
                int count = 0;
                @Override
                public void run() {
                    if (count >= 6 || !player.isOnline()) return;
                    ParticleUtils.dashTrail(player.getLocation());
                    count++;
                    Bukkit.getScheduler().runTaskLater(plugin, this, 1L);
                }
            }.run();
        }
    }

    public SwordType type() {
        return SwordType.DASH;
    }
}
