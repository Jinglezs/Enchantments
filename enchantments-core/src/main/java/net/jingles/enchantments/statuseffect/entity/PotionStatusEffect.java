package net.jingles.enchantments.statuseffect.entity;

import net.jingles.enchantments.enchant.CustomEnchant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

public abstract class PotionStatusEffect extends EntityStatusEffect {

  private final PotionEffect potionEffect;

  public PotionStatusEffect(@NotNull PotionEffect potionEffect, @NotNull LivingEntity target, @NotNull CustomEnchant source, int interval) {
    super(target, source, potionEffect.getDuration(), interval);
    this.potionEffect = potionEffect;
  }

  @NotNull
  public PotionEffect getPotionEffect() {
    return this.potionEffect;
  }

  @Override
  public void start() {
    getTarget().addPotionEffect(potionEffect);
  }

  @Override
  public abstract void effect();

  @Override
  public void stop() {
    getTarget().removePotionEffect(potionEffect.getType());
    super.stop();
  }

}
