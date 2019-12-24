package net.jingles.enchantments.armor.boots;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.persistence.EnchantTeam;
import net.jingles.enchantments.statuseffect.context.ItemEffectContext;
import net.jingles.enchantments.statuseffect.entity.PotionStatusEffect;
import net.jingles.enchantments.util.EnchantUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@Enchant(name = "Undying", key = "Undying", maxLevel = 3, cooldown = 1, targetItem = EnchantmentTarget.ARMOR_FEET,
    timeUnit = TimeUnit.MINUTES, description = "Undying grants the owner a second chance by providing temporary "
    + "absorption and regeneration to themselves and nearby entities for 15 seconds.")

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
    return hasEnchantment(getItem(entity)) && !Enchantments.getCooldownManager().hasCooldown(entity, this);
  }

  @EventHandler
  public void onFatalDamage(EntityDamageByEntityEvent event) {

    if (!(event.getEntity() instanceof LivingEntity) || !canTrigger((LivingEntity) event.getEntity()))
      return;

    LivingEntity entity = (LivingEntity) event.getEntity();

    if (entity.getHealth() - event.getDamage() >= 0)
      return;

    event.setCancelled(true);
    entity.setHealth(1.5);

    ItemStack item = getItem(entity);
    int level = getLevel(item);
    PotionEffect effect = new PotionEffect(PotionEffectType.REGENERATION, 300, level, false, false);
    ItemEffectContext context = new ItemEffectContext(entity, item, this);

    Enchantments.getStatusEffectManager().add(new UndyingEffect(context, effect, entity, level * 3));

  }

  private class UndyingEffect extends PotionStatusEffect {

    private final Particle.DustOptions options;
    private final PotionEffect absorption;

    private final EnchantTeam team;
    private final int radius;
    private float angle = 0F;

    private UndyingEffect(@NotNull ItemEffectContext context, @NotNull PotionEffect potionEffect, @NotNull LivingEntity target, int radius) {
      super(potionEffect, target, context, 1);
      this.team = EnchantUtils.getEnchantTeam(context.getTrigger());
      this.radius = radius;
      this.options = new Particle.DustOptions(Color.RED, 2.3F);
      this.absorption = new PotionEffect(PotionEffectType.ABSORPTION, getMaxTicks(), 1, false, false);
    }

    @Override
    public void start() {
      super.start();
      getTarget().addPotionEffect(absorption);
      addCooldown(getTarget());
    }

    @Override
    public void effect() {

      double x = radius * Math.sin(angle);
      double y = 1.25 * Math.sin(angle * 100);
      double z = radius * Math.cos(angle);
      angle += 0.1;

      Location displayLoc = getTarget().getLocation().add(x, y, z);
      getTarget().getWorld().spawnParticle(Particle.REDSTONE, displayLoc, 1, options);

      getTarget().getNearbyEntities(radius, radius, radius).stream()
        .filter(e -> e instanceof LivingEntity)
        .filter(team::isTeamed)
        .forEach(entity -> {
          LivingEntity living = (LivingEntity) entity;
          living.addPotionEffect(getPotionEffect());
          living.addPotionEffect(absorption);
        });

    }

  }

}
