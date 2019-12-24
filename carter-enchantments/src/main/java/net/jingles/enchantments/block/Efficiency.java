package net.jingles.enchantments.block;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Container;
import org.bukkit.block.Furnace;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Enchant(name = "Efficiency", key = "efficiency", enchantChance = 0.45, levelRequirement = 20,
    targetItem = EnchantmentTarget.ALL, targetGroup = TargetGroup.FURNACES,
    description = "Increases how long fuel burns for by 40% per level. Additionally, there is a " +
        "20% chance per level that the amount of items smelted at once is doubled.")

public class Efficiency extends BlockEnchant {

  public Efficiency(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean canTrigger(@NotNull TileState tile) {
    return hasEnchant(tile) && !Enchantments.getCooldownManager()
        .hasCooldown(tile, this);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  // Applies the buffed burn times to the furnace state
  @EventHandler
  public void onItemBurn(FurnaceBurnEvent event) {

    if (!canTrigger((Container) event.getBlock().getState())) return;

    Furnace furnace = (Furnace) event.getBlock().getState();

    double multiplier = (getLevel(furnace) * 40) / 100;
    short burnTime = (short) Math.min(Short.MAX_VALUE, furnace.getBurnTime() + (furnace.getBurnTime() * multiplier));

    furnace.setBurnTime(burnTime);
    furnace.update(true);

  }

  @EventHandler
  public void onItemSmelt(FurnaceSmeltEvent event) {

    Furnace furnace = (Furnace) event.getBlock().getState();
    if (!canTrigger(furnace)) return;

    if (Math.random() > (getLevel(furnace) * 20) / 100D) return;

    ItemStack smelting = furnace.getInventory().getSmelting();
    ItemStack result = furnace.getInventory().getResult();

    if (smelting == null || smelting.getAmount() == 1 ||
        result == null || result.getAmount() + 1 > result.getMaxStackSize()) return;

    smelting.setAmount(smelting.getAmount() - 1);
    result.setAmount(result.getAmount() + 1);

  }

}
