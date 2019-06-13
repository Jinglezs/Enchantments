package net.jingles.enchantments.armor;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Enchant(name = "Thiccness", key = "thiccness", targetItem = EnchantmentTarget.ARMOR_LEGS, cooldown = 3,
    enchantChance = 0.45, description = "Allows the player to use the strength of their thicc thighs to " +
    "supercharge the power of their jump, increasing their jump height dramatically.")

public class Thiccness extends CustomEnchant {

  private final Map<UUID, Double> charges = new HashMap<>();

  public Thiccness(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(Inventory inventory, Event event) {
    ItemStack leggings = getItem(inventory);
    return leggings != null && hasEnchantment(leggings) &&
        !Enchantments.getCooldownManager().hasCooldown(((Player) inventory.getHolder()).getUniqueId(), this);
  }

  @EventHandler
  public void onPlayerCrouch(PlayerToggleSneakEvent event) {

    if (!canTrigger(event.getPlayer().getInventory(), event)) return;

    Player player = event.getPlayer();

    if (player.isOnGround() && event.isSneaking()) {

      Enchantments plugin = (Enchantments) Bukkit.getPluginManager().getPlugin("Enchantments");

      new BukkitRunnable() {
        public void run() {

          double charge = charges.getOrDefault(player.getUniqueId(), 0.0D);
          if (charge < 500) charges.put(player.getUniqueId(), charge += 4.5);

          TextComponent message = new TextComponent("Thiccness Power: " + charge + "%");
          message.setColor(ChatColor.GREEN);
          player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);

          if (!player.isSneaking()) {

            charges.remove(player.getUniqueId());
            message.setText("RELEASE THE THICCNESS");
            message.setColor(ChatColor.RED);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);

            //Negate fall damage
            NamespacedKey key = Enchantments.getEnchantmentManager().getFallDamageKey();
            player.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);

            player.setVelocity(player.getVelocity().setY((charge / 75)));

            Enchantments.getCooldownManager().addCooldown(player.getUniqueId(),
                Thiccness.this, getCooldown(), getTimeUnit());
            this.cancel();

          }

        }
      }.runTaskTimer(plugin, 0L, 0L);
    }

  }

}
