package net.jingles.enchantments.statuseffect;

import net.jingles.enchantments.enchant.CustomEnchant;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public abstract class LocationStatusEffect extends StatusEffect {

  private Location location;

  public LocationStatusEffect(@NotNull CustomEnchant source, int maxTicks, int interval, @NotNull Location location) {
    super(source, maxTicks, interval);
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
