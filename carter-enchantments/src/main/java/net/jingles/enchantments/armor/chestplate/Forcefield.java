package net.jingles.enchantments.armor.chestplate;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.util.ParticleUtil;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Enchant(name = "Forcefield", key = "forcefield", maxLevel = 3, targetItem = EnchantmentTarget.ARMOR_TORSO,
    levelRequirement = 13, description = "When a projectile nears the owner, there is a 10 (+10% for each " +
    "level) percent chance for the arrow to be deflected")

public class Forcefield extends CustomEnchant {

  public Forcefield(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public boolean canEnchantItem(@NotNull ItemStack item) {
    if (item.getType().toString().contains("HORSE_ARMOR")) return true;
    else return super.canEnchantItem(item);
  }

  @Override
  public boolean canTrigger(LivingEntity entity) {
    ItemStack enchanted = getItem(entity);
    if (enchanted == null || !hasEnchantment(enchanted)) return false;
    return (Math.random() * 100) <= (enchanted.getItemMeta().getEnchantLevel(this) * 10);
  }

  @EventHandler
  public void onProjectileHit(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Projectile)) return;

    Entity hit = event.getEntity();
    if (!(hit instanceof Player) || !canTrigger((Player) hit)) return;

    event.setCancelled(true);
    Projectile projectile = (Projectile) event.getDamager();
    projectile.setVelocity(projectile.getVelocity().multiply(-1));

    Particle.DustOptions options = new Particle.DustOptions(Color.GRAY, 2);
    ParticleUtil.sphere(hit.getLocation(), 3, Particle.REDSTONE, options);

  }

}
