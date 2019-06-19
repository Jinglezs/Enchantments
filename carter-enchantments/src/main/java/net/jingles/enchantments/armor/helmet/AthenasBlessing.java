package net.jingles.enchantments.armor.helmet;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;

@Enchant(name = "Athena's Blessing", key = "athenas_blessing", levelRequirement = 30,
  targetItem = EnchantmentTarget.ARMOR_HEAD, maxLevel = 10, description = "Increases all " +
        "experience gained by 10% (+10% per level).")

public class AthenasBlessing extends CustomEnchant {

  public AthenasBlessing(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(Player player) {
    ItemStack helm = getItem(player.getInventory());
    return helm != null && hasEnchantment(helm);
  }

  @EventHandler
  public void onExperienceGain(PlayerExpChangeEvent event) {
    if (!canTrigger(event.getPlayer())) return;

    double original = event.getAmount();
    int level = getItem(event.getPlayer().getInventory()).getItemMeta().getEnchantLevel(this);
    double increase = 0.10 + ((level * 10) / 100D);
    int newExperience = (int) (original + (original * increase));
    event.setAmount(newExperience);

    TextComponent component = new TextComponent("Athena has blessed you with " + (event.getAmount() - original) + " additional experience!");
    component.setColor(ChatColor.AQUA);
    event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
  }

}
