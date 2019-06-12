package net.jingles.enchantments.projectile;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectileManager {

  private final Set<Projectile> projectiles = new HashSet<>();

  public void register(Projectile projectile) {
    projectiles.add(projectile);
  }

  public Set<Projectile> getNearbyProjectiles(Location center, double radius) {
    return getNearbyProjectiles(center, radius, radius, radius);
  }

  public Set<Projectile> getNearbyProjectiles(Location center, double x, double y, double z) {
    BoundingBox boundingBox = BoundingBox.of(center, x, y, z);
    return projectiles.stream()
        .filter(projectile -> boundingBox.overlaps(projectile.getHitbox()))
        .collect(Collectors.toSet());
  }

  public void bounceProjectileFromSource(Projectile projectile, Location source) {
    Vector direction = source.toVector().subtract(projectile.getLocation().toVector()).normalize();
    double multiplier = 1 + (Math.random() * 5);

    projectile.setDirection(direction.multiply((projectile.getDirection().dot(direction)))
        .multiply(-2).add(projectile.getDirection()).multiply(multiplier));
  }



}
