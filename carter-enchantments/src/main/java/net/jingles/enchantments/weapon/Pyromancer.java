package net.jingles.enchantments.weapon;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.projectile.HomingProjectile;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

@Enchant(name = "Pyromancer", key = "pyromancer", targetItem = EnchantmentTarget.WEAPON, cooldown = 10, enchantChance = 0.25,
    description = "When the wielder right clicks while holding an enchanted blade and looking at an entity, " +
        "homing flame projectiles are launched at the target. Upon finding their target, the entity is dealt " +
        "5 (+2 per level) damage and ignited for 5 (+2 per level) seconds.")

public class Pyromancer extends CustomEnchant {

  public Pyromancer(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(@NotNull LivingEntity entity) {
    ItemStack sword = getItem(entity);
    return sword != null && hasEnchantment(sword) &&
        !Enchantments.getCooldownManager().hasCooldown(entity, this);
  }

  @EventHandler
  public void onEntityTarget(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_AIR ||
        !canTrigger(event.getPlayer())) return;

    Player player = event.getPlayer();
    RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(),
        player.getEyeLocation().getDirection(), 120, entity -> entity instanceof LivingEntity
            && !entity.equals(player));

    final LivingEntity target = result != null ? (LivingEntity) result.getHitEntity() : null;
    if (target == null) return;

    int level = getLevel(getItem(player));
    float damage = 5 + (level * 2);
    int fireTicks = (5 + (level * 2)) * 20;

    new HomingProjectile(player, target)
        .withAccuracy(0.35)
        .withDistanceThreshold(7)
        .setStartLocation(player.getEyeLocation())
        .setDirection(player.getEyeLocation().getDirection())
        .setParticle(Particle.REDSTONE)
        .withParticleData(new Particle.DustOptions(Color.RED, 2))
        .withHitbox(0.75, 0.75, 0.75)
        .withEntityFilter(entity -> entity instanceof LivingEntity)
        .setMaxDistance(300)
        .onBlockHit(((projectile, block) -> {

          if (!block.isLiquid()) {
            //Sets the block on fire
            Block up = block.getRelative(BlockFace.UP);
            up.setType(Material.FIRE);
            up.getState().update();
          }

          projectile.cancel();

        }))
        .onEntityHit((projectile, entity) -> {

          LivingEntity hit = (LivingEntity) entity;
          hit.setFireTicks(fireTicks);
          hit.damage(damage);
          projectile.cancel();

        }).launch();

    addCooldown(player);

  }

}
