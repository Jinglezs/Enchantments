package net.jingles.enchantments.block;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.enchantments.Enchantment;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.statuseffect.LocationStatusEffect;
import net.jingles.enchantments.enchant.TargetGroup;

@Enchant(name = "Demeter's Blessing", key = "demeters_blessing", enchantChance = 0.35, targetGroup = TargetGroup.BANNER,
  description = "For every crop or sapling within a 10 block (per level) radius, there is a 10% chance per level that " +
    "the crop will will be fully grown every 30 seconds.")
public class DemetersBlessing extends BlockEnchant {

  public DemetersBlessing(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean canTrigger(TileState tile) {
    return false;
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public void onChunkLoad(TileState tile) {
    DemetersBlessingEffect effect = new DemetersBlessingEffect(tile);
    Enchantments.getStatusEffectManager().add(effect);
  }

  private class DemetersBlessingEffect extends LocationStatusEffect {

    private final int radius;
    private final int chance;

    public DemetersBlessingEffect(TileState tile) {
      super(DemetersBlessing.this, Integer.MAX_VALUE, 20 * 20, tile.getLocation());

      int level = getLevel(tile);
      this.radius = level * 10;
      this.chance = (level * 10);

    }

    @Override
        public void effect() {
            
          ThreadLocalRandom random = ThreadLocalRandom.current();

          for (BlockData data : getCropsInRadius(getLocation(), radius)) {

            if (random.nextInt(100) >= chance) return;
            
            if (data instanceof Sapling) {
              Sapling sapling = (Sapling) data;
              sapling.setStage(sapling.getMaximumStage());
            } else {
              Ageable ageable = (Ageable) data;
              ageable.setAge(ageable.getMaximumAge());
            }

          }

        }

    private ArrayList<BlockData> getCropsInRadius(Location center, int radius) {

      ArrayList<BlockData> crops = new ArrayList<>();

      for (double x = center.getX() - radius; x <= center.getX() + radius; x++) {
        for (double y = center.getY() - radius; y <= center.getY() + radius; y++) {
          for (double z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
            BlockData data = center.getWorld().getBlockAt((int) x, (int) y, (int) z).getBlockData();
            if (data instanceof Ageable || data instanceof Sapling) crops.add(data);
          }
        }
      }

      return crops;

    }

  }

}