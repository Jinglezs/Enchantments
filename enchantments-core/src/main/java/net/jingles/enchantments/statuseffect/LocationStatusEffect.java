package net.jingles.enchantments.statuseffect;

import net.jingles.enchantments.statuseffect.context.EffectContext;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public abstract class LocationStatusEffect extends StatusEffect {

  private Location location;

  public LocationStatusEffect(EffectContext context, int maxTicks, int interval, @NotNull Location location) {
    super(context, maxTicks, interval);
    this.location = location;
  }

  @NotNull
  public Location getLocation() {
    return this.location;
  }

  public void setLocation(@NotNull Location location) {
    this.location = location;
  }

  public abstract void effect();

}
