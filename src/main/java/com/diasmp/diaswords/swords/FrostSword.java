package com.diasmp.diaswords.swords;

import com.diasmp.diaswords.DiaSwords;
import com.diasmp.diaswords.utils.ParticleUtils;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

/**
 * Frost Sword: right-click to apply Slowness + Mining Fatigue ("freeze") to all
 * enemies within a configurable radius. Good counter to mobility swords like Dash.
 */
public class FrostSword {

    private final DiaSwords plugin;

    public FrostSword(DiaSwords plugin) {
        this.plugin = plugin;
    }

    public void activate(Player player) {
        double radius = plugin.getConfig().getDouble("frost-sword.radius", 4.0);
        int duration = plugin.getConfig().getInt("frost-sword.freeze-duration-seconds", 3) * 20;
        int slowAmp = plugin.getConfig().getInt("frost-sword.slowness-amplifier", 3);
        int fatigueAmp = plugin.getConfig().getInt("frost-sword.mining-fatigue-amplifier", 2);
        boolean affectSelf = plugin.getConfig().getBoolean("frost-sword.affect-self", false);
        boolean particleBurst = plugin.getConfig().getBoolean("frost-sword.particle-burst", true);

        if (particleBurst) {
            ParticleUtils.frostBurst(player.getLocation(), radius);
        }
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.8f, 0.7f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, 0.6f, 1.5f);

        Collection<LivingEntity> nearby = player.getWorld().getNearbyLivingEntities(player.getLocation(), radius);
        for (LivingEntity entity : nearby) {
            if (entity.equals(player) && !affectSelf) continue;
            if (!(entity instanceof Player) && !affectSelf) {
                // still allow affecting mobs in FFA if desired; default behavior targets all living entities
            }
            entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, slowAmp, false, true, true));
            entity.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, duration, fatigueAmp, false, true, true));
            ParticleUtils.frostFreeze(entity.getLocation().add(0, 1, 0));
        }
    }
}
