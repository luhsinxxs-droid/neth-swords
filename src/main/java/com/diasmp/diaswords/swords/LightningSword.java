package com.diasmp.diaswords.swords;

import com.diasmp.diaswords.DiaSwords;
import com.diasmp.diaswords.utils.ParticleUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Lightning Sword: passive effect that procs on melee hit.
 * Has a configurable chance to strike the target with bonus damage
 * and (optionally) a visual lightning bolt.
 */
public class LightningSword {

    private final DiaSwords plugin;

    public LightningSword(DiaSwords plugin) {
        this.plugin = plugin;
    }

    /**
     * Called on every melee hit landed with this sword. Rolls the proc chance
     * and applies bonus damage / visuals if it procs.
     */
    public void onHit(Player attacker, LivingEntity target) {
        double chance = plugin.getConfig().getDouble("lightning-sword.proc-chance", 0.10);
        if (ThreadLocalRandom.current().nextDouble() > chance) return;

        double bonusDamage = plugin.getConfig().getDouble("lightning-sword.bonus-damage", 4.0);
        boolean visualStrike = plugin.getConfig().getBoolean("lightning-sword.visual-strike", true);
        boolean setFire = plugin.getConfig().getBoolean("lightning-sword.damage-fire", false);

        Location loc = target.getLocation();

        if (visualStrike) {
            // strikeLightningEffect deals no extra damage itself — just the visual/sound,
            // since we apply bonusDamage ourselves for precise config control.
            target.getWorld().strikeLightningEffect(loc);
        } else {
            ParticleUtils.lightningSparks(loc.clone().add(0, 1, 0));
            target.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.4f);
        }

        if (bonusDamage > 0) {
            target.damage(bonusDamage, attacker);
        }

        if (setFire) {
            target.setFireTicks(60);
        }
    }
}
