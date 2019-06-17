package net.jingles.enchantments.statuseffect;

import net.jingles.enchantments.enchant.CustomEnchant;

public abstract class StatusEffect implements Comparable<StatusEffect> {

  private CustomEnchant source; // The enchantment that created the effect.
  private int maxTicks; // The maximum amount of ticks that the effect can live for.
  private int interval; // Time between effect executions
  private int nextExecution = 0; // The next tick amount that will trigger the effect
  private int ticks = 0; // The current amount of ticks the effect has been alive for
  private boolean cancelled = false; // Whether or not the effect should be stopped.

  public StatusEffect(CustomEnchant source, int maxTicks, int interval) {
    this.source = source;
    this.maxTicks = maxTicks;
    this.interval = Math.max(1, interval);
  }

  public abstract void effect();

  public void start() {
  }

  public void stop() {
    this.cancelled = true;
  }

  /**
   * Gets the CustomEnchant that this effect belongs to.
   *
   * @return the owning CustomEnchant
   */
  public CustomEnchant getSource() {
    return this.source;
  }

  /**
   * Gets how long the effect will last in ticks.
   *
   * @return duration of effect
   */
  public int getMaxTicks() {
    return this.maxTicks;
  }

  /**
   * Gets how long the effect has lived in ticks.
   * @return effect lifetime in ticks
   */
  public int getTicks() {
    return this.ticks;
  }

  /**
   * Gets the amount of ticks remaining util the effect expires.
   * @return ticks remaining
   */
  public int getTicksLeft() {
    return getMaxTicks() - getTicks();
  }

  /**
   * Gets the time interval at which the effects will be applied.
   *
   * @return time between each effect trigger.
   */
  public int getInterval() {
    return this.interval;
  }

  /**
   * Sets the next tick count that will run the effect() method.
   */
  public void setNextExecution() {
    this.nextExecution = ticks + getInterval();
  }

  /**
   * Whether or not the effect has been cancelled.
   * @return true if cancelled.
   */
  public boolean isCancelled() {
    return this.cancelled;
  }

  /**
   * Cancels the effect, which prevents its effect() method
   * from running again and removes it from the owning container.
   */
  public void cancel() {
    this.cancelled = true;
  }

  /**
   * Determines whether or not the effect is permitted to run the
   * effect() method.
   * @return the reason for failure, or SUCCESS if none.
   */
  public TickFailure canTick() {
    return isCancelled() ? TickFailure.CANCELLED :
        (ticks > maxTicks) ? TickFailure.EXPIRED :
            (nextExecution != ticks++) ? TickFailure.INTERVAL :
                TickFailure.SUCCESS;
  }

  enum TickFailure {
    CANCELLED(true), // the effect has been cancelled
    EXPIRED(true), // the effect has reached its maximum duration
    INTERVAL(false), // waiting for the next interval tick
    SUCCESS(false); // there was no failure :V

    private boolean isFatal;

    TickFailure(boolean isFatal) {
      this.isFatal = isFatal;
    }

    public boolean isFatal() {
      return this.isFatal;
    }

  }

  @Override
  public int compareTo(StatusEffect other) {
    return Integer.compare(ticks, other.getTicks());
  }

}
