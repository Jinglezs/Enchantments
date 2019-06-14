package net.jingles.enchantments.tools;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Enchant(name = "Molten Core", key = "molten_core", maxLevel = 1, targetItem = EnchantmentTarget.TOOL,
    targetGroup = TargetGroup.DIGGING, description = "Automatically smelts ores/powders as they are " +
    "mined/shoveled.")

public class MoltenCore extends CustomEnchant {

  private final Map<Material, Material> SMELTABLE = new HashMap<>();

  public MoltenCore(NamespacedKey key) {
    super(key);

    SMELTABLE.put(Material.IRON_ORE, Material.IRON_INGOT);
    SMELTABLE.put(Material.GOLD_ORE, Material.GOLD_INGOT);
    SMELTABLE.put(Material.SAND, Material.GLASS);
    SMELTABLE.put(Material.COBBLESTONE, Material.STONE);

    //Accounts for all concrete powder -> concrete transformations.
    Stream.of(Material.values())
        .filter(material -> material.name().endsWith("CONCRETE_POWDER"))
        .forEach(material -> SMELTABLE.put(material,
            Material.valueOf(material.name().replace("_POWDER", ""))));

  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    // Incompatible with Magnetic enchant due to the immutability of block drops.
    return other.equals(Enchantment.SILK_TOUCH) || other.getKey().getKey().equals("magnetic");
  }

  @Override
  public boolean canTrigger(Inventory inventory, Event event) {
    ItemStack held = getItem(inventory);
    return held != null && hasEnchantment(held);
  }

  @EventHandler
  public void onSmeltableBreak(BlockBreakEvent event) {

    if (!canTrigger(event.getPlayer().getInventory(), event) ||
        !SMELTABLE.containsKey(event.getBlock().getType())) return;

    Block block = event.getBlock();
    Material replacement = SMELTABLE.get(block.getType());
    ItemStack tool = getItem(event.getPlayer().getInventory());
    Collection<ItemStack> drops = block.getDrops(tool);

    int amount = drops.stream().filter(stack -> stack.getType() == block.getType())
        .findFirst().map(ItemStack::getAmount).orElse(1);

    drops.removeIf(stack -> stack.getType() == block.getType());
    drops.add(new ItemStack(replacement, amount));

    event.setCancelled(true);
    block.setType(Material.AIR);
    block.getState().update();
    drops.forEach(item -> block.getWorld().dropItemNaturally(block.getLocation(), item));

  }

}
