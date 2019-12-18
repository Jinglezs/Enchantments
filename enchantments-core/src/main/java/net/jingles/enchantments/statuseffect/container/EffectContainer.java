package net.jingles.enchantments.statuseffect.container;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.statuseffect.StatusEffect;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface EffectContainer<T extends StatusEffect> {

  /**
   * Gets all active status effects
   *
   * @return a set of the effects
   */
  Set<T> getStatusEffects();

  /**
   * Gets whether or not the entity has an effect of the given class.
   *
   * @param effect the class of the effect in question
   * @param <U>    the effect
   * @return true if entity has an effect of the given type.
   */
  default <U extends StatusEffect> boolean hasEffect(Class<U> effect) {
    return getStatusEffects().stream().anyMatch(statusEffect -> statusEffect.getClass() == effect);
  }

  /**
   * Gets a set of all effects with the given source.
   *
   * @param source the enchantment
   * @return status effects created by the enchantment
   */
  default Set<T> getEffectsBySource(CustomEnchant source) {
    return getStatusEffects().stream()
        .filter(effect -> effect.getSource().equals(source))
        .collect(Collectors.toSet());
  }

  default <U extends StatusEffect> Optional<U> getEffectBySource(CustomEnchant source, Class<U> effect) {
    return getEffectsBySource(source).stream()
      .filter(e -> e.getClass() == effect)
      .map(e -> (U) e)
      .findFirst();
  }

  /**
   * Removes and stops all effects created by the given source.
   *
   * @param source the enchantment
   */
  default void removeEffects(CustomEnchant source) {
    getStatusEffects().stream()
        .filter(statusEffect -> statusEffect.getSource().equals(source))
        .forEach(StatusEffect::stop);
  }

}
