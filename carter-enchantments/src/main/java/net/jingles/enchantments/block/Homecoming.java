package net.jingles.enchantments.block;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.persistence.EnchantTeam;
import net.jingles.enchantments.statuseffect.LocationStatusEffect;
import net.jingles.enchantments.statuseffect.context.TileEntityContext;
import net.jingles.enchantments.util.EnchantUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@Enchant(name = "Homecoming", key = "homecoming", enchantChance = 0.47, targetGroup = TargetGroup.CAMPFIRE,
  description = "The enchanted campfire has a radius of 20 blocks per level. Every 5 seconds, all players " +
      "within the fire's range will receive Regeneration 1 and Saturation 1 potion effects as a " +
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
    TileEntityContext context = new TileEntityContext(tile, this);
    HomecomingEffect effect = new HomecomingEffect(context, tile.getLocation(), getLevel(tile));
    Enchantments.getStatusEffectManager().add(effect);
  }

  private static class HomecomingEffect extends LocationStatusEffect {

    private final PotionEffect regen;
    private final PotionEffect saturation;
    private final double radius;

    private HomecomingEffect(TileEntityContext context, @NotNull Location location, int level) {
      super(context, Integer.MAX_VALUE, 100, location);

      int duration = level * (10 * 20);

      this.radius = level * 20;
      this.regen = new PotionEffect(PotionEffectType.REGENERATION, duration, 1, false, false);
      this.saturation = new PotionEffect(PotionEffectType.SATURATION, duration, 1, false, false);
    }

    @Override
    public void effect() {

      EnchantTeam team = EnchantUtils.getEnchantTeam(((TileEntityContext) getContext()).getTrigger());

      getLocation().getWorld().getNearbyEntities(getLocation(), radius, radius, radius).stream()
          .filter(e -> e instanceof LivingEntity && team.isTeamed(e))
          .map(e -> (LivingEntity) e)
          .forEach(entity -> {
            entity.addPotionEffect(regen);
            entity.addPotionEffect(saturation);
          });

    }

  }

}
