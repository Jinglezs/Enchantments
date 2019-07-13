package net.jingles.enchantments;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.util.RomanNumerals;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class EnchantListener implements Listener {

  @EventHandler
  public void onEnchant(EnchantItemEvent event) {

    List<CustomEnchant> enchants = Enchantments.getEnchantmentManager().getRegisteredEnchants().stream()
        .filter(customEnchant -> customEnchant.canEnchantItem(event.getItem()))
        .filter(customEnchant -> event.getItem().getEnchantments().keySet()
            .stream().noneMatch(enchant -> enchant.conflictsWith(customEnchant)))
        .filter(customEnchant -> event.getEnchanter().getLevel() >= customEnchant.getLevelRequirement())
        .collect(Collectors.toList());

    Collections.shuffle(enchants);
    Map<Enchantment, Integer> additions = event.getEnchantsToAdd();

    enchants.forEach(enchant -> {

      if (Math.random() < enchant.getEnchantChance()) {
        int level = ThreadLocalRandom.current().nextInt(enchant.getStartLevel(), enchant.getMaxLevel() + 1);
        additions.put(enchant, level);
      }

    });

    additions.entrySet().stream()
        .filter(entry -> entry.getKey() instanceof CustomEnchant)
        .forEach(entry -> {

          CustomEnchant enchant = (CustomEnchant) entry.getKey();
          ItemMeta meta = event.getItem().getItemMeta();
          List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

          lore.add((enchant.isCursed() ? ChatColor.RED : ChatColor.GRAY)
              + enchant.getName() + " " + RomanNumerals.toRoman(entry.getValue()));

          meta.setLore(lore);
          event.getItem().setItemMeta(meta);

        });

  }

  @EventHandler
  public void onAvilEnchantment(PrepareAnvilEvent event) {

    ItemStack previous = event.getInventory().getItem(0);
    ItemStack result = event.getResult();

    if (previous == null || result == null ||
        !previous.hasItemMeta() || !result.hasItemMeta()) return;

    ItemMeta resultMeta = result.getItemMeta();

    previous.getItemMeta().getEnchants().entrySet().forEach(entry -> {
      if (!resultMeta.getEnchants().containsKey(entry.getKey()))
        resultMeta.addEnchant(entry.getKey(), entry.getValue(), true);
    });

    result.setItemMeta(resultMeta);

  }

}
