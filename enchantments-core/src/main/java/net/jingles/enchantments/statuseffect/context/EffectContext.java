package net.jingles.enchantments.statuseffect.context;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.statuseffect.PersistentEffect;
import org.bukkit.persistence.PersistentDataContainer;

public interface EffectContext {

  CustomEnchant getSource();

  PersistentDataContainer getContainer();

  void serialize(PersistentEffect effect);

  void deserialize(PersistentEffect effect);

}
