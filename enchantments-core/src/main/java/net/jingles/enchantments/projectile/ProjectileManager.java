package net.jingles.enchantments.projectile;

import net.jingles.enchantments.Enchantments;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class ProjectileManager extends BukkitRunnable {

  private final Set<Projectile> projectiles = new ConcurrentSkipListSet<>();

  public ProjectileManager(@NotNull Enchantments plugin) {
    runTaskTimer(plugin, 0, 1);
  }

  public void register(@NotNull Projectile projectile) {
    projectiles.add(projectile);
  }

  public void unregister(@NotNull Projectile projectile) {
    projectiles.remove(projectile);
  }

  @NotNull
  public Set<Projectile> getNearbyProjectiles(@NotNull Location center, double radius) {
    return getNearbyProjectiles(center, radius, radius, radius);
  }

  @NotNull
  public Set<Projectile> getNearbyProjectiles(@NotNull Location center, double x, double y, double z) {
    BoundingBox boundingBox = BoundingBox.of(center, x, y, z);
    return projectiles.stream()
        .filter(projectile -> boundingBox.overlaps(projectile.getHitbox()))
        .collect(Collectors.toSet());
  }

  public void bounceProjectileFromSource(@NotNull Projectile projectile, @NotNull Location source) {
    Vector direction = source.toVector().subtract(projectile.getLocation().toVector()).normalize();
    double multiplier = 1 + (Math.random() * 5);

    projectile.setDirection(direction.multiply((projectile.getDirection().dot(direction)))
        .multiply(-2).add(projectile.getDirection()).multiply(multiplier));
  }


  @Override
  public void run() {
    projectiles.forEach(Projectile::run);
  }

}
