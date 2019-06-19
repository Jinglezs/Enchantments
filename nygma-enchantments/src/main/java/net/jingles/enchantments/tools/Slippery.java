package net.jingles.enchantments.tools;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

@Enchant(name = "Slippery", key = "slippery", levelRequirement = 10, enchantChance = 0.20, maxLevel = 3,
    targetItem = EnchantmentTarget.ALL, targetGroup = TargetGroup.NON_WEARABLE, cursed = true,
    description = "When the player switches to this item, there is a 10% (+5% per level) chance " +
        "for the item to slip out of their hands and drop to the ground.")

public class Slippery extends CustomEnchant {

  public Slippery(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment enchant) {
    return false;
  }

  @Override
  public boolean canTrigger(Player player) {
    ItemStack item = getItem(player.getInventory());
    return item != null && hasEnchantment(item);
  }

  @EventHandler
  public void onMainHandSwitch(PlayerItemHeldEvent event) {
    Player player = event.getPlayer();
    if (canTrigger(player)) tryDrop(player, getItem(player.getInventory()));
  }

  @EventHandler
  public void onSwitchToOffhand(PlayerSwapHandItemsEvent event) {
    Player player = event.getPlayer();
    if (canTrigger(player)) tryDrop(player, getItem(player.getInventory()));
  }

  private void tryDrop(Player player, ItemStack item) {
    int level = item.getItemMeta().getEnchantLevel(this);
    double probability = 0.10 + ((level * 5) / 100D);
    if (Math.random() >= probability) return;

    Map<Integer, ItemStack> leftOvers = player.getInventory().removeItem(item);
    //Only drop the item if it was successfully removed from the player's inventory.
    if (leftOvers.isEmpty()) {
      Item dropped = player.getWorld().dropItemNaturally(player.getLocation(), item);
      dropped.setVelocity(dropped.getVelocity().multiply(0.15));
    }

    TextComponent dropMessage = new TextComponent("Nice grip, m8");
    dropMessage.setColor(ChatColor.RED);
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, dropMessage);
  }
}
