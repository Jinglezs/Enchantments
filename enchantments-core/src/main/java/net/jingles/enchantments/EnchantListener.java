package net.jingles.enchantments;

import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.util.InventoryUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
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
          InventoryUtils.addEnchantLore(event.getItem(), Collections.singleton(enchant));
        });

  }

  // This fixes an issue where repairing an item or adding any enchantment to an
  // item with custom enchants caused all custom enchants to be erased.
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

  @EventHandler
  public void onEnchantedBlockPlace(BlockPlaceEvent event) {

    ItemStack item = event.getItemInHand();
    BlockState state = event.getBlockPlaced().getState();

    // BlockEnchants can only affect BlockStates that extend Container
    if (!(state instanceof Container)) return;

    PersistentDataContainer container = ((Container) state).getPersistentDataContainer();
    // Save the enchantment level to the container with the corresponding key.
    BlockEnchant.getBlockEnchants(item).forEach((enchant, level) ->
        container.set(enchant.getKey(), PersistentDataType.INTEGER, level));
  }

  @EventHandler
  public void onEnchantedBlockBreak(BlockBreakEvent event) {

    BlockState state = event.getBlock().getState();
    if (!(state instanceof Container)) return;

    PersistentDataContainer container = ((Container) state).getPersistentDataContainer();
    Map<BlockEnchant, Integer> enchants = BlockEnchant.getBlockEnchants(container);

    if (enchants.isEmpty()) return;
    event.setCancelled(true);

    Block block = event.getBlock();
    Collection<ItemStack> items = block.getDrops();

    items.forEach(item -> {

      if (item.getType() == block.getType()) {
        // Add the enchantments themselves
        ItemMeta meta = item.getItemMeta();
        enchants.forEach((enchant, level) -> meta.addEnchant(enchant, level, true));
        item.setItemMeta(meta);

        // Add the enchantment lore
        InventoryUtils.addEnchantLore(item, enchants.keySet());
      }

      // Manually drop each block.
      block.getWorld().dropItemNaturally(block.getLocation(), item);

    });

  }

}
