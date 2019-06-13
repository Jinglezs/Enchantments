package net.jingles.enchantments;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.util.RomanNumerals;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class EnchantListener implements Listener {

  @EventHandler
  public void onPrepareEnchant(PrepareItemEnchantEvent event) {

    if (event.isCancelled()) event.setCancelled(false);

    // Gets all possible enchantments, both custom and vanilla.
    List<Enchantment> enchantments = Arrays.asList(Enchantment.values());
    enchantments.addAll(Enchantments.getEnchantmentManager().getRegisteredEnchants());

    // Filter them so it only includes those that can applied to the item
    enchantments = enchantments.stream()
        .filter(enchantment -> enchantment.canEnchantItem(event.getItem()))
        .collect(Collectors.toList());

    // If it's a custom enchantment, ensure that the player meets the level requirement
    // Remove the enchantment from the list of possibilities if they do not.
    enchantments.removeIf(enchantment -> enchantment instanceof CustomEnchant &&
        event.getEnchanter().getLevel() < ((CustomEnchant) enchantment).getLevelRequirement());

    // Shuffle the list so that it is random.
    Collections.shuffle(enchantments);
    // Get the array that determines what shows up in the enchantment table from the event.
    EnchantmentOffer[] offers = event.getOffers();

    // Override the array with our own enchantments.
    for (int i = 0; i < 3; i++) {
      if (i < enchantments.size()) offers[i] = getEnchantOffer(enchantments.get(i));
      else offers[i] = null;
    }

  }

  /**
   * Creates an EnchantmentOffer for the given enchantment. The level and cost are determined
   * randomly and are bounded by the enchantment values.
   * @param enchantment the desired enchantment
   * @return the enchantment offer
   */
  private EnchantmentOffer getEnchantOffer(Enchantment enchantment) {
    int level = ThreadLocalRandom.current().nextInt(enchantment.getStartLevel(), enchantment.getMaxLevel() + 1);
    int cost = ThreadLocalRandom.current().nextInt(1, 11);
    return new EnchantmentOffer(enchantment, level, cost);
  }

  @EventHandler
  public void onEnchant(EnchantItemEvent event) {

    Map<Enchantment, Integer> enchants = event.getEnchantsToAdd();

    enchants.entrySet().stream()
        .filter(entry -> entry.getKey() instanceof CustomEnchant)
        .forEach(entry -> {

          CustomEnchant enchant = (CustomEnchant) entry.getKey();
          ItemMeta meta = event.getItem().getItemMeta();
          List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

          lore.add(ChatColor.AQUA + enchant.getName() + " " + RomanNumerals.toRoman(entry.getValue()));
          meta.setLore(lore);
          event.getItem().setItemMeta(meta);

        });

  }

}
