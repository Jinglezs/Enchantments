package net.jingles.enchantments.statuseffect;

import net.jingles.enchantments.enchant.CustomEnchant;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public abstract class EntityStatusEffect extends StatusEffect {

  private final LivingEntity target;

  public EntityStatusEffect(LivingEntity target, CustomEnchant source, int maxTicks, int interval) {
    super(source, maxTicks, interval);
    this.target = target;
  }

  public LivingEntity getTarget() {
    return this.target;
  }

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
