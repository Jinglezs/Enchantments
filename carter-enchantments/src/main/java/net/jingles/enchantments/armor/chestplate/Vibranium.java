package net.jingles.enchantments.armor.chestplate;

import com.google.common.primitives.Doubles;
import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.statuseffect.LocationStatusEffect;
import net.jingles.enchantments.util.ParticleUtil;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Enchant(name = "Vibranium", key = "vibranium", enchantChance = 0.45, levelRequirement = 30,
    cooldown = 1, timeUnit = TimeUnit.MINUTES, targetItem = EnchantmentTarget.ARMOR,
    description = "The kinetic energy of all attacks are absorbed by the wearer's armor if they " +
        "have a complete set. Once the armor reaches its maximum capacity of 25 damage per level, " +
        "the kinetic energy is released, throwing away and damaging nearby enemies in a 7 block radius.")

public class Vibranium extends CustomEnchant {

  private final Map<UUID, Double> kineticEnergy = new HashMap<>();

  public Vibranium(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(LivingEntity entity) {

    if (Enchantments.getCooldownManager().hasCooldown(entity, this)) return false;

    EntityEquipment equipment = entity.getEquipment();
    if (equipment == null) return false;

    // Only returns true if every armor piece is present and has the enchantment.
    return Stream.of(equipment.getArmorContents())
        .noneMatch(item -> item == null || !hasEnchantment(item));
  }

  @EventHandler
  public void onEntityDamage(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Player) ||
        !canTrigger((Player) event.getEntity())) return;

    Player player = (Player) event.getEntity();
    UUID id = player.getUniqueId();
    // The enchantment level is the average of each armor piece's level.
    int level = Stream.of(player.getInventory().getArmorContents())
        .collect(Collectors.averagingInt(this::getLevel)).intValue();

    // Clamps the energy value so that it lies between 0.0 and 25 damage per level.
    // Sadly Java does not have Math.clamp() smh my head... Guava will have to do.
    kineticEnergy.put(id, kineticEnergy.getOrDefault(id, 0D) + event.getDamage());
    double heldEnergy = Doubles.constrainToRange(kineticEnergy.get(id), 0D, level * 25);

    Particle.DustOptions options = new Particle.DustOptions(Color.PURPLE, 0.5f);
    ParticleUtil.sphere(player.getLocation(), 2, Particle.REDSTONE, options);

    // Should be equivalent at max capacity due to the clamp method.
    if (heldEnergy == level * 25) {
      kineticEnergy.remove(player.getUniqueId());
      Enchantments.getStatusEffectManager().add(new VibraniumReleaseEffect(player, player.getLocation(), heldEnergy));
      Enchantments.getCooldownManager().addCooldown(player, this, getCooldown(), getTimeUnit());
    }

    // Cancel the damage because it was "absorbed." We do not want to cancel the entire
    // event because the player can still receive knock backs and stuff.
    event.setDamage(0D);
  }

  private class VibraniumReleaseEffect extends LocationStatusEffect {

    private final List<LivingEntity> affected = new ArrayList<>();
    private final Particle.DustOptions options = new Particle.DustOptions(Color.PURPLE, 1);
    private final double damage;
    private double radius;

    public VibraniumReleaseEffect(Player owner, Location location, double damage) {
      super(Vibranium.this, 10, 1, location);
      this.damage = damage;
      affected.add(owner);
    }

    @Override
    public void start() {
      getLocation().getWorld().playSound(getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2, 2f);
    }

    @Override
    public void effect() {
      // Creates a sphere effect that grows each tick to simulate an explosion's quick expansion.
      ParticleUtil.sphere(getLocation(), ++radius, Particle.REDSTONE, options);
      // Damages and knocks away nearby enemies that have not been already. Adds them to the affected list.
      getLocation().getWorld().getNearbyEntities(getLocation(), radius, radius, radius,
          entity -> entity instanceof LivingEntity && !affected.contains(entity))
          .stream().map(entity -> (LivingEntity) entity)
          .forEach(entity -> {
            entity.damage(damage);
            entity.setVelocity(ParticleUtil.bounceFromSource(entity.getVelocity(), getLocation()).setY(0.95));
            affected.add(entity);
          });

    }

  }

}
