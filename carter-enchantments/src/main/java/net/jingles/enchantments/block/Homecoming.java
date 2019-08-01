package net.jingles.enchantments.block;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.statuseffect.LocationStatusEffect;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@Enchant(name = "Homecoming", key = "homecoming", enchantChance = 0.47, targetGroup = TargetGroup.CAMPFIRE,
  description = "The enchanted campfire has a radius of 20 blocks per level. Every 5 seconds, all players " +
      "within the fire's range will receive Regeneration 2 and Saturation 2 potion effects as a " +
      "homecoming gift.")

public class Homecoming extends BlockEnchant {

  public Homecoming(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(@NotNull TileState tile) {
    return false;
  }

  @Override
  public void onChunkLoad(TileState tile) {
    HomecomingEffect effect = new HomecomingEffect(tile, Integer.MAX_VALUE, tile.getLocation());
    Enchantments.getStatusEffectManager().add(effect);
  }

  private class HomecomingEffect extends LocationStatusEffect {

    private final PotionEffect regen;
    private final PotionEffect saturation;
    private final double radius;

    public HomecomingEffect(TileState tile, int maxTicks, @NotNull Location location) {
      super(Homecoming.this, maxTicks, 100, location);

      int level = getLevel(tile);
      int duration = level * (10 * 20);

      this.radius = level * 20;
      this.regen = new PotionEffect(PotionEffectType.REGENERATION, duration, 2, false, false);
      this.saturation = new PotionEffect(PotionEffectType.SATURATION, duration, 2, false, false);
    }

    @Override
    public void effect() {

      getLocation().getWorld().getNearbyEntities(getLocation(), radius, radius, radius).stream()
          .filter(entity -> entity instanceof Player)
          .map(entity -> (Player) entity)
          .forEach(entity -> {
            entity.addPotionEffect(regen);
            entity.addPotionEffect(saturation);
          });

    }

  }

}
