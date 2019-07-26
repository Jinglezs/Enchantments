package net.jingles.enchantments.block;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.BlockEnchant;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Container;
import org.bukkit.block.Furnace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.FurnaceBurnEvent;

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

  @EventHandler
  public void onItemBurn(FurnaceBurnEvent event) {

    Furnace furnace = (Furnace) event.getBlock().getState();
    int level = getLevel(furnace);

    int burnTime = event.getBurnTime() + (event.getBurnTime() * (level / 100));
    int cookTime = furnace.getCookTimeTotal() - (furnace.getCookTimeTotal() * (level/100));

    event.setBurnTime(burnTime);
    furnace.setCookTimeTotal(cookTime);

  }

}
