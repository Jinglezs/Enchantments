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

@Enchant(name = "Efficiency", key = "efficiency", enchantChance = 0.45, levelRequirement = 20,
    targetItem = EnchantmentTarget.ALL, targetGroup = TargetGroup.CONTAINER, maxLevel = 10,
    description = "Increases how long fuel burns for by 10% per level and decreases the time " +
        "it takes to smelt any item by 10% per level.")

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
    int level = getLevel(furnace) * 10;

    int burnTime = event.getBurnTime() + (event.getBurnTime() * (level / 100));
    int cookTime = furnace.getCookTimeTotal() - (furnace.getCookTimeTotal() * (level / 100));

    event.setBurnTime(burnTime);
    furnace.setCookTimeTotal(cookTime);

  }

}
