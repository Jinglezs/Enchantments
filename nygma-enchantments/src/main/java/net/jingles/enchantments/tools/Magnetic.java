package net.jingles.enchantments.tools;

import net.jingles.backpacks.BackpackUtils;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;

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
    ItemStack tool = getItem(player.getInventory());
    Collection<ItemStack> drops = block.getDrops(tool);

    event.setCancelled(true);
    block.setType(Material.AIR);
    block.getState().update();

    int level = tool.getItemMeta().getEnchantLevel(Enchantment.DURABILITY);
    // Adds one damage to the tool.
    if (level == 0 || Math.random() < (1.0 / (level + 1))) addDamage(tool);

    drops.forEach(item -> BackpackUtils.addToResourceBackpacks(item, player.getInventory(), block.getLocation()));
  }

  private void addDamage(ItemStack item) {
    ItemMeta meta = item.getItemMeta();
    int durability = ((Damageable) meta).getDamage() + 1;
    ((Damageable) meta).setDamage(durability);
    item.setItemMeta(meta);
  }

}
