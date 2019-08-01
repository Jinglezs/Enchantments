package net.jingles.enchantments.statuseffect.container;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.statuseffect.entity.EntityStatusEffect;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class EntityEffectContainer implements EffectContainer<EntityStatusEffect> {

  private final UUID owner;

  public EntityEffectContainer(@NotNull UUID owner) {
    this.owner = owner;
  }

  /**
   * Gets an instance of the container's owner.
   * @return the Entity instance.
   */
  @Nullable
  public Entity getEntity() {
    return Bukkit.getEntity(owner);
  }

  /**
   * Gets the unique id of the owner.
   * @return the id
   */
  @NotNull
  public UUID getOwner() {
    return this.owner;
  }

  @Override
  @NotNull
  public Set<EntityStatusEffect> getStatusEffects() {
    return Enchantments.getStatusEffectManager().getStatusEffects().stream()
        .filter(effect -> effect instanceof EntityStatusEffect)
        .map(effect -> (EntityStatusEffect) effect)
        .filter(effect -> effect.getTargetId().equals(owner))
        .collect(Collectors.toSet());
  }
}
