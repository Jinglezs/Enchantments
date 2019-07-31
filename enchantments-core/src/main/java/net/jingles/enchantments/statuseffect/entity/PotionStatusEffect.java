package net.jingles.enchantments.statuseffect.entity;

import net.jingles.enchantments.enchant.CustomEnchant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

public abstract class PotionStatusEffect extends EntityStatusEffect {

  private final PotionEffect potionEffect;

  public PotionStatusEffect(PotionEffect potionEffect, LivingEntity target, CustomEnchant source, int interval) {
    super(target, source, potionEffect.getDuration(), interval);
    this.potionEffect = potionEffect;
  }

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
