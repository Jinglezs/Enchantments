package net.jingles.enchantments.projectile;

import net.jingles.enchantments.Enchantments;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class HomingProjectile extends Projectile {

  private final Entity target;
  private final double height;
  private double threshold = 25;
  private double accuracy = 0.25;
  private double localAccuracy = accuracy;

  public HomingProjectile(Player owner, Entity target) {
    super(owner);
    this.target = target;
    //Aiming for the entity's center
    this.height = target.getHeight() / 2;
  }

  @Override
  public void move() {

    if (target == null || target.isDead()) {
      Enchantments.getProjectileManager().unregister(this);
      return;
    }

    Location location = target.getLocation().add(0, height, 0);

    double speed = getDirection().length();
    double addition;
    double distance = getLocation().distanceSquared(location);

    addition = (distance > threshold) ? 0 : (threshold + 1) - distance / 5;
    localAccuracy += addition;

    setDirection(getDirection().add(location.subtract(getLocation()).toVector().normalize()
        .multiply(localAccuracy * speed)).normalize().multiply(speed));
    super.move();
  }

  public HomingProjectile withDistanceThreshold(double threshold) {
    this.threshold = threshold * threshold;
    return this;
  }

  public HomingProjectile withAccuracy(double accuracy) {
    this.accuracy = accuracy;
    return this;
  }

  @Override
  public void launch() {
    if (target != null) super.launch();
  }

}
