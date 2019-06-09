package net.jingles.enchantments.cooldown;

import co.aikar.util.Table;
import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchants.CustomEnchant;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CooldownManager extends BukkitRunnable {

  // A table is pretty much a Map within a Map :D
  // The UUID is the key, the Enchantment is the key of the *nested* Map,
  // and the Long is the value of the *nested* Map. This can also be written
  // as Map<UUID, Map<CustomEnchant, Long>>, but that's less cool.
  private final Table<UUID, CustomEnchant, Long> cooldowns = new Table<>();
  private final Enchantments plugin;

  public CooldownManager(Enchantments plugin) {
    this.plugin = plugin;
    runTaskTimerAsynchronously(plugin, 0, 5);
  }

  // Cooldowns will be checked for removal every 5 ticks (which means that
  // cooldowns cannot be shorter than 0.25 seconds, or 250 milliseconds).
  @Override
  public void run() {
    cooldowns.removeIf((id, enchant, time) -> time < System.currentTimeMillis());
  }

  /**
   * Adds a new cooldown or overwrites an existing one.
   * @param id the player's unique id
   * @param enchant the enchantment the cooldown is being applied to
   * @param time the length of the cooldown **PRIOR** to conversions.
   * @param unit the time unit of the cooldown, such as SECONDS or MINUTES
   */
  public void addCooldown(UUID id, CustomEnchant enchant, long time, TimeUnit unit) {

    EnchantmentCooldownEvent event = new EnchantmentCooldownEvent(id, enchant, time, unit);
    plugin.getServer().getPluginManager().callEvent(event);

    if (!event.isCancelled())
      cooldowns.put(id, enchant, System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(time, unit));

  }

  public Table<UUID, CustomEnchant, Long> getCooldowns() {
    return this.cooldowns;
  }

  /**
   * Checks if the player has an active cooldown for the given enchantment.
   * @param id the player's unique id
   * @param enchant the enchantment in question
   * @return whether or not an active cooldown is present.
   */
  public boolean hasCooldown(UUID id, CustomEnchant enchant) {
    return cooldowns.containsKey(id, enchant);
  }

  /**
   * Removes a cooldown for the given enchantment.
   * @param id the player's unique id
   * @param enchant the enchantment in question.
   */
  public void removeCooldown(UUID id, CustomEnchant enchant) {
    cooldowns.remove(id, enchant);
  }

}
