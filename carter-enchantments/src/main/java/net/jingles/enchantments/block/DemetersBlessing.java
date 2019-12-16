package net.jingles.enchantments.block;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
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

@Enchant(name = "Demeter's Blessing", key = "demeters_blessing", enchantChance = 0.35, maxLevel = 3, targetGroup = TargetGroup.BANNER, description = "For every crop or sapling within a 5 block (per level) radius, the crop's age will increase by one every 20 seconds.")
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

    public DemetersBlessingEffect(TileState tile) {
      super(DemetersBlessing.this, Integer.MAX_VALUE, 400, tile.getLocation());

      int level = getLevel(tile);
      this.radius = level * 5;

    }

    @Override
    public void effect() {

      for (Block block : getCropsInRadius(getLocation(), radius)) {

        BlockData data = block.getBlockData();

        if (data instanceof Sapling) {
          Sapling sapling = (Sapling) data;
          sapling.setStage(Math.min(sapling.getStage() + 1, sapling.getMaximumStage()));
        } else {
          Ageable ageable = (Ageable) data;
          ageable.setAge(Math.min(ageable.getAge() + 1, ageable.getMaximumAge()));
        }

        block.setBlockData(data);

      }

    }

    private ArrayList<Block> getCropsInRadius(Location center, int radius) {

      ArrayList<Block> crops = new ArrayList<>();

      for (int x = center.getBlockX() - radius; x <= center.getBlockX() + radius; x++) {
        for (int y = center.getBlockY() - radius; y <= center.getBlockY() + radius; y++) {
          for (int z = center.getBlockZ() - radius; z <= center.getBlockZ() + radius; z++) {
            Block block = center.getWorld().getBlockAt(x, y, z);
            if (block.getBlockData() instanceof Ageable || block.getBlockData() instanceof Sapling)
              crops.add(block);
          }
        }
      }

      return crops;

    }

  }

}