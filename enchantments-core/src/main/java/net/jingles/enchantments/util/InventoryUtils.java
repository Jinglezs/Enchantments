package net.jingles.enchantments.util;

import net.jingles.enchantments.enchant.CustomEnchant;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InventoryUtils {

  public static void removeItem(Inventory inventory, Material material, int amount) {
    Stream.of(inventory.getContents())
        .filter(Objects::nonNull)
        .filter(item -> item.getType() == material)
        .findFirst().ifPresent(item -> {
          int newAmount = item.getAmount() - amount;
          if (newAmount < 1) inventory.remove(item);
          else item.setAmount(newAmount);
    });
  }


  public static void removeItem(Inventory inventory, Tag<Material> tag, int amount) {
    Stream.of(inventory.getContents())
        .filter(Objects::nonNull)
        .filter(item -> tag.isTagged(item.getType()))
        .findFirst().ifPresent(item -> {
          int newAmount = item.getAmount() - amount;
          if (newAmount < 1) inventory.remove(item);
          else item.setAmount(newAmount);
    });

  }

  public static void addEnchantLore(ItemStack item, Set<? extends CustomEnchant> enchants) {

    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

    enchants.forEach(enchant -> lore.add((enchant.isCursed() ? ChatColor.RED : ChatColor.GRAY)
        + enchant.getName() + " " + RomanNumerals.toRoman(enchant.getLevel(item))));

    meta.setLore(lore);
    item.setItemMeta(meta);

  }

  public static void removeEnchantLore(ItemStack item) {
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

  public static void removeEnchantLore(ItemStack item, CustomEnchant enchant) {
    if (item.getItemMeta() == null || item.getEnchantments().isEmpty()) return;
    else if (item.getItemMeta().getLore() == null) return;

    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    lore.removeIf(line -> line.contains(enchant.getName()));
    meta.setLore(lore);

    item.setItemMeta(meta);
  }

}
