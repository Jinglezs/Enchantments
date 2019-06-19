package net.jingles.enchantments.tools;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;

@Enchant(name = "Magnetic", key = "magnetic", levelRequirement = 30, maxLevel = 1, enchantChance = 0.40,
    targetItem = EnchantmentTarget.TOOL, targetGroup = TargetGroup.DIGGING, description = "All item drops " +
    "resulting from mining a block are automatically added to the owner's inventory if possible. If the player " +
    "has a Resource Backpack, items will be added to it before they are added to the inventory.")

//TODO:
//   - Add support for Backpacks

public class Magnetic extends CustomEnchant {

  public Magnetic(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment enchant) {
    // Incompatible with Molten Core enchant due to the immutability of block drops.
    return enchant.getKey().getKey().equals("molten_core");
  }

  @Override
  public boolean canTrigger(Player player) {
    ItemStack tool = getItem(player.getInventory());
    return tool != null && hasEnchantment(tool);
  }

  // Should be highest priority so that it can be aware of all changes to the block's drops
  // done by other listeners, such as Molten Core.
  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {

    if (!canTrigger(event.getPlayer())) return;

    Player player = event.getPlayer();
    Block block = event.getBlock();
    Collection<ItemStack> drops = block.getDrops(getItem(player.getInventory()));

    event.setCancelled(true);
    block.setType(Material.AIR);
    block.getState().update();

    drops.forEach(item -> tryAddItems(player, block.getLocation(), item));
  }

  private void tryAddItems(Player player, Location location, ItemStack items) {
    final Map<Integer, ItemStack> overflow = player.getInventory().addItem(items);
    if (!overflow.isEmpty()) {
      player.getWorld().dropItemNaturally(location, items);
    }
  }
}
