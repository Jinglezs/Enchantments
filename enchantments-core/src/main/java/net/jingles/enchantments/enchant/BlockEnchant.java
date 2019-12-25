package net.jingles.enchantments.enchant;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.persistence.DataType;
import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class BlockEnchant extends CustomEnchant {

  public BlockEnchant(NamespacedKey key) {
    super(key);
  }

  public abstract boolean canTrigger(@Nullable TileState tile);

  /**
   * This method can be overrode to determine how the enchanted block
   * behaves when it is loaded with a chunk. This allows for continuous
   * effects to be stopped and restarted when the chunk unloads and loads again.
   *
   * @param tile the tile entity with the enchantment
   */
  public void onChunkLoad(TileState tile) {
  }

  /**
   * Allows the developer to determine how unloading the enchanted
   * block's chunk affects its behavior.
   *
   * The default behavior is to stop all location status effects
   * at the tile entity's location.
   *
   * @param tile the tile entity with the enchantment
   */
  public void onChunkUnload(TileState tile) {
  }

  @Override
  public boolean canTrigger(@NotNull LivingEntity entity) {
    return false;
  }

  public int getLevel(@Nullable TileState tile) {
      return !hasEnchant(tile) ? 0 :
        tile.getPersistentDataContainer().get(getKey(), PersistentDataType.INTEGER);
  }

  public UUID getOwner(TileState tile) {
    return tile.getPersistentDataContainer().get(Enchantments.OWNER_KEY, DataType.UUID);
  }

  @NotNull
  public static Map<BlockEnchant, Integer> getBlockEnchants(@NotNull ItemStack item) {
    if (item.getItemMeta() == null || item.getItemMeta().getEnchants().isEmpty())
      return Collections.emptyMap();

    return item.getEnchantments().entrySet().stream()
        .filter(entry -> BlockEnchant.isBlockEnchant(entry.getKey()))
        .collect(Collectors.toMap(entry -> getBlockEnchant(entry.getKey().getKey()), Map.Entry::getValue));
  }

  /**
   * Gets all BlockEnchants that are saved to a Container Block's PDC.
   * @param container the data container to search through
   * @return a map where the enchantment is the key and the enchantment level is the value.
   */
  @NotNull
  public static Map<BlockEnchant, Integer> getBlockEnchants(PersistentDataContainer container) {
    return Enchantments.getEnchantmentManager().getRegisteredBlockEnchants().stream()
        .filter(enchant -> container.has(enchant.getKey(), PersistentDataType.INTEGER))
        .collect(Collectors.toMap(enchant -> enchant,
            enchant -> container.get(enchant.getKey(), PersistentDataType.INTEGER)));
  }

  public static boolean isBlockEnchant(@NotNull Enchantment enchantment) {
    return Enchantments.getEnchantmentManager().getRegisteredBlockEnchants().stream()
        .anyMatch(enchant -> enchant.getKey().equals(enchantment.getKey()));
  }

  @Nullable
  public static BlockEnchant getBlockEnchant(@NotNull NamespacedKey key) {
    return Enchantments.getEnchantmentManager().getRegisteredBlockEnchants().stream()
        .filter(enchant -> enchant.getKey().equals(key))
        .findFirst().orElse(null);
  }

  public boolean hasEnchant(@Nullable TileState tile) {
    return tile != null && tile.getPersistentDataContainer().has(getKey(), PersistentDataType.INTEGER);
  }

  public void addCooldown(@NotNull TileState tile) {
    Enchantments.getCooldownManager().addCooldown(tile, this, getCooldown(), getTimeUnit());
  }

}
