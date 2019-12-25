package net.jingles.enchantments.block;

import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

import static org.bukkit.entity.EntityType.*;

@Enchant(name = "Summoner", key = "summoner", levelRequirement = 30, targetItem = EnchantmentTarget.ALL, maxLevel = 1,
    enchantChance = 0.15, targetGroup = TargetGroup.NONE, description = "This enchantment can be applied to any item. " +
    "Right clicking a diamond block with the enchanted item will consume the item and replace the block with a mob spawner. " +
    "The type of mob that is spawned is based on what the item material is. For example, a feather would result in a " +
    "chicken spawner. Right clicking a spawner created by this enchantment while sneaking will refund the enchantment.")

public class Summoner extends BlockEnchant {

  public Summoner(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean canTrigger(@Nullable TileState tile) {
    return hasEnchant(tile);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @EventHandler
  public void onItemInteract(PlayerInteractEvent event) {

    ItemStack item = event.getItem();
    Block block = event.getClickedBlock();

    if (item == null || block == null) return;

    if (hasEnchantment(item) && block.getType() == Material.DIAMOND_BLOCK) {

      event.setCancelled(true);

      block.setType(Material.SPAWNER);
      block.getState().update(true);

      CreatureSpawner spawner = (CreatureSpawner) block.getState();

      // Set the spawner's values
      spawner.setSpawnedType(getEntityType(item.getType()));
      spawner.setMaxNearbyEntities(15);
      spawner.setRequiredPlayerRange(40000);
      spawner.setMaxSpawnDelay(400);

      // Add the enchantment to the spawner's persistent data container
      spawner.getPersistentDataContainer().set(getKey(), PersistentDataType.INTEGER, getLevel(item));

      // Update the block state so that all of the changes are saved.
      spawner.update(true);

      // Consume the enchanted item used to create the spawner.
      event.getPlayer().getInventory().remove(item);

      // Replaces the spawner with a diamond block and refunds the enchantment.
    } else if (block.getType() == Material.SPAWNER && event.getPlayer().isSneaking()) {

      event.setCancelled(true);

      CreatureSpawner spawner = (CreatureSpawner) block.getState();
      if (!spawner.getPersistentDataContainer().has(getKey(), PersistentDataType.INTEGER)) return;

      // Get the meta instance or create a new one
      ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());

      // Add the enchantment
      int level = spawner.getPersistentDataContainer().get(getKey(), PersistentDataType.INTEGER);
      meta.addEnchant(this, level, true);

      // Add the enchantment lore
      InventoryUtils.addEnchantLore(meta, Collections.singletonMap(this, level));
      item.setItemMeta(meta);

      block.setType(Material.DIAMOND_BLOCK);
      block.getState().update(true);

    }

  }

  private EntityType getEntityType(Material material) {

    switch (material) {

      // Standard farm animals
      case FEATHER: return CHICKEN;
      case CARROT: return PIG;
      case WHITE_WOOL: return SHEEP;
      case LEATHER: return COW;
      case SADDLE: return HORSE;

      // Other docile land mobs:
      case COOKED_BEEF: return WOLF;
      case IRON_INGOT: return IRON_GOLEM;
      case SNOWBALL: return SNOWMAN;
      case ICE: return POLAR_BEAR;
      case RABBIT_HIDE: return RABBIT;
      case RED_MUSHROOM: return MUSHROOM_COW;
      case COAL: return BAT;
      case WHEAT_SEEDS: return PARROT;
      case COOKED_SALMON: return CAT;
      case BAMBOO: return PANDA;
      case EMERALD: return VILLAGER;
      case COMPASS: return WANDERING_TRADER;
      case ORANGE_DYE: return FOX;
      case HONEY_BOTTLE: return BEE;

      // Aquatic mobs
      case SALMON: return SALMON;
      case PUFFERFISH: return PUFFERFISH;
      case TROPICAL_FISH: return TROPICAL_FISH;
      case COD: return COD;
      case COOKED_COD: return DOLPHIN;
      case INK_SAC: return SQUID;
      case TURTLE_EGG: return TURTLE;
      case PRISMARINE_SHARD: return GUARDIAN;
      case KELP: return DROWNED;

      // Hostile Overworld mobs
      case GUNPOWDER: return CREEPER;
      case BONE: return SKELETON;
      case STRING: return SPIDER;
      case STICK: return GIANT;
      case ROTTEN_FLESH: return ZOMBIE;
      case SLIME_BALL: return SLIME;
      case SPIDER_EYE: return CAVE_SPIDER;
      case COOKIE: return SILVERFISH;
      case POTION: return WITCH;
      case PHANTOM_MEMBRANE: return PHANTOM;
      case CROSSBOW: return PILLAGER;
      case TOTEM_OF_UNDYING: return EVOKER;
      case IRON_SWORD: return VEX;
      case IRON_AXE: return VINDICATOR;
      case BOW: return ILLUSIONER;
      case ARROW: return STRAY;

      // Hostile Nether mobs
      case GHAST_TEAR: return GHAST;
      case GOLDEN_SWORD: return PIG_ZOMBIE;
      case BLAZE_POWDER: return BLAZE;
      case MAGMA_CREAM: return MAGMA_CUBE;
      case WITHER_SKELETON_SKULL: return WITHER_SKELETON;

      // Hostile End mobs
      case SHULKER_SHELL: return SHULKER;
      case ENDER_PEARL: return ENDERMAN;
      case ENDER_EYE: return ENDERMITE;

      // Bosses
      case DRAGON_BREATH: return ENDER_DRAGON;
      case NETHER_STAR: return WITHER;

      default: return PIG;
    }

  }

}
