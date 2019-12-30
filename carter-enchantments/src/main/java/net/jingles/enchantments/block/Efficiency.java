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
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Enchant(name = "Efficiency", key = "efficiency", enchantChance = 0.45, levelRequirement = 20,
    targetItem = EnchantmentTarget.ALL, targetGroup = TargetGroup.COOKING_BLOCKS, description =
      "FOR FURNACES: Increases how long fuel burns for by 40% per level. Additionally, there is a " +
          "20% chance per level that the amount of items smelted at once is doubled.\nFOR BREWING STANDS: " +
          "Fuel lasts twice as long and there is a 20% chance per level that the ingredient is not consumed.")

public class Efficiency extends BlockEnchant {

  public Efficiency(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean canTrigger(@Nullable TileState tile) {
    return tile != null && hasEnchant(tile) && !Enchantments.getCooldownManager()
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

  // Adds 2 to the result ItemStack, removes 1 from the smelting ItemStack.
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

  // Doubles the stand's fuel power
  @EventHandler
  public void onBrewingStandFuel(BrewingStandFuelEvent event) {
    event.setFuelPower(event.getFuelPower() * 2);
  }

  // Adds 1 to the ingredient ItemStack, so that it will remain at the
  // previous amount when it is consumed.
  @EventHandler
  public void onPotionBrew(BrewEvent event) {

    if (!canTrigger(event.getContents().getHolder())) return;

    double chance = (getLevel(event.getContents().getHolder()) * 20) / 100D;

    if (Math.random() <= chance) {
      ItemStack item = event.getContents().getIngredient();
      if (item != null) item.setAmount(item.getAmount() + 1);
    }

  }

}
