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
   * Targets only armor
   */
  ALL_ARMOR(EnchantmentTarget.ARMOR::includes),
  /**
   * Targets items that can be worn by the player
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
