package net.jingles.enchantments;

import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.persistence.DataType;
import net.jingles.enchantments.persistence.EnchantTeam;
import net.jingles.enchantments.statuseffect.LocationStatusEffect;
import net.jingles.enchantments.util.EnchantUtils;
import net.jingles.enchantments.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class EnchantListener implements Listener {

  @EventHandler
  public void onEnchant(EnchantItemEvent event) {

    ItemStack item = event.getItem();

    List<CustomEnchant> enchants = CustomEnchant.getApplicableEnchants(event.getItem()).stream()
        .filter(customEnchant -> event.getEnchanter().getLevel() >= customEnchant.getLevelRequirement())
        .collect(Collectors.toList());

    Map<Enchantment, Integer> additions = event.getEnchantsToAdd();

    // In the PrepareItemEnchantEvent, fake offers were added to the table so that enchantment was possible.
    // This casually removes those while still allowing custom enchantments to be applied to the item.
    if (TargetGroup.NON_VANILLA.canEnchant(item.getType())) {
      additions.clear();
    }

    // Add the enchantment to the additions list if the RNG gods agree.
    // The level is randomly generated for more RN-Jesus fun.

    enchants.forEach(enchant -> {

      if (Math.random() < enchant.getEnchantChance()) {
        int level = ThreadLocalRandom.current().nextInt(enchant.getStartLevel(), enchant.getMaxLevel() + 1);
        additions.put(enchant, level);
      }

    });

    // Add the enchantment lore to the item

    ItemMeta meta = item.getItemMeta();

    InventoryUtils.addEnchantLore(meta, additions.entrySet().stream()
        .filter(entry -> CustomEnchant.isCustomEnchant(entry.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

    item.setItemMeta(meta);

    // There is no need to add the enchantments manually. Upon completion of
    // this event, all of the enchantments in the additions map will be
    // added to the item automagically.

  }

  // Implements vanilla enchantment for items that are not
  // normally able to be enchanted, like blocks.
  @EventHandler
  public void onEnchantPrepare(PrepareItemEnchantEvent event) {

    ItemStack item = event.getInventory().getItem(0);

    // We're only interested if the item cannot be enchanted normally and is not already enchanted.
    if (item == null || !item.getEnchantments().isEmpty() || !TargetGroup.NON_VANILLA.canEnchant(item.getType()))
      return;

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

  @EventHandler
  public void onAnvilInventoryClick(InventoryClickEvent event) {

    if (event.getInventory().getType() != InventoryType.ANVIL) return;

    AnvilInventory inventory = (AnvilInventory) event.getInventory();

    // Only interested if there's an item in the first and middle slot and if there is no result.
    if (inventory.getItem(0) == null || event.getSlot() != 1 ||
        inventory.getItem(2) != null) return;

    ItemStack original = inventory.getItem(0);
    ItemStack addition = inventory.getItem(1);
    ItemStack result = new ItemStack(original.getType(), 1);

    ItemMeta resultMeta = result.getItemMeta();
    Map<Enchantment, Integer> additions = new HashMap<>();

    // Collect the enchantments from the original item
    if (original.getItemMeta() != null)
      original.getItemMeta().getEnchants().forEach(additions::put);


    // Collects the enchantments from the second item
    if (addition != null) {

      if (addition.getType() == Material.ENCHANTED_BOOK) {

        EnchantmentStorageMeta additionMeta = (EnchantmentStorageMeta) addition.getItemMeta();
        additionMeta.getStoredEnchants().forEach(additions::put);

      } else addition.getItemMeta().getEnchants().forEach(additions::put);

    }

    // Add the enchantments to the resulting items
    if (result.getType() == Material.ENCHANTED_BOOK) {

      additions.forEach((enchant, level) ->
          ((EnchantmentStorageMeta) resultMeta).addStoredEnchant(enchant, level, true));

    } else {

      additions.forEach((enchant, level) ->
          resultMeta.addEnchant(enchant, level, true));

    }

    // Add lore for the custom enchants.
    InventoryUtils.addEnchantLore(resultMeta, additions.entrySet().stream()
        .filter(entry -> CustomEnchant.isCustomEnchant(entry.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

    result.setItemMeta(resultMeta);
    inventory.setItem(2, result);
    inventory.setRepairCost(1);
    ((Player) event.getWhoClicked()).updateInventory();

  }

  @EventHandler
  public void onEnchantedBlockPlace(BlockPlaceEvent event) {

    Player player = event.getPlayer();
    ItemStack item = event.getItemInHand();
    BlockState state = event.getBlockPlaced().getState();

    // BlockEnchants can only affect BlockStates that extend TileState
    if (!(state instanceof TileState)) return;

    PersistentDataContainer container = ((TileState) state).getPersistentDataContainer();
    // Save the enchantment level to the container with the corresponding key.
    // Call the onChunkLoad method because the block is being loaded in
    BlockEnchant.getBlockEnchants(item).forEach((enchant, level) -> {
      container.set(enchant.getKey(), PersistentDataType.INTEGER, level);
      enchant.onChunkLoad((TileState) state);
    });

    // Set the enchant's owner
    container.set(Enchantments.OWNER_KEY, DataType.UUID, player.getUniqueId());

    // Apply the default team, which is a copy of the player's.
    EnchantTeam team = EnchantUtils.getEnchantTeam(player);
    team.addTeamedEntity(player.getUniqueId());
    container.set(Enchantments.TEAM_KEY, DataType.ENCHANT_TEAM, team);

    // Update the BlockState so all of these things take effect >_>
    state.update(true);
  }

  @EventHandler
  public void onEnchantedBlockBreak(BlockBreakEvent event) {

    BlockState state = event.getBlock().getState();
    if (!(state instanceof TileState)) return;

    PersistentDataContainer container = ((TileState) state).getPersistentDataContainer();
    Map<BlockEnchant, Integer> enchants = BlockEnchant.getBlockEnchants(container);

    // Non-enchanted blocks are lame, they can be broken without interference.
    if (enchants.isEmpty()) return;

    // Stops all status effects originating from the block.
    Enchantments.getStatusEffectManager().getWorldContainer().getEffectsAtLocation(state.getLocation())
        .forEach(LocationStatusEffect::stop);

    Block block = event.getBlock();
    ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();

    // I'm just gonna casually copy the drops... cause they're annoying
    List<ItemStack> items = new ArrayList<>(block.getDrops(tool));

    // Stop dropping stuff ffs
    event.setDropItems(false);

    items.forEach(item -> {

      if (item.getType() == block.getType()) {

        // Add the enchantments themselves
        ItemMeta meta = item.getItemMeta();

        //TODO: If an enchant has persistent effects, serialize them to
        //  this item's persistent data container.
        enchants.forEach((enchant, level) -> meta.addEnchant(enchant, level, true));

        // Add the enchantment lore
        InventoryUtils.addEnchantLore(meta, enchants);
        item.setItemMeta(meta);

      }

      // Manually drop each block.
      block.getWorld().dropItemNaturally(block.getLocation(), item);

    });

    // Spigot needs to stop changing BlockStates without telling anyone.
    state.setType(Material.AIR);
    state.update(true);

  }

  // Sets the player's enchant team if they don't have one.
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {

    if (event.getPlayer().getPersistentDataContainer().has(Enchantments.TEAM_KEY, DataType.ENCHANT_TEAM)) return;

    Player player = event.getPlayer();
    HashSet<UUID> entities = new HashSet<>();
    entities.add(player.getUniqueId());

    player.getPersistentDataContainer().set(Enchantments.TEAM_KEY, DataType.ENCHANT_TEAM, new EnchantTeam(entities, true));

  }

}
