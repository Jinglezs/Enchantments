package net.jingles.enchantments.statuseffect;

import org.bukkit.persistence.PersistentDataContainer;

public interface PersistentEffect {

  void serialize(PersistentDataContainer container);

  void deserialize(PersistentDataContainer container);

}
