package net.jingles.enchantments.tools;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicInteger;

@Enchant(name = "Thor", key = "thor", levelRequirement = 30, maxLevel = 1, cooldown = 15,
    targetItem = EnchantmentTarget.TOOL, targetGroup = TargetGroup.AXES, description = "Allows the " +
    "user to right click while looking at a block to summon lightning. Alternatively, the user may " +
    "hold crouch and right click to charge a brief flight, which lasts 3 seconds for every second it " +
    "is charged, for a maximum of 7 seconds (21 seconds of flight time).")

public class Thor extends CustomEnchant {

  private final String remaining = "%d seconds of flight remaining";
  private final String charging = "Flight Charge: %d seconds";

  public Thor(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(Inventory inventory, Event e) {
    Player player = ((PlayerEvent) e).getPlayer();
    ItemStack axe = getItem(inventory);
    return axe != null && hasEnchantment(axe) && !Enchantments.getCooldownManager()
        .hasCooldown(player.getUniqueId(), this);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_AIR ||
      !canTrigger(event.getPlayer().getInventory(), event)) return;

    Player player = event.getPlayer();
    Plugin enchantments = Bukkit.getPluginManager().getPlugin("Enchantments");

    if (player.isSneaking()) {

      AtomicInteger charge = new AtomicInteger();

      new BukkitRunnable() {
        public void run() {

          int duration = charge.getAndIncrement();

          TextComponent component = new TextComponent(String.format(charging, duration * 3));
          component.setColor(ChatColor.AQUA);
          player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);

          if (!player.isSneaking() || duration > 7) {
            flight(player, enchantments, duration * 20);
            this.cancel();
          }

        }
      }.runTaskTimer(enchantments, 0, 20);

    } else {
      Block targetted = player.getTargetBlockExact(300, FluidCollisionMode.ALWAYS);
      if (targetted != null) {
        targetted.getWorld().strikeLightning(targetted.getLocation());
        addCooldown(player.getUniqueId());
      }
    }

  }

  private void flight(Player player, Plugin plugin, int duration) {

    AtomicInteger timeInFlight = new AtomicInteger();

    new BukkitRunnable() {
      public void run() {

        int time = timeInFlight.getAndIncrement();

        //Cancel flight if player logs off, time runs out, or if they begin sneaking.
        if (!player.isOnline() || time > duration || player.isSneaking()) {
          addCooldown(player.getUniqueId());
          this.cancel();
          return;
        }

        TextComponent component = new TextComponent(String.format(remaining, (duration - time) / 20));
        component.setColor(ChatColor.GOLD);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
        player.setVelocity(player.getEyeLocation().getDirection().multiply(3));

      }
    }.runTaskTimer(plugin, 0, 0);

  }

}
