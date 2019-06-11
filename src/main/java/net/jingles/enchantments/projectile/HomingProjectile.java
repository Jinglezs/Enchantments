package net.jingles.enchantments.projectile;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class HomingProjectile extends Projectile {

  private final Entity target;
  private double threshold = 25;
  private double accuracy = 0.25;
  private double localAccuracy = accuracy;

  public HomingProjectile(Player owner, Entity target) {
    super(owner);
    this.target = target;
  }

  @Override
  public void move() {
    if (target == null) {
      cancel();
      return;
    }

    double speed = getDirection().length();
    double addition;
    double distance = getLocation().distanceSquared(target.getLocation());

    addition = (distance > threshold) ? 0 : (threshold + 1) - distance / 5;
    localAccuracy += addition;

    Location location = target.getLocation();
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
