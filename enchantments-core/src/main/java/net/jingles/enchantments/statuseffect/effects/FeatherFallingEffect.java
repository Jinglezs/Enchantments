package net.jingles.enchantments.statuseffect.effects;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.statuseffect.EntityStatusEffect;
import org.bukkit.entity.LivingEntity;

public class FeatherFallingEffect extends EntityStatusEffect {

  public FeatherFallingEffect(LivingEntity target, CustomEnchant source, int maxTicks) {
    super(target, source, maxTicks, 1);
  }

  @Override
  public void effect() {
    if (getTarget().isOnGround()) this.stop();
    getTarget().setFallDistance(0F);
  }

}
