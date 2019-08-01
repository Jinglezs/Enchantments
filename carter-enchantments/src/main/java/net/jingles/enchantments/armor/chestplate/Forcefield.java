package net.jingles.enchantments.armor.chestplate;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.projectile.ProjectileManager;
import net.jingles.enchantments.statuseffect.entity.EntityStatusEffect;
import net.jingles.enchantments.util.ParticleUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

@Enchant(name = "Forcefield", key = "forcefield", maxLevel = 3, targetItem = EnchantmentTarget.ARMOR_TORSO,
    levelRequirement = 13, description = "When a projectile nears the owner, a forcefield will surround them " +
    "for up to 15 seconds that deflects incoming projectiles (custom and vanilla).")

public class Forcefield extends CustomEnchant {

  public Forcefield(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(LivingEntity entity) {
    return hasEnchantment(getItem(entity)) && !Enchantments.getCooldownManager()
        .hasCooldown(entity, this);
  }

  @EventHandler
  public void onProjectileHit(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Projectile) || !(event.getEntity() instanceof LivingEntity)) return;

    LivingEntity hit = (LivingEntity) event.getEntity();
    if (!canTrigger(hit)) return;

    // Cancel the event so that the hit is not registered.
    event.setCancelled(true);

    // 5 seconds per level
    int duration = getLevel(getItem(hit)) * (5 * 20);
    Enchantments.getStatusEffectManager().add(new ForcefieldEffect(hit, duration));

  }

  private class ForcefieldEffect extends EntityStatusEffect {

    private final Particle.DustOptions options = new Particle.DustOptions(Color.GRAY, 2);

    private ForcefieldEffect(LivingEntity target, int maxTicks) {
      super(target, Forcefield.this, maxTicks, 5);
    }

    @Override
    public void effect() {

      Location location = getTarget().getLocation();
      ParticleUtil.sphere(location, 3, Particle.REDSTONE, options);

      // Vanilla projectiles

      getTarget().getNearbyEntities(3, 3, 3).stream()
          .filter(entity -> entity instanceof Projectile)
          .map(entity -> (Projectile) entity)
          .forEach(projectile -> projectile.setVelocity(projectile.getVelocity().multiply(-1)));

      // Custom projectiles

      ProjectileManager manager = Enchantments.getProjectileManager();
      manager.getNearbyProjectiles(location, 3)
          .forEach(projectile -> manager.bounceProjectileFromSource(projectile, getTarget().getLocation()));

    }

  }

}
