package net.jingles.enchantments.statuseffect.container;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.statuseffect.LocationStatusEffect;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class WorldEffectContainer implements EffectContainer<LocationStatusEffect> {

  public Set<? extends LocationStatusEffect> getEffectsAtLocation(Location location) {
    return getStatusEffects().stream()
        .filter(effect -> effect.getLocation().equals(location))
        .collect(Collectors.toSet());
  }

  public boolean hasEffectAtLocation(Location location, Class<? extends LocationStatusEffect> effect) {
    return getEffectsAtLocation(location).stream().anyMatch(statusEffect ->  statusEffect.getClass() == effect);
  }

  public <T extends LocationStatusEffect> Optional<T> getEffect(Location location, Class<T> clazz) {
    return getEffectsAtLocation(location).stream()
        .filter(effect -> effect.getClass() == clazz)
        .map(effect -> (T) effect)
        .findFirst();
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
