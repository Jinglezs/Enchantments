package net.jingles.enchantments.enchant;

import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;

import java.util.function.Predicate;

public enum TargetGroup {

  AXES(material -> material.name().endsWith("_AXE")),
  HOES(material -> material.name().endsWith("_HOE")),
  SHOVELS(material -> material.name().endsWith("_SHOVEL")),
  PICKAXES(material -> material.name().endsWith("_PICKAXE")),
  DIGGING(material -> PICKAXES.canEnchant(material) || SHOVELS.canEnchant(material)),
  MELEE_WEAPONS(material -> EnchantmentTarget.WEAPON.includes(material) || AXES.canEnchant(material)),
  SHEARS(material -> material == Material.SHEARS),
  FLINT_AND_STEEL(material -> material == Material.FLINT_AND_STEEL),
  ALL_ARMOR(EnchantmentTarget.ARMOR::includes),
  NON_WEARABLE(material -> !EnchantmentTarget.WEARABLE.includes(material)),
  ELYTRA(material -> material == Material.ELYTRA),
  BOWS(material -> material == Material.BOW || material == Material.CROSSBOW),
  NONE(material -> true);

  private Predicate<Material> includes;

  TargetGroup(Predicate<Material> includes) {
    this.includes = includes;
  }

  public boolean canEnchant(Material material) {
    return includes.test(material);
  }

}
