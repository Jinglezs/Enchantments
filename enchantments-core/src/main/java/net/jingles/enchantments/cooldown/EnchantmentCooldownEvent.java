package net.jingles.enchantments.cooldown;

import net.jingles.enchantments.enchant.CustomEnchant;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.persistence.PersistentDataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * This event is called whenever a cooldown is added to a player for a
 * specific custom Enchantment. This does not apply to vanilla enchantments!
 * The cooldown can be edited through a normal event listener as the listener
 * pleases, or the cooldown addition can be cancelled entirely.
 */
public class EnchantmentCooldownEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private final CustomEnchant enchant;
  private final PersistentDataHolder holder;
  private long cooldown;
  private TimeUnit unit;
  private boolean cancelled;

  public EnchantmentCooldownEvent(PersistentDataHolder holder, CustomEnchant enchant, long cooldown, TimeUnit unit) {
    this.holder = holder;
    this.enchant = enchant;
    this.cooldown = cooldown;
    this.unit = unit;
  }

  /**
   * Returns the PersistentDataHolder that the cooldown will
   * be applied to.
   * @return the cooldown target.
   */
  public PersistentDataHolder getHolder() {
    return this.holder;
  }

  /**
   * Gets the enchantment that the cooldown will
   * be applied to.
   * @return the applicable enchantment
   */
  public CustomEnchant getEnchant() {
    return this.enchant;
  }

  /**
   * Gets the raw cooldown in the original time unit.
   * @return the raw cooldown.
   */
  public long getRawCooldown() {
    return this.cooldown;
  }

  /**
   * Sets the raw cooldown in the original time unit.
   * @param cooldown the new cooldown.
   */
  public void setRawCooldown(long cooldown) {
    this.cooldown = cooldown;
  }

  /**
   * Sets which time unit (conversion factor)
   * the raw cooldown represents.
   * @param unit the time unit
   */
  public void setTimeUnit(TimeUnit unit) {
    this.unit = unit;
  }

  /**
   * Gets the time unit (conversion factor) that the raw cooldown
   * represents. Ex: if the raw cooldown is 5, and
   * the time unit is HOURS, then the calculated cooldown
   * will be 5 HOURS converted to MILLISECONDS.
   * @return the time unit of the raw cooldown.
   */
  public TimeUnit getTimeUnit() {
    return this.unit;
  }

  /**
   * Gets the cooldown in milliseconds.
   * Note: must be added to current System time!
   * @return the cooldown in milliseconds.
   */
  public long getConvertedCooldown() {
    return TimeUnit.MILLISECONDS.convert(cooldown, unit);
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled;
  }

  @Override
  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

}
