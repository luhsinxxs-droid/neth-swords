package com.diasmp.diaswords.swords;

import com.diasmp.diaswords.DiaSwords;
import com.diasmp.diaswords.utils.ParticleUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;

/**
 * Vortex Sword: right-click to pull all nearby enemies toward the caster.
 * Great combo-starter and counter to enemies trying to flee.
 */
public class VortexSword {

    private final DiaSwords plugin;

    public VortexSword(DiaSwords plugin) {
        this.plugin = plugin;
    }

    public void activate(Player player) {
        double radius = plugin.getConfig().getDouble("vortex-sword.radius", 6.0);
        double pullStrength = plugin.getConfig().getDouble("vortex-sword.pull-strength", 0.9);
        boolean affectSelf = plugin.getConfig().getBoolean("vortex-sword.affect-self", false);
        boolean spiral = plugin.getConfig().getBoolean("vortex-sword.particle-spiral", true);

        Location origin = player.getLocation();
        ParticleUtils.vortexPulse(origin);
        player.getWorld().playSound(origin, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.8f);

        Collection<LivingEntity> nearby = player.getWorld().getNearbyLivingEntities(origin, radius);
        for (LivingEntity entity : nearby) {
            if (entity.equals(player) && !affectSelf) continue;

            Location targetLoc = entity.getLocation();
            Vector pull = origin.toVector().subtract(targetLoc.toVector());
            double distance = pull.length();
            if (distance < 0.5) continue;

            pull.normalize().multiply(pullStrength);
            pull.setY(Math.max(pull.getY(), 0.1)); // small lift so they don't get stuck in terrain
            entity.setVelocity(entity.getVelocity().add(pull));

            if (spiral) {
                ParticleUtils.vortexSpiral(targetLoc, origin);
            }
        }
    }
}
