package net.jingles.enchantments.cooldown;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CooldownManager implements Listener {

  private final Enchantments plugin;

  public CooldownManager(Enchantments plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  /**
   * Adds a new cooldown or overwrites an existing one.
   *
   * @param player  the player to apply the cooldown on
   * @param enchant the enchantment the cooldown is being applied to
   * @param time    the length of the cooldown **PRIOR** to conversions.
   * @param unit    the time unit of the cooldown, such as SECONDS or MINUTES
   */
  public void addCooldown(Player player, CustomEnchant enchant, long time, TimeUnit unit) {

    EnchantmentCooldownEvent event = new EnchantmentCooldownEvent(player, enchant, time, unit);
    plugin.getServer().getPluginManager().callEvent(event);

    if (!event.isCancelled()) {
      long cooldown = System.currentTimeMillis() + TimeUnit.MILLISECONDS
          .convert(event.getRawCooldown(), event.getTimeUnit());
      player.getPersistentDataContainer().set(enchant.getKey(), PersistentDataType.LONG, cooldown);
    }

  }

  /**
   * Checks if the player has an active cooldown for the given enchantment.
   *
   * @param player  the player in question
   * @param enchant the enchantment in question
   * @return whether or not an active cooldown is present.
   */
  public boolean hasCooldown(Player player, CustomEnchant enchant) {
    if (!hasCooldownNoMessage(player, enchant)) return false;

    long cooldown = getCooldown(player, enchant);

    long remainingTime = TimeUnit.SECONDS.convert(
        cooldown - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

    String message = String.format("%s is on cooldown for %d %s", enchant.getName(),
        remainingTime, enchant.getTimeUnit().name().toLowerCase());

    TextComponent component = new TextComponent(message);
    component.setColor(ChatColor.RED);
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);

    return true;
  }

  private boolean hasCooldownNoMessage(Player player, CustomEnchant enchant) {
    if (!player.getPersistentDataContainer().has(enchant.getKey(), PersistentDataType.LONG)) return false;

    long cooldown = getCooldown(player, enchant);

    if (System.currentTimeMillis() > cooldown) {
      removeCooldown(player, enchant);
      return false;
    }

    return true;
  }

  public long getCooldown(Player player, CustomEnchant enchant) {
    return player.getPersistentDataContainer().getOrDefault(enchant.getKey(), PersistentDataType.LONG, 0L);
  }

  /**
   * Removes a cooldown for the given enchantment.
   *
   * @param player  the player
   * @param enchant the enchantment in question.
   */
  public void removeCooldown(Player player, CustomEnchant enchant) {
    player.getPersistentDataContainer().remove(enchant.getKey());
  }

  public Map<CustomEnchant, Long> getCooldowns(Player player) {

    Map<CustomEnchant, Long> cooldowns = new HashMap<>();

    Enchantments.getEnchantmentManager().getRegisteredEnchants().stream()
        .filter(enchant -> hasCooldownNoMessage(player, enchant))
        .forEach(enchant -> cooldowns.put(enchant, getCooldown(player, enchant)));

    return cooldowns;
  }

  private void purgeCooldowns(Player player) {
    if (player.getPersistentDataContainer().isEmpty()) return;
    // Will remove the cooldown if it exists and no longer applies.
    Enchantments.getEnchantmentManager().getRegisteredEnchants().forEach(enchant ->
        hasCooldownNoMessage(player, enchant));
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    purgeCooldowns(event.getPlayer());
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    purgeCooldowns(event.getPlayer());
  }

}
