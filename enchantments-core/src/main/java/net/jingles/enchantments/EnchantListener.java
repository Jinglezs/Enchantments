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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnchantListener implements Listener {

  @EventHandler
  public void onPrepareEnchant(PrepareItemEnchantEvent event) {

    for (EnchantmentOffer offer : event.getOffers()) {

      if (offer == null) continue;
      if (!(offer.getEnchantment() instanceof CustomEnchant)) continue;

      CustomEnchant enchant = (CustomEnchant) offer.getEnchantment();
      if (offer.getEnchantmentLevel() < enchant.getLevelRequirement())
        offer.setEnchantmentLevel(enchant.getLevelRequirement());

    }

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
