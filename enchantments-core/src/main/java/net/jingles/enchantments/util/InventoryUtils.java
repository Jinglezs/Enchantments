package net.jingles.enchantments.util;

import net.jingles.enchantments.enchant.CustomEnchant;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InventoryUtils {

  public static void removeItem(@NotNull Inventory inventory, @NotNull Material material, int amount) {
    Stream.of(inventory.getContents())
        .filter(Objects::nonNull)
        .filter(item -> item.getType() == material)
        .findFirst().ifPresent(item -> {
          int newAmount = item.getAmount() - amount;
          if (newAmount < 1) inventory.remove(item);
          else item.setAmount(newAmount);
    });
  }


  public static void removeItem(@NotNull Inventory inventory, @NotNull Tag<Material> tag, int amount) {
    Stream.of(inventory.getContents())
        .filter(Objects::nonNull)
        .filter(item -> tag.isTagged(item.getType()))
        .findFirst().ifPresent(item -> {
          int newAmount = item.getAmount() - amount;
          if (newAmount < 1) inventory.remove(item);
          else item.setAmount(newAmount);
    });

  }

  public static void addEnchantLore(@NotNull ItemStack item, @NotNull Map<? extends Enchantment, Integer> enchants) {
    addEnchantLore(item.getItemMeta(), enchants);
  }

  public static void addEnchantLore(@NotNull ItemMeta meta, @NotNull Map<? extends Enchantment, Integer> enchants) {
    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

    enchants.forEach((enchant, level) -> lore.add((enchant.isCursed() ? ChatColor.RED : ChatColor.GRAY)
        + enchant.getName() + " " + RomanNumerals.toRoman(level)));

    meta.setLore(lore);
  }

  public static void removeEnchantLore(@NotNull ItemStack item) {
    if (item.getItemMeta() == null || item.getEnchantments().isEmpty()) return;
    else if (item.getItemMeta().getLore() == null) return;

    Set<CustomEnchant> enchants = item.getEnchantments().keySet().stream()
        .filter(enchantment -> enchantment instanceof CustomEnchant)
        .map(enchantment -> (CustomEnchant) enchantment)
        .collect(Collectors.toSet());

    List<String> lore = item.getItemMeta().getLore();
    lore.removeIf(line -> enchants.stream().anyMatch(enchant -> line.contains(enchant.getName())));

    ItemMeta meta = item.getItemMeta();
    meta.setLore(lore);
    item.setItemMeta(meta);
  }

  public static void removeEnchantLore(@NotNull ItemStack item, @NotNull CustomEnchant enchant) {
    if (item.getItemMeta() == null || item.getEnchantments().isEmpty()) return;
    else if (item.getItemMeta().getLore() == null) return;

    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    lore.removeIf(line -> line.contains(enchant.getName()));
    meta.setLore(lore);

    item.setItemMeta(meta);
  }

}
