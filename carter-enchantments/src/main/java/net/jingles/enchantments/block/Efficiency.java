package net.jingles.enchantments.block;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Container;
import org.bukkit.block.Furnace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;

@Enchant(name = "Efficiency", key = "efficiency", enchantChance = 0.45, levelRequirement = 20,
    targetItem = EnchantmentTarget.ALL, targetGroup = TargetGroup.CONTAINER, maxLevel = 10,
    description = "Increases how long fuel burns for by 20% per level. Additionally, smelting " +
        "progress automatically jumps to (20 * level)%, effectively reducing smelting times.")

public class Efficiency extends BlockEnchant {

  public Efficiency(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean canTrigger(Container container) {
    return hasEnchant(container) && !Enchantments.getCooldownManager()
        .hasCooldown(container, this);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  // Applies the buffed burn and cook times
  // Decreasing CookTimeTotal led to weird bugs so let's just do this instead >_>
  @EventHandler
  public void onItemBurn(FurnaceBurnEvent event) {

    if (!canTrigger((Container) event.getBlock().getState())) return;

    Furnace furnace = (Furnace) event.getBlock().getState();

    furnace.setBurnTime(getNewBurnTime(furnace));
    if (furnace.getCookTime() == 0) furnace.setCookTime(getNewCookTime(furnace));
    furnace.update(true);

  }

  @EventHandler
  public void onItemSmelt(FurnaceSmeltEvent event) {

    if (!canTrigger((Container) event.getBlock().getState())) return;

    Furnace furnace = (Furnace) event.getBlock().getState();
    furnace.setCookTime(getNewCookTime(furnace));
    furnace.update(true);

  }

  private short getNewBurnTime(Furnace furnace) {
    double multiplier = (getLevel(furnace) * 20) / 100;
    return (short) Math.min(Short.MAX_VALUE, furnace.getBurnTime() + (furnace.getBurnTime() * multiplier));
  }

  private short getNewCookTime(Furnace furnace) {
    double multiplier = (getLevel(furnace) * 10) / 100;
    return (short) Math.min(90, furnace.getCookTimeTotal() * multiplier);
  }

}
