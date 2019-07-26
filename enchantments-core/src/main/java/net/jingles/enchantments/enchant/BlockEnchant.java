package net.jingles.enchantments.enchant;

import net.jingles.enchantments.Enchantments;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BlockEnchant extends CustomEnchant {

  public BlockEnchant(NamespacedKey key) {
    super(key);
  }

  public static Map<BlockEnchant, Integer> getBlockEnchants(ItemStack item) {
    if (item.getItemMeta() == null || item.getItemMeta().getEnchants().isEmpty())
      return Collections.emptyMap();

    return item.getItemMeta().getEnchants().entrySet().stream()
        .filter(entry -> entry.getKey() instanceof BlockEnchant)
        .collect(Collectors.toMap(entry -> (BlockEnchant) entry.getKey(), Map.Entry::getValue));
  }

  /**
   * Gets all BlockEnchants that are saved to a Container Block's PDC.
   * @param container the data container to search through
   * @return a map where the enchantment is the key and the enchantment level is the value.
   */
  public static Map<BlockEnchant, Integer> getBlockEnchants(PersistentDataContainer container) {
    return Enchantments.getEnchantmentManager().getRegisteredBlockEnchants().stream()
        .filter(enchant -> container.has(enchant.getKey(), PersistentDataType.INTEGER))
        .collect(Collectors.toMap(enchant -> enchant,
            enchant -> container.get(enchant.getKey(), PersistentDataType.INTEGER)));
  }

  public boolean hasEnchant(Container container) {
    return container.getPersistentDataContainer().has(getKey(), PersistentDataType.INTEGER);
  }

}
