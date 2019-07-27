package net.jingles.enchantments;

import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.statuseffect.LocationStatusEffect;
import net.jingles.enchantments.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
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

    ItemStack item = event.getItem();

    List<CustomEnchant> enchants = CustomEnchant.getApplicableEnchants(event.getItem()).stream()
        .filter(customEnchant -> item.getEnchantments().keySet()
            .stream().noneMatch(enchant -> enchant.conflictsWith(customEnchant)))
        .filter(customEnchant -> event.getEnchanter().getLevel() >= customEnchant.getLevelRequirement())
        .collect(Collectors.toList());

    Collections.shuffle(enchants);
    Map<Enchantment, Integer> additions = event.getEnchantsToAdd();

    // In the PrepareItemEnchantEvent, fake offers were added to the table so that enchantment was possible.
    // This casually removes those while still allowing custom enchantments to be applied to the item.
    if (TargetGroup.NON_VANILLA.canEnchant(item.getType())) {
      additions.clear();
    }

    enchants.forEach(enchant -> {

      if (Math.random() < enchant.getEnchantChance()) {
        int level = ThreadLocalRandom.current().nextInt(enchant.getStartLevel(), enchant.getMaxLevel() + 1);
        additions.put(enchant, level);
      }

    });

    if (item.getType() == Material.BOOK) {

      Map<CustomEnchant, Integer> customAddtions = additions.entrySet().stream()
          .filter(entry -> entry.getKey() instanceof CustomEnchant)
          .collect(Collectors.toMap(entry -> (CustomEnchant) entry.getKey(), Map.Entry::getValue));

      if (!customAddtions.isEmpty()) {

        item.setType(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();

        customAddtions.forEach((key, value) -> meta.addStoredEnchant(key, value, true));
        item.setItemMeta(meta);
        InventoryUtils.addEnchantLore(item, customAddtions);

        customAddtions.keySet().forEach(additions::remove);

      }

    } else {

      InventoryUtils.addEnchantLore(item, additions.entrySet().stream()
          .filter(entry -> entry.getKey() instanceof CustomEnchant)
          .collect(Collectors.toMap(entry -> (CustomEnchant) entry.getKey(), Map.Entry::getValue)));

    }

  }

  // Implements vanilla enchantment for items that are not
  // normally able to be enchanted, like blocks.
  @EventHandler
  public void onEnchantPrepare(PrepareItemEnchantEvent event) {

    ItemStack item = event.getItem();
    // We're only interested if the item cannot be enchanted normally and is not already enchanted.
    if (item.getEnchantments().isEmpty() && !TargetGroup.NON_VANILLA.canEnchant(item.getType())) return;

    // Event must be un-cancelled because the item cannot be enchanted by default.
    event.setCancelled(false);

    // Generate a "filler" enchantment so that the enchantment offers actually
    // appear in game. Whoever hardcoded enchantment names at Mojang is a PoS

    int bound = Enchantment.values().length;
    EnchantmentOffer[] offers = event.getOffers();

    for (int i = 0; i < 3; i++) {

      Enchantment filler = Enchantment.values()[ThreadLocalRandom.current().nextInt(bound)];
      // Generates a random level (based on the enchant's max level) and cost
      int level = ThreadLocalRandom.current().nextInt(1, filler.getMaxLevel() + 1);
      int cost = ThreadLocalRandom.current().nextInt(1, 4);
      offers[i] = new EnchantmentOffer(filler, level, cost);

    }

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

    // Update the BlockState so all of these things take effect >_>
    state.update(true);
  }

  @EventHandler
  public void onEnchantedBlockBreak(BlockBreakEvent event) {

    BlockState state = event.getBlock().getState();
    if (!(state instanceof TileState)) return;

    PersistentDataContainer container = ((TileState) state).getPersistentDataContainer();
    Map<BlockEnchant, Integer> enchants = BlockEnchant.getBlockEnchants(container);

    if (enchants.isEmpty()) return;

    // Cancels all status effects originating from the block.
    Enchantments.getStatusEffectManager().getWorldContainer().getEffectsAtLocation(state.getLocation())
        .forEach(LocationStatusEffect::cancel);

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
        InventoryUtils.addEnchantLore(item, enchants);
      }

      // Manually drop each block.
      block.getWorld().dropItemNaturally(block.getLocation(), item);

      block.setType(Material.AIR);
      state.update();

    });

  }

}
