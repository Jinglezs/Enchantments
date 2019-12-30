package net.jingles.enchantments.weapon.bows;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

@Enchant(name = "Supersede", key = "supersede", cursed = true, targetGroup = TargetGroup.BOWS,
  description = "There is a 20% chance per level that the shooter will be switch places with any " +
      "entity shot with this bow.")
public class Supersede extends CustomEnchant {

  public Supersede(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(@NotNull LivingEntity entity) {
    return hasEnchantment(getItem(entity));
  }

  @EventHandler
  public void onArrowHit(EntityDamageByEntityEvent event) {

    if (!(event.getDamager() instanceof Arrow)) return;

    ProjectileSource source = ((Arrow) event.getDamager()).getShooter();

    if (!(source instanceof LivingEntity) || !canTrigger((LivingEntity) source)) return;

    LivingEntity shooter = (LivingEntity) source;

    double chance = (getLevel(getItem(shooter)) * 20) / 100D;
    if (Math.random() > chance) return;

    Entity hit = event.getEntity();
    Location hitLoc = hit.getLocation().clone();

    hit.teleport(shooter.getLocation());
    shooter.teleport(hitLoc);

    shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 3F, 1F);

  }

}
