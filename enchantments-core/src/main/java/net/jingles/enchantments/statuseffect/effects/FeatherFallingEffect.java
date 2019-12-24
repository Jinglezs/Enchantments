package net.jingles.enchantments.statuseffect.effects;

import net.jingles.enchantments.statuseffect.context.EffectContext;
import net.jingles.enchantments.statuseffect.entity.EntityStatusEffect;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class FeatherFallingEffect extends EntityStatusEffect {

  public FeatherFallingEffect(@NotNull LivingEntity target, EffectContext context, int maxTicks) {
    super(target, context, maxTicks, 1);
  }

  @Override
  public void effect() {
    getTarget().setFallDistance(0F);
    if (getTarget().isOnGround()) this.stop();
  }

}
