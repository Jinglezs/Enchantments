package net.jingles.enchantments.statuseffect;

import net.jingles.enchantments.enchant.CustomEnchant;
import org.bukkit.Location;

public abstract class LocationStatusEffect extends StatusEffect {

  private Location location;

  public LocationStatusEffect(CustomEnchant source, int maxTicks, int interval, Location location) {
    super(source, maxTicks, interval);
    this.location = location;
  }

  public Location getLocation() {
    return this.location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public abstract void effect();

}
