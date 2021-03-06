package net.jingles.enchantments.weapon;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.statuseffect.context.ItemEffectContext;
import net.jingles.enchantments.statuseffect.entity.EntityStatusEffect;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Enchant(name = "Backstab", key = "backstab", levelRequirement = 30, enchantChance = 0.15, maxLevel = 1,
    cooldown = 7, targetItem = EnchantmentTarget.WEAPON, description = "If the user is attacked " +
    "while holding crouch and the enchanted blade, then they will teleport behind the attacking " +
    "entity and their next melee attack will deal 150% of the damage initially dealt to them.")

public class Backstab extends CustomEnchant {

  public Backstab(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(@NotNull LivingEntity entity) {

    if (!(entity instanceof Player)) return false;

    ItemStack sword = getItem(entity);

    return ((Player) entity).isSneaking() && sword != null && hasEnchantment(sword) &&
        !Enchantments.getCooldownManager().hasCooldown(entity, this);
  }

  @EventHandler
  public void onEntityDamage(EntityDamageByEntityEvent event) {

    if (event.getDamager() instanceof Player) {
      // If the attacking player has the BackstabEffect and the entity they are attacking matches,
      // then apply the damage bonus.
      Enchantments.getStatusEffectManager().getEntityContainer(event.getDamager().getUniqueId())
          .ifPresent(container -> {

            if (!container.hasEffect(BackstabEffect.class)) return;
            BackstabEffect effect = (BackstabEffect) container.getEffectsBySource(this)
                .stream().findFirst().get();

            if (event.getEntity().getUniqueId().equals(effect.getAttacker())) {
              event.setDamage(event.getDamage() + effect.getDamage());
              container.removeEffects(this);
            }

          });

    }

    if (!(event.getEntity() instanceof Player) ||
        !canTrigger((Player) event.getEntity())) return;

    Player player = (Player) event.getEntity();
    LivingEntity attacker = getAttacker(event.getDamager());
    if (attacker == null || player.equals(attacker)) return;

    // Inverts the attacker's direction and normalizes it so that the length is one block, then
    // halves it so that the player is placed half a block behind the attacker.
    Location teleport = attacker.getLocation().add(attacker.getLocation().getDirection().normalize().multiply(-0.5));
    teleport.setYaw(attacker.getLocation().getYaw());
    teleport.setPitch(attacker.getLocation().getPitch());
    player.teleport(teleport);

    double damage = event.getDamage() * 1.5;
    ItemEffectContext context = new ItemEffectContext(player, getItem(player), this);
    Enchantments.getStatusEffectManager().add(new BackstabEffect(context, player, attacker.getUniqueId(), damage));
    addCooldown(player);
  }

  private LivingEntity getAttacker(Entity entity) {

    if (entity instanceof Projectile) {
      ProjectileSource source = ((Projectile) entity).getShooter();
      return (source instanceof LivingEntity) ? (LivingEntity) entity : null;
    }

    return entity instanceof LivingEntity ? (LivingEntity) entity : null;
  }

  private static class BackstabEffect extends EntityStatusEffect {

    private final UUID attacker;
    private final double damage;

    public BackstabEffect(ItemEffectContext context, Player player, UUID attacker, double damage) {
      super(player, context, 100, 100);
      this.attacker = attacker;
      this.damage = damage;
    }

    @Override
    public void effect() {
    }

    public double getDamage() {
      return this.damage;
    }

    public UUID getAttacker() {
      return this.attacker;
    }

  }

}
