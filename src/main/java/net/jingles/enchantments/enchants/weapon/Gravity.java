package net.jingles.enchantments.enchants.weapon;

import net.jingles.enchantments.Enchant;
import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchants.CustomEnchant;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.stream.Collectors;

@Enchant(name = "Gravity", key = "gravity", levelRequirement = 30, targetItem = EnchantmentTarget.BOW, maxLevel = 3,
        description = "When an arrow launched by a bow with Gravity hits a block or entity, the arrow creates a gravitational " +
                "field in a 5 (+3 per additional level) block radius that pulls entities towards itself. The arrow then " +
                "detonates, dealing an additional 7 damage and slowing all entities caught in the blast for 5 seconds per level")

public class Gravity extends CustomEnchant {

  public Gravity(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(Inventory inventory, Event event) {
    ItemStack bow = getItem(inventory);
    return bow != null && hasEnchantment(bow) && !Enchantments.getCooldownManager()
        .hasCooldown(((Player) inventory.getHolder()).getUniqueId(), this);
  }

  @EventHandler
  public void onArrowHit(ProjectileHitEvent event) {
    if (!(event.getEntity() instanceof Arrow) ||
            !(event.getEntity().getShooter() instanceof Player)) return;

    Player player = (Player) event.getEntity().getShooter();
    if (!canTrigger(player.getInventory(), event)) return;

    Location hitLoc = event.getHitEntity() != null ? event.getHitEntity().getLocation() :
            event.getHitBlock().getLocation();

    int level = getItem(player.getInventory()).getItemMeta().getEnchantLevel(this);
    int radius = 5 + (3 * level);

    //Gets nearby entities within the radius that are living, not players, and are not tamed by the enchant owner.
    Set<Monster> targets = hitLoc.getWorld().getNearbyEntities(hitLoc, radius, radius, radius).stream()
            .filter(entity -> entity instanceof Monster)
            .map(entity -> (Monster) entity)
            .collect(Collectors.toSet());

    //Quickly pull the target towards the hit location.
    targets.forEach(target -> {
      Vector difference = hitLoc.toVector().subtract(target.getLocation().toVector());
      target.setVelocity(difference.multiply(0.45).setY(Math.min(1.5F, difference.getY())));
    });

    Enchantments plugin = (Enchantments) Bukkit.getPluginManager().getPlugin("Enchantments");

    //Detonate the arrow about half a second after pulling the entities towards it.
    plugin.getServer().getScheduler().runTaskLater(plugin, () -> targets.forEach(target -> {
      hitLoc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, hitLoc, 0, 0, 0, 1);
      target.damage(7.0F);
    }), 12L);

    int duration = (5 * level) * 20;
    PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, duration, 1, false, true);
    targets.forEach(target -> target.addPotionEffect(slow));

    Enchantments.getCooldownManager().addCooldown(player.getUniqueId(), this, getCooldown(), getTimeUnit());

  }

}
