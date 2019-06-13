package net.jingles.enchantments.armor;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@Enchant(name = "Forcefield", key = "forcefield", maxLevel = 3, targetItem = EnchantmentTarget.ARMOR_TORSO,
    levelRequirement = 13, targetGroup = TargetGroup.ALL_ARMOR, description = "When a projectile nears " +
    "the owner, there is a 10 (+10% for each level) percent chance for the arrow to be deflected")

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
  public boolean canTrigger(Inventory inventory, Event event) {
    ItemStack enchanted = getItem(inventory);
    if (enchanted == null || !hasEnchantment(enchanted)) return false;

    return (Math.random() * 100) <= (enchanted.getItemMeta().getEnchantLevel(this) * 10);
  }

  @EventHandler
  public void onProjectileHit(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Projectile)) return;

    Entity hit = event.getEntity();

    if (!(hit instanceof InventoryHolder) ||
        !canTrigger(((InventoryHolder) hit).getInventory(), event)) return;

    event.setCancelled(true);
    Projectile projectile = (Projectile) event.getDamager();
    projectile.setVelocity(projectile.getVelocity().multiply(-1));
    sphere(hit.getLocation(), 3, Particle.REDSTONE, Color.GRAY);

  }

  private void sphere(Location location, double radius, Particle particle, Color color) {
    int d = 3;
    for (double inc = (Math.random() * Math.PI) / 5; inc < Math.PI; inc += Math.PI / d) {
      for (double azi = (Math.random() * Math.PI) / d; azi < 2 * Math.PI; azi += Math.PI / d) {
        double[] spher = new double[2];
        spher[0] = inc;
        spher[1] = azi;
        Location e = location.clone().add(spherToVec(spher, radius));

        Particle.DustOptions options = new Particle.DustOptions(color, 1);
        location.getWorld().spawnParticle(particle, e, 0, 0, 0, 0, 1, options);
      }
    }
  }

  private Vector spherToVec(double[] spher, double radius) {
    double inc = spher[0];
    double azi = spher[1];
    double x = radius * Math.sin(inc) * Math.cos(azi);
    double z = radius * Math.sin(inc) * Math.sin(azi);
    double y = radius * Math.cos(inc);
    return new Vector(x, y, z);
  }

}
