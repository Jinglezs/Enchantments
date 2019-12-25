package net.jingles.enchantments.block;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.statuseffect.LocationStatusEffect;
import net.jingles.enchantments.statuseffect.context.TileEntityContext;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.jetbrains.annotations.NotNull;

@Enchant(name = "Volatile", key = "volatile", targetGroup = TargetGroup.BREWING_STAND,
  description = "This brewing stand has been infused with gunpowder, causing all of its potions " +
      "to become volatile. Anything it brews becomes a splash potion and is upgraded by one level.")
public class Volatile extends BlockEnchant {

  public Volatile(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean canTrigger(@NotNull TileState tile) {
    return hasEnchant(tile);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @EventHandler
  public void onPotionBrew(BrewEvent event) {

    BrewingStand stand = event.getContents().getHolder();
    if (stand == null || !canTrigger(stand)) return;

    TileEntityContext context = new TileEntityContext(stand, this);
    Enchantments.getStatusEffectManager().add(new VolatilePotionDetectEffect(context));

  }

  // After a delay of 3 ticks, this will replace all potions with their splash potion
  // counterparts. Additionally, all of the potions will be upgraded.
  private static class VolatilePotionDetectEffect extends LocationStatusEffect {

    private final BrewingStand stand;

    private VolatilePotionDetectEffect(TileEntityContext context) {
      super(context, 1, 1, 3, context.getTrigger().getLocation());
      this.stand = (BrewingStand) context.getTrigger();
    }

    @Override
    public void effect() {

      BrewerInventory inventory = stand.getInventory();

      for (int i = 0; i < 3; i++) {

        ItemStack item = inventory.getItem(i);
        if (item == null || item.getType() != Material.POTION) continue;

        PotionMeta meta = (PotionMeta) item.getItemMeta();
        PotionData data = meta.getBasePotionData();

        // Do nothing to incomplete potions
        switch (data.getType()) {
          case WATER:
          case THICK:
          case AWKWARD:
          case MUNDANE:
            continue;
        }

        // Upgrade the potion. Ex: Speed I ---> Speed II
        if (!data.isUpgraded()) {
          data = new PotionData(data.getType(), false, true);
          meta.setBasePotionData(data);
        }

        // Make it a splash potion
        item.setType(Material.SPLASH_POTION);
        item.setItemMeta(meta);

      }

    }

  }

}
