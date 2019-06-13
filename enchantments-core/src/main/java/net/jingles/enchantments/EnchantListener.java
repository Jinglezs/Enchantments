package net.jingles.enchantments;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.util.RomanNumerals;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class EnchantListener implements Listener {

  @EventHandler
  public void onPrepareEnchant(PrepareItemEnchantEvent event) {

    EnchantmentOffer[] offers = event.getOffers();

    for (int i = 0; i < event.getOffers().length; i++) {

      if (offers[i] == null) offers[i] = populateEmptyEnchant(event.getItem().getType());

      EnchantmentOffer offer = offers[i];
      //The offer can still be null. The populate method is not always successful!
      if (offer == null || !(offer.getEnchantment() instanceof CustomEnchant)) continue;

      CustomEnchant enchant = (CustomEnchant) offer.getEnchantment();

      if (enchant.getTargetGroup() != null && !enchant.getTargetGroup().canEnchant(event.getItem().getType())) {
        offers[i] = null; //Remove the enchantment offer
        continue;
      }

      if (offer.getCost() < enchant.getLevelRequirement())
        offer.setCost(enchant.getLevelRequirement());

    }

  }

  @Nullable
  private EnchantmentOffer populateEmptyEnchant(Material material) {

    List<CustomEnchant> applicable = Enchantments.getEnchantmentManager().getRegisteredEnchants().stream()
        .filter(enchant -> enchant.getTargetGroup() != null && enchant.getTargetGroup().canEnchant(material))
        .collect(Collectors.toList());

    if (applicable.isEmpty()) return null;

    Collections.shuffle(applicable);
    CustomEnchant enchant = applicable.get(0);
    int level = ThreadLocalRandom.current().nextInt(1, enchant.getMaxLevel() + 1);
    int cost = ThreadLocalRandom.current().nextInt(1, 11);

    return new EnchantmentOffer(enchant, level, cost);
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
