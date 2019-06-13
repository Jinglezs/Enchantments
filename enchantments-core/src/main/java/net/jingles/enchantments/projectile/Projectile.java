package net.jingles.enchantments.projectile;

import net.jingles.enchantments.Enchantments;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Projectile extends BukkitRunnable {

  private final Set<Predicate<Block>> blockFilters;
  private final Set<Predicate<Entity>> entityFilters;
  private final Player owner;

  private Particle particle;
  private Object options;
  private Vector direction;
  private Location location;
  private BoundingBox hitbox;
  private double maxDistance;
  private boolean noClip = false;
  private double distanceTraveled = 0D;

  private Runnable onTick = () -> {};
  private BiConsumer<Projectile, Block> onBlockHit = (projectile, block) -> {};
  private BiConsumer<Projectile, Entity> onEntityHit = (projectile, block) -> {};

  public Projectile(Player owner) {
    this.blockFilters = new HashSet<>();
    this.entityFilters = new HashSet<>();
    this.owner = owner;
    blockFilters.add(block -> !block.getType().name().endsWith("AIR"));
    Enchantments.getProjectileManager().register(this);
  }

  @Override
  public void run() {
    this.onTick.run();
    move();
    display();
    target();

    if (distanceTraveled > maxDistance) this.cancel();
  }

  public void move() {
    Location previous = this.location.clone();
    this.location = location.add(direction);
    resizeHitBox(location);
    this.distanceTraveled += previous.distanceSquared(location);
  }

  public void target() {

    List<Entity> targets = location.getWorld().getNearbyEntities(hitbox).stream()
        .filter(entity -> !entity.equals(owner))
        .filter(entityFilters.stream().reduce(Predicate::or).orElse(t -> true))
        .collect(Collectors.toList());

    if (!targets.isEmpty()) onEntityHit.accept(this, targets.get(0));
    if (noClip) return; //No need to check for block collisions

    Block block = location.getBlock();
    boolean canCollide = blockFilters.stream().reduce(Predicate::and).orElse(t -> false).test(block);
    if (canCollide) onBlockHit.accept(this, block);

  }

  public void display() {
    //Spawn the particle
    if (options != null) {
      location.getWorld().spawnParticle(particle, location, 1, 0, 0, 0, options);
    } else location.getWorld().spawnParticle(particle, location, 1);
  }

  /**
   * Ensures that all fields have a non-null value and starts the BukkitRunnable.
   */
  public void launch() {

    //Ensures that none of the necessary fields are null.
    boolean canLaunch = Stream.of(Projectile.class.getDeclaredFields())
        .filter(field -> field.getType() != Object.class)
        .allMatch(field -> {
          try {
            return field.get(this) != null;
          } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
          }
        });

    if (canLaunch) runTaskTimer(Bukkit.getPluginManager().getPlugin("Enchantments"), 0, 1);
  }

  /**
   * Sets which particle will be displayed at the projectile's location.
   *
   * @param particle the particle
   * @return the projectile instance.
   */
  public Projectile setParticle(Particle particle) {
    this.particle = particle;
    return this;
  }

  /**
   * Sets which direction (vector) the projectile will travel in
   * every tick.
   *
   * @param direction the directional vector
   * @return the projectile instance
   */
  public Projectile setDirection(Vector direction) {
    this.direction = direction;
    return this;
  }

  /**
   * Sets the runnable to be executed every tick while the projectile is alive.
   *
   * @param runnable actions to be executed every tick.
   * @return the projectile instance.
   */
  public Projectile onTick(Runnable runnable) {
    this.onTick = runnable;
    return this;
  }

  /**
   * Sets the location the projectile will be launched from.
   *
   * @param location the starting location.
   * @return the projectile instance.
   */
  public Projectile setStartLocation(Location location) {
    this.location = location;
    return this;
  }

  /**
   * Adds a filter that will only trigger a collision and trigger the
   * onBlockHit() consumer if the block meets the provided predicate.
   *
   * @param filter the block predicate
   * @return the projectile instance
   */
  public Projectile withBlockFilter(Predicate<Block> filter) {
    this.blockFilters.add(filter);
    return this;
  }

  /**
   * Adds a filter that will only allow entities that meet the predicate
   * to cause a collision and trigger the onEntityHit() consumer.
   *
   * @param filter the entity predicate
   * @return the projectile instance.
   */
  public Projectile withEntityFilter(Predicate<Entity> filter) {
    this.entityFilters.add(filter);
    return this;
  }

  /**
   * Sets the dust options used by the Particle instance to determine color and size.
   * Note: DustOptions only apply to Dust particle types.
   *
   * @param data the dust options
   * @return the projectile instance.
   */
  public Projectile withParticleData(Object data) {
    this.options = data;
    return this;
  }

  /**
   * Set whether or not the projectile can move through blocks.
   *
   * @param noClip block collision or not?
   * @return the projectile instance.
   */
  public Projectile setNoClip(boolean noClip) {
    this.noClip = noClip;
    return this;
  }

  /**
   * Set what happens when the projectile hits an entity.
   *
   * @param consumer takes the entity and projectile instances and does something with them.
   * @return the projectile instance.
   */
  public Projectile onEntityHit(BiConsumer<Projectile, Entity> consumer) {
    this.onEntityHit = consumer;
    return this;
  }

  /**
   * Set what happens when the projectile hits a block.
   *
   * @param consumer takes the block and projectile instances and does something with them.
   * @return the projectile instance.
   */
  public Projectile onBlockHit(BiConsumer<Projectile, Block> consumer) {
    this.onBlockHit = consumer;
    return this;
  }

  /**
   * Set the maximum distance that the projectile can travel before being killed.
   *
   * @param maxDistance the maximum distance
   * @return the projectile instance
   */
  public Projectile setMaxDistance(double maxDistance) {
    //Square the distance so it can be compared with Location.getDistanceSquared()
    this.maxDistance = maxDistance * maxDistance;
    return this;
  }

  /**
   * Sets the collision hitbox of the projectile.
   * Note: Must be called after the starting location is set!
   * @param x distance from location on the x-axis
   * @param y distance from location on the y-axis
   * @param z distance from location on the z-axis
   * @return the projectile instance.
   */
  public Projectile withHitbox(double x, double y, double z) {
    this.hitbox = BoundingBox.of(location, x, y, z);
    return this;
  }

  /**
   * Shifts the hitbox so that it is at the current location.
   *
   * @param loc the location to be shifted to.
   */
  private void resizeHitBox(Location loc) {
    BoundingBox bb = this.hitbox;
    bb.resize(loc.getX(), loc.getY(), loc.getZ(), loc.getX() + bb.getWidthX(),
        loc.getY() + bb.getHeight(), loc.getZ() + bb.getWidthZ());
  }

  public Player getOwner() {
    return this.owner;
  }

  public Vector getDirection() {
    return this.direction;
  }

  public Location getLocation() {
    return this.location;
  }

  public Particle getParticle() {
    return this.particle;
  }

  public BoundingBox getHitbox() {
    return this.hitbox;
  }

  public double getMaxDistance() {
    return this.maxDistance;
  }

  public double getDistanceTraveled() {
    return this.distanceTraveled;
  }

  public boolean isNoClip() {
    return this.noClip;
  }

}
