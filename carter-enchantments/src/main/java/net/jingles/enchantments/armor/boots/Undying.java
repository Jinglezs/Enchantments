package net.jingles.enchantments.armor.boots;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.statuseffect.entity.PotionStatusEffect;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@Enchant(name = "Undying", key = "Undying", maxLevel = 3, cooldown = 1, targetItem = EnchantmentTarget.ARMOR_FEET,
    timeUnit = TimeUnit.MINUTES, description = "Undying grants the owner a second chance by providing temporary " +
    "absorption and regeneration to themselves and nearby entities for 15 seconds.")

public class Undying extends CustomEnchant {

  public Undying(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(@NotNull LivingEntity entity) {
    return hasEnchantment(getItem(entity)) &&
        !Enchantments.getCooldownManager().hasCooldown(entity, this);
  }

  @EventHandler
  public void onFatalDamage(EntityDamageByEntityEvent event) {

    if (!(event.getEntity() instanceof LivingEntity) ||
        !canTrigger((LivingEntity) event.getEntity())) return;

    LivingEntity entity = (LivingEntity) event.getEntity();

    if (entity.getHealth() - event.getDamage() >= 0) return;

    event.setCancelled(true);
    entity.setHealth(1.5);

    int level = getLevel(getItem(entity));
    PotionEffect effect = new PotionEffect(PotionEffectType.REGENERATION, 300, level, false, false);
    Enchantments.getStatusEffectManager().add(new UndyingEffect(effect, entity, level * 3, level));

  }

  private class UndyingEffect extends PotionStatusEffect {

    private final PotionEffect regen;
    private final int radius;
    private float angle = 0F;

    private UndyingEffect(@NotNull PotionEffect potionEffect, @NotNull LivingEntity target, int radius, int level) {
      super(potionEffect, target, Undying.this, 2);
      this.radius = radius;
      this.regen = new PotionEffect(PotionEffectType.REGENERATION, 60, level, false, false);
    }

    @Override
    public void start() {
      getTarget().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, getMaxTicks(), 2, false, false));
      addCooldown(getTarget());
    }

    @Override
    public void effect() {

      double x = radius * Math.sin(angle);
      double y = 2 * Math.sin(angle / 3);
      double z = radius * Math.cos(angle);
      angle += 0.5;

      Location displayLoc = getTarget().getLocation().add(x, y, z);
      getTarget().getWorld().spawnParticle(Particle.HEART, displayLoc, 1);

      getTarget().getNearbyEntities(radius, radius, radius).stream()
          .filter(entity -> entity instanceof LivingEntity)
          .map(entity -> (LivingEntity) entity)
          .forEach(entity -> entity.addPotionEffect(regen));

    }

  }

}
