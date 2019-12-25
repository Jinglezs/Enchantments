package net.jingles.enchantments.enchant;

import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;

import java.util.function.Predicate;

public enum TargetGroup {

  /**
   * Targets only axes
   */
  AXES(material -> material.name().endsWith("_AXE")),

  /**
   * Targets only hoes
   */
  HOES(material -> material.name().endsWith("_HOE")),

  /**
   * Targets only shovels
   */
  SHOVELS(material -> material.name().endsWith("_SHOVEL")),

  /**
   * Targets only pickaxes
   */
  PICKAXES(material -> material.name().endsWith("_PICKAXE")),

  /**
   * Targets tools meant for breaking blocks
   */
  DIGGING(material -> PICKAXES.canEnchant(material) || SHOVELS.canEnchant(material)),

  /**
   * Targets tools meant to deal damage at a close range.
   */
  MELEE_WEAPONS(material -> EnchantmentTarget.WEAPON.includes(material) || AXES.canEnchant(material)),

  /**
   * Targets shears
   */
  SHEARS(material -> material == Material.SHEARS),

  /**
   * Targets Flint and Steel item
   */
  FLINT_AND_STEEL(material -> material == Material.FLINT_AND_STEEL),

  /**
   * Leads/leashes
   */
  LEAD(material -> material == Material.LEAD),

  /**
   * Targets only armor
   */
  ALL_ARMOR(EnchantmentTarget.ARMOR::includes),

  /**
   * Targets items that can't be worn by the player
   */
  NON_WEARABLE(material -> !EnchantmentTarget.WEARABLE.includes(material)),

  /**
   * Targets the Elytra item
   */
  ELYTRA(material -> material == Material.ELYTRA),

  /**
   * Targets both types of bows
   */
  BOWS(material -> material == Material.BOW || material == Material.CROSSBOW),

  /**
   * Only targets the Shield item
   */
  SHIELD(material -> material == Material.SHIELD),

  /**
   * Targets all items that can be placed
   */
  BLOCK(Material::isBlock),

  /**
   * The enchantment can only target blocks that are subinterfaces of the Container interface
   */
  CONTAINER(material -> material == Material.CHEST || material == Material.BARREL || material == Material.FURNACE ||
      material == Material.BLAST_FURNACE || material == Material.BREWING_STAND || material == Material.SMOKER ||
      material == Material.HOPPER || material == Material.DISPENSER || material == Material.DROPPER || material == Material.SHULKER_BOX),

  /**
   * Represents all items that have tile entities at their location when placed. This includes all of the materials listed under CONTAINER
   */
  TILE_ENTITY(material -> CONTAINER.canEnchant(material) || material.name().endsWith("_BANNER") || material.name().endsWith("_BED") ||
      material.name().endsWith("_SIGN") || material.name().endsWith("_SKULL") || material == Material.CONDUIT || material == Material.END_CRYSTAL ||
      material == Material.COMPARATOR || material == Material.JUKEBOX || material == Material.LECTERN || material == Material.BEACON ||
      material == Material.CAMPFIRE),

  /**
   * Blocks that have the same functionality as the default furnace
   */
  FURNACES(material -> material == Material.FURNACE || material == Material.BLAST_FURNACE || material == Material.SMOKER),

  /**
   * The lectern block, *Billie Eilish duh*
   */
  LECTERN(material -> material == Material.LECTERN),

  /**
   * All banner blocks
   */
  BANNER(material -> material.name().endsWith("_BANNER")),

  /**
   * Jukeboxes only
   */
  JUKEBOX(material -> material == Material.JUKEBOX),

  /**
   * Campfires yay
   */
  CAMPFIRE(material -> material == Material.CAMPFIRE),

  /**
   * Bells yay
   */
  BELL(material -> material == Material.BELL),

  /**
   * Compasses wew
   */
  COMPASS(material-> material == Material.COMPASS),

  /**
   * Mob skull blocks
   */
  SKULLS(material -> material.name().endsWith("_SKULL")),

  /**
   * Brewing stand, obviously
   */
  BREWING_STAND(material -> material == Material.BREWING_STAND),

  /**
   * Any block that is capable of smelting/brewing items, excluding Campfires.
   */
  COOKING_BLOCKS(material -> FURNACES.canEnchant(material) || BREWING_STAND.canEnchant(material)),

  /**
   * Represents all items that can be enchanted through this plugin, but cannot be enchanted normally in vanilla.
   */
  NON_VANILLA(material -> TILE_ENTITY.canEnchant(material) || BLOCK.canEnchant(material) || ELYTRA.canEnchant(material) ||
      SHIELD.canEnchant(material) || SHEARS.canEnchant(material) || FLINT_AND_STEEL.canEnchant(material)),

  /**
   * The enchant can target any material.
   */
  NONE(material -> true);

  private Predicate<Material> includes;

  TargetGroup(Predicate<Material> includes) {
    this.includes = includes;
  }

  public boolean canEnchant(Material material) {
    return includes.test(material);
  }

}
