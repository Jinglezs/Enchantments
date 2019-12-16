package net.jingles.enchantments.statuseffect.container;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.statuseffect.LocationStatusEffect;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class WorldEffectContainer implements EffectContainer<LocationStatusEffect> {

  public Set<? extends LocationStatusEffect> getEffectsAtLocation(@NotNull Location location) {
    return getStatusEffects().stream()
        .filter(effect -> effect.getLocation().equals(location))
        .collect(Collectors.toSet());
  }

  public boolean hasEffectAtLocation(@NotNull Location location, @NotNull Class<? extends LocationStatusEffect> effect) {
    return getEffectsAtLocation(location).stream().anyMatch(statusEffect ->  statusEffect.getClass() == effect);
  }

  public boolean hasEffectNearLocation(Location location, double radius, Class<? extends LocationStatusEffect> effect) {
    return getEffectsWithinRadius(location, radius, radius, radius).stream()
      .anyMatch(e -> e.getClass() == effect);
  }

  @NotNull
  public <T extends LocationStatusEffect> Optional<T> getEffect(@NotNull Location location, @NotNull Class<T> clazz) {
    return getEffectsAtLocation(location).stream()
        .filter(effect -> effect.getClass() == clazz)
        .map(effect -> (T) effect)
        .findFirst();
  }

  @NotNull
  public Set<? extends LocationStatusEffect> getEffectsWithinRadius(@NotNull Location center, double x, double y, double z) {
    BoundingBox box = BoundingBox.of(center, x, y, z);
    return getStatusEffects().stream()
        .filter(effect -> box.contains(effect.getLocation().toVector()))
        .collect(Collectors.toSet());
  }

  @Override
  @NotNull
  public Set<LocationStatusEffect> getStatusEffects() {
    return Enchantments.getStatusEffectManager().getStatusEffects().stream()
        .filter(effect -> effect instanceof LocationStatusEffect)
        .map(effect -> (LocationStatusEffect) effect)
        .collect(Collectors.toSet());
  }

}
