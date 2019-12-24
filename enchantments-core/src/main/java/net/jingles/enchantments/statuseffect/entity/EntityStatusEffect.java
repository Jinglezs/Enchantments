package net.jingles.enchantments.statuseffect.entity;

import net.jingles.enchantments.statuseffect.StatusEffect;
import net.jingles.enchantments.statuseffect.context.EffectContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class EntityStatusEffect extends StatusEffect {

  private final LivingEntity target;

  public EntityStatusEffect(LivingEntity target, EffectContext context, int maxTicks, int interval) {
    super(context, maxTicks, interval);
    this.target = target;
  }

  @NotNull
  public LivingEntity getTarget() {
    return this.target;
  }

  @NotNull
  public UUID getTargetId() {
    return target.getUniqueId();
  }

  public void heal(double amount) {
    double maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    double newHealth = target.getHealth() + amount;

    if (newHealth > maxHealth) target.setHealth(maxHealth);
    else target.setHealth(newHealth);
  }

  public abstract void effect();

}
