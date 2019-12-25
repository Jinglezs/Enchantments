package net.jingles.enchantments.tools;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.persistence.EnchantTeam;
import net.jingles.enchantments.projectile.HomingProjectile;
import net.jingles.enchantments.statuseffect.container.EntityEffectContainer;
import net.jingles.enchantments.statuseffect.context.ItemEffectContext;
import net.jingles.enchantments.statuseffect.entity.EntityStatusEffect;
import net.jingles.enchantments.util.EnchantUtils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Enchant(name = "Reinforcement", key = "reinforcement", targetItem = EnchantmentTarget.ALL, cooldown = 30,
    targetGroup = TargetGroup.SHIELD, description = "Summons a storm spirit to the player's side that shoots " +
    "homing projectiles at nearby hostile mobs once every second. Reinforcements last 7 seconds for each level " +
    "and their projectiles deal 3 damage per level.")

public class Reinforcement extends CustomEnchant {

  public Reinforcement(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(@NotNull LivingEntity entity) {

    ItemStack shield = getItem(entity);
    if (!hasEnchantment(shield)) return false;

    Optional<EntityEffectContainer> container = Enchantments.getStatusEffectManager()
        .getEntityContainer(entity.getUniqueId());

    return container.map(entityEffectContainer -> !entityEffectContainer.hasEffect(ReinforcementEffect.class) &&
        !Enchantments.getCooldownManager().hasCooldown(entity, this)).orElse(true);

  }

  @EventHandler
  public void onShieldUse(PlayerInteractEvent event) {

    Player player = event.getPlayer();

    if (event.getItem() == null || event.getItem().getType() != Material.SHIELD ||
        !canTrigger(player)) return;

    ItemStack item = getItem(player);

    // The duration increases by 7 seconds per level.
    int level = getLevel(item);
    int duration = level * (7 * 20);
    int damage = level * 3;

    ItemEffectContext context = new ItemEffectContext(player, item, this);
    Enchantments.getStatusEffectManager().add(new ReinforcementEffect(context, player, duration, damage));
  }

  private class ReinforcementEffect extends EntityStatusEffect {

    private final EnchantTeam team;
    private final double damage;
    private long lastFireTime;

    private ReinforcementEffect(ItemEffectContext context, Player target, int maxTicks, double damage) {
      super(target, context, maxTicks, 1);
      this.damage = damage;
      this.team = EnchantUtils.getEnchantTeam(target);
    }

    @Override
    public void effect() {

      Location playerLoc = getTarget().getLocation();
      // Circles to the left and right of the player's head?
      // Circle particle effect stuff
      Location circle = getLocation(playerLoc, 0.75);
      circle.setDirection(getTarget().getEyeLocation().getDirection());

      // Display a particle at each point on the circle
      for (int i = 0; i < 360; i++) {
        // Calculate the x and z positions.
        double radians = Math.toRadians(i);
        double x = Math.cos(radians) * 0.05;
        double z = Math.sin(radians) * 0.05;

        // FYI: I originally wanted it to be a flat circle that faced the player's eye direction.
        // Unfortunately I'm trash at math and it never worked out that way. But now it spasms
        // out and rotates 50 times whenever the player's moves their head and its funny so
        // I'm keeping it this way :D It kinda looks like a storm spirit :>

        Vector rotated = new Vector(x, 0, z);
        rotated.rotateAroundX(circle.getPitch());
        rotated.rotateAroundY(circle.getYaw());
        rotated.normalize().multiply(0.002);

        // Rotate the circle on the x and y axises.
        // Vector rotated = new Vector(x, 0, z).rotateAroundX(75);
        // rotated.rotateAroundY(30).normalize().multiply(0.009);

        getTarget().getWorld().spawnParticle(Particle.BUBBLE_POP, circle.add(rotated), 0, 0, 0, 0);
      }

      if (lastFireTime + 1000L > System.currentTimeMillis()) return;
      // Fire at the closest nearby enemy and set the lastFireTime.

      Optional<LivingEntity> closest = getTarget().getNearbyEntities(15, 15, 15).stream()
          .filter(entity -> entity instanceof LivingEntity && !entity.isDead() && !team.isTeamed(entity))
          .map(entity -> (LivingEntity) entity)
          .min((e1, e2) -> {
            Location player = getTarget().getLocation();
            return Double.compare(e1.getLocation().distanceSquared(player), e2.getLocation().distanceSquared(player));
          });

      if (closest.isPresent()) {

        LivingEntity entity = closest.get();
        entity.getWorld().playSound(getTarget().getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1F, 1F);

        // Fire a projectile from the circle.
        new HomingProjectile((Player) getTarget(), closest.get())
            .withAccuracy(0.35)
            .withDistanceThreshold(7)
            .setStartLocation(circle)
            .setDirection(circle.getDirection().normalize())
            .withHitbox(0.5, 0.5, 0.5)
            .setParticle(Particle.SWEEP_ATTACK)
            .setMaxDistance(200)
            .withEntityFilter(target -> target.getUniqueId().equals(entity.getUniqueId()))
            // Only collide with blocks if they are not passable.
            .withBlockFilter(block -> !block.isPassable())
            .onEntityHit((projectile, target) -> {
              projectile.cancel();
              ((LivingEntity) target).damage(damage);
            })
            .onBlockHit((projectile, block) -> projectile.cancel())
            .launch();

        this.lastFireTime = System.currentTimeMillis();

      }

    }

    @Override
    public void stop() {
      super.stop();
      addCooldown((Player) getTarget());
    }

    // Gets the location that will be the center of the circles.
    private Location getLocation(Location origin, double distance) {
      // Gets the direction that the player is looking
      Vector direction = origin.getDirection().normalize();
      // Gets the location directly behind the player, multiplied by the provided distance
      Location behind = origin.clone().add(direction.clone().multiply((distance < 0) ? distance : -distance));
      // Gets the direction perpendicular to both the UP vector and the player's direction.
      Vector perpendicular = direction.getCrossProduct(new Vector(0, 1, 0)).multiply(distance);
      // Add it to the "behind" location and increase the height by a couple blocks.
      return behind.add(perpendicular).add(0, 2.3, 0);
    }

  }

}
