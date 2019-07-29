package net.jingles.enchantments.cooldown;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataHolder;
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
   * @param holder  the holder to apply the cooldown to
   * @param enchant the enchantment the cooldown is being applied to
   * @param time    the length of the cooldown **PRIOR** to conversions.
   * @param unit    the time unit of the cooldown, such as SECONDS or MINUTES
   */
  public void addCooldown(PersistentDataHolder holder, CustomEnchant enchant, long time, TimeUnit unit) {

    EnchantmentCooldownEvent event = new EnchantmentCooldownEvent(holder, enchant, time, unit);
    plugin.getServer().getPluginManager().callEvent(event);

    if (!event.isCancelled()) {
      long cooldown = System.currentTimeMillis() + TimeUnit.MILLISECONDS
          .convert(event.getRawCooldown(), event.getTimeUnit());
      holder.getPersistentDataContainer().set(enchant.getCooldownKey(), PersistentDataType.LONG, cooldown);
      if (holder instanceof TileState) ((TileState) holder).update();
    }

  }

  /**
   * Checks if the player has an active cooldown for the given enchantment.
   *
   * @param holder  the holder in question
   * @param enchant the enchantment in question
   * @return whether or not an active cooldown is present.
   */
  public boolean hasCooldown(PersistentDataHolder holder, CustomEnchant enchant) {
    if (!hasCooldownNoMessage(holder, enchant)) return false;

    if (holder instanceof Player) {

      long cooldown = getCooldown(holder, enchant);

      long remainingTime = TimeUnit.SECONDS.convert(
          cooldown - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

      String message = String.format("%s is on cooldown for %d %s", enchant.getName(),
          remainingTime, enchant.getTimeUnit().name().toLowerCase());

      TextComponent component = new TextComponent(message);
      component.setColor(ChatColor.RED);
      ((Player) holder).spigot().sendMessage(ChatMessageType.ACTION_BAR, component);

    }

    return true;
  }

  private boolean hasCooldownNoMessage(PersistentDataHolder holder, CustomEnchant enchant) {
    if (!holder.getPersistentDataContainer().has(enchant.getCooldownKey(), PersistentDataType.LONG)) return false;

    long cooldown = getCooldown(holder, enchant);

    if (System.currentTimeMillis() > cooldown) {
      removeCooldown(holder, enchant);
      return false;
    }

    return true;
  }

  public long getCooldown(PersistentDataHolder holder, CustomEnchant enchant) {
    return holder.getPersistentDataContainer().getOrDefault(enchant.getCooldownKey(), PersistentDataType.LONG, 0L);
  }

  /**
   * Removes a cooldown for the given enchantment.
   *
   * @param holder  the holder
   * @param enchant the enchantment in question.
   */
  public void removeCooldown(PersistentDataHolder holder, CustomEnchant enchant) {
    holder.getPersistentDataContainer().remove(enchant.getCooldownKey());
    if (holder instanceof TileState) ((TileState) holder).update();
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
