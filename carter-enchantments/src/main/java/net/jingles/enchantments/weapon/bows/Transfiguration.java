package net.jingles.enchantments.weapon.bows;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.statuseffect.EntityStatusEffect;
import net.jingles.enchantments.statuseffect.container.EntityEffectContainer;
import net.jingles.enchantments.util.ParticleUtil;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Enchant(name = "Transfiguration", key = "transfiguration", cooldown = 20, enchantChance = 0.15,
    maxLevel = 3, levelRequirement = 30, targetItem = EnchantmentTarget.ALL, targetGroup = TargetGroup.BOWS,
    description = "There is a 20% chance per level that any entity shot with a Transfiguration bow will " +
        "be transformed into a random docile mob. The transformation lasts 15 seconds, during which the mob " +
        "will still drop its original drops. This enchantment does not work on boss mobs.")

public class Transfiguration extends CustomEnchant {

  public Transfiguration(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(Inventory inventory, Event event) {
    Player player = (Player) ((ProjectileHitEvent) event).getEntity().getShooter();
    ItemStack bow = getItem(inventory);

    return bow != null && hasEnchantment(bow) && !Enchantments.getCooldownManager()
        .hasCooldown(player, this);
  }

  @EventHandler
  public void onArrowHit(ProjectileHitEvent event) {

    if (event.getHitEntity() == null || !(event.getHitEntity() instanceof LivingEntity) ||
        !(event.getEntity().getShooter() instanceof Player)) return;

    Player player = (Player) event.getEntity().getShooter();
    if (!canTrigger(player.getInventory(), event)) return;

    LivingEntity entity = (LivingEntity) event.getHitEntity();
    if (entity.isDead() || entity.getClass().isAssignableFrom(Boss.class)) return;

    Enchantments.getStatusEffectManager().add(new TransfigurationEffect(entity));
    addCooldown(player);
  }

  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {
    // We're looking for the original entity, which will be a passenger of a transfigured entity.
    if (event.getEntity().getPassengers().isEmpty()) return;

    // Kill each passenger that has the TransfigurationEffect.
    event.getEntity().getPassengers().stream()
        .filter(passenger -> {

          EntityEffectContainer effectContainer = Enchantments.getStatusEffectManager()
              .getEntityContainer(passenger.getUniqueId()).orElse(null);

          return effectContainer != null && effectContainer.hasEffect(TransfigurationEffect.class);

          // We already know the entity is instanceof LivingEntity if they have the TransfigurationEffect.
        }).map(entity -> (LivingEntity) entity)
        .forEach(entity -> {
          entity.setInvulnerable(false);
          entity.setHealth(0);
        });

    // Clears the drops of the transfigured entity, so that only the drops of the
    // original entity are dropped.
    event.getDrops().clear();
  }

  private class TransfigurationEffect extends EntityStatusEffect {

    private LivingEntity mount;

    private TransfigurationEffect(LivingEntity target) {
      super(target, Transfiguration.this, 15 * 20, 1);
    }

    @Override
    public void start() {

      // Apply invisibility and invulnerability.
      PotionEffect invisibility = new PotionEffect(PotionEffectType.INVISIBILITY, getMaxTicks(), 3, false, false);
      getTarget().addPotionEffect(invisibility);
      getTarget().setInvulnerable(true);

      // Get a list of possible entities to transfigure the original into.
      List<EntityType> options = Stream.of(EntityType.values())
          .filter(type -> type.isSpawnable() && type.isAlive())
          .filter(type -> type.getEntityClass() != null && !type.getEntityClass().isAssignableFrom(Monster.class))
          .collect(Collectors.toList());

      // Shuffle the options to get a random EntityType and apply particle effects.
      Collections.shuffle(options);
      Particle.DustOptions data = new Particle.DustOptions(Color.WHITE, 2);
      ParticleUtil.sphere(getTarget().getLocation(), getTarget().getHeight(), Particle.REDSTONE, data);

      // Spawn the new transfigured entity
      this.mount = (LivingEntity) getTarget().getWorld().spawnEntity(getTarget().getLocation(), options.get(0));

      // Set the transfigured entity's health to the original's. Add the original as a passenger to the transfigured.
      mount.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(getTarget()
          .getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
      mount.setHealth(getTarget().getHealth());
      mount.addPassenger(getTarget());
    }

    @Override
    public void effect() {

      //Cancels the effect is the mount has been killed.
      if (mount == null || mount.isDead()) {
        this.stop();
        return;
      }

      //Ensures that the target stays mounted on the entity
      if (!mount.getPassengers().contains(getTarget())) mount.addPassenger(getTarget());
    }

    @Override
    public void stop() {
      // The EntityDeathEvent handles what happens if the mount is dead.
      if (mount != null && !mount.isDead()) {
        getTarget().removePotionEffect(PotionEffectType.INVISIBILITY);
        getTarget().setInvulnerable(false);
        getTarget().setHealth(mount.getHealth());
        mount.getPassengers().forEach(mount::removePassenger);
        mount.remove();
      }

      super.stop();
    }

  }

}
