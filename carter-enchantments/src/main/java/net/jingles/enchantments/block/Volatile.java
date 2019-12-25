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
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Enchant(name = "Volatile", key = "volatile", cursed = true, maxLevel = 1, targetGroup = TargetGroup.BREWING_STAND,
    description = "This brewing stand has been cursed by creeper mojo, causing all of its potions " +
        "to become volatile. Anything it brews becomes a splash potion and is upgraded by one level.")
public class Volatile extends BlockEnchant {

  public Volatile(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean canTrigger(@Nullable TileState tile) {
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

  // After a delay of 5 ticks, this will replace all potions with their splash potion
  // counterparts. Additionally, all of the potions will be upgraded.
  private static class VolatilePotionDetectEffect extends LocationStatusEffect {

    private final BrewingStand stand;

    private VolatilePotionDetectEffect(TileEntityContext context) {
      super(context, 5, 1, 5, context.getTrigger().getLocation());
      this.stand = (BrewingStand) context.getTrigger();
    }

    @Override
    public void effect() {

      BrewerInventory inventory = stand.getInventory();
      ItemStack[] contents = inventory.getContents();

      // Gets the indexes of all results that are potions or splash potions and
      // collects them into a set
      Set<Integer> indexes = IntStream.range(0, 3)
          .filter(i -> {

            ItemStack item = contents[i];
            return item != null && (item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION);

          }).boxed().collect(Collectors.toSet());

      // Iterate over the potion at each index and replace them as necessary
      for (int index : indexes) {

        // Get the potion info
        ItemStack potion = inventory.getItem(index);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        PotionData data = meta.getBasePotionData();

        // Upgrade the potion if possible. Ex: Speed I ---> Speed II
        if (data.getType().isUpgradeable() && !data.isUpgraded()) {
          data = new PotionData(data.getType(), false, true);
          meta.setBasePotionData(data);
        }

        // Create a splash potion with the same potion data as the original
        if (potion.getType() != Material.SPLASH_POTION)
          potion = new ItemStack(Material.SPLASH_POTION, 1);

        // Set the new item meta and replace the potion with the updated one
        potion.setItemMeta(meta);
        inventory.setItem(index, potion);

      }

      // Take one item from the ingredient slot.

      ItemStack ingredient = inventory.getIngredient();

      if (ingredient != null && ingredient.getType() != Material.AIR) {

        int newAmount = ingredient.getAmount() - 1;

        if (newAmount < 0) inventory.setIngredient(null);
        else ingredient.setAmount(newAmount);

      }

    }

  }

}
