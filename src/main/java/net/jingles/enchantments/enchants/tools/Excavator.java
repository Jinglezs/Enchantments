package net.jingles.enchantments.enchants.tools;

import net.jingles.enchantments.Enchant;
import net.jingles.enchantments.enchants.CustomEnchant;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Enchant(name = "Excavator", key = "excavator", targetItem = EnchantmentTarget.TOOL, levelRequirement = 30,
        maxLevel = 1, description = "Breaks all of the blocks surrounding the broken block.")

public class Excavator extends CustomEnchant {

  private static final List<BlockFace> FACES = Arrays.asList(BlockFace.NORTH, BlockFace.EAST,
          BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST,
          BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST);

  public Excavator(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(Inventory inventory, Event event) {
    return hasEnchantment(((PlayerInventory) inventory).getItemInMainHand());
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {

    if (!canTrigger(event.getPlayer().getInventory(), event)) return;

    Block center = event.getBlock();
    List<Block> blocks = new ArrayList<>();
    FACES.forEach(face -> blocks.add(center.getRelative(face)));

    ItemStack item = event.getPlayer().getInventory().getItemInMainHand();

    //Block.breakNaturally(ItemStack) does not respect enchantments.

    blocks.forEach(block -> {
      if (item.containsEnchantment(Enchantment.SILK_TOUCH)) {
        block.getDrops().clear();
        block.getDrops().addAll(block.getDrops(item));
      }

      if (item.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
        int fortune = item.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);
        block.getDrops().forEach(stack -> stack.setAmount(stack.getAmount() * fortune));
      }

      block.breakNaturally(item);
    });

  }


}
