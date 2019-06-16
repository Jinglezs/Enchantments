package net.jingles.enchantments.statuseffect.container;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.statuseffect.LocationStatusEffect;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.util.Set;
import java.util.stream.Collectors;

public class WorldEffectContainer implements EffectContainer<LocationStatusEffect> {

  public Set<? extends LocationStatusEffect> getEffectsAtLocation(Location location) {
    return getStatusEffects().stream()
        .filter(effect -> effect.getLocation().equals(location))
        .collect(Collectors.toSet());
  }

  public Set<? extends LocationStatusEffect> getEffectsWithinRadius(Location center, double x, double y, double z) {
    BoundingBox box = BoundingBox.of(center, x, y, z);
    return getStatusEffects().stream()
        .filter(effect -> box.contains(effect.getLocation().toVector()))
        .collect(Collectors.toSet());
  }

  @Override
  public Set<LocationStatusEffect> getStatusEffects() {
    return Enchantments.getStatusEffectManager().getStatusEffects().stream()
        .filter(effect -> effect instanceof LocationStatusEffect)
        .map(effect -> (LocationStatusEffect) effect)
        .collect(Collectors.toSet());
  }

}
