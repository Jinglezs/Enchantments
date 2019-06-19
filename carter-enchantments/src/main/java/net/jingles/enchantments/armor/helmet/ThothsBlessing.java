package net.jingles.enchantments.armor.helmet;

import net.jingles.enchantments.cooldown.EnchantmentCooldownEvent;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

@Enchant(name = "Thoth's Blessing", key = "thoths_blessing", levelRequirement = 30, maxLevel = 3, enchantChance = 0.25,
    targetItem = EnchantmentTarget.ARMOR_HEAD, description = "While a helmet with this enchantment is worn, any " +
    "new custom enchantment cooldowns added to the wearer will be reduced by 10% per level.")

public class ThothsBlessing extends CustomEnchant {

  public ThothsBlessing(NamespacedKey key) {
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
  public void onEnchantmentAddtion(EnchantmentCooldownEvent event) {
    if (canTrigger(event.getPlayer())) {
      ItemStack helmet = getItem(event.getPlayer().getInventory());
      double reduction = (10 * helmet.getItemMeta().getEnchantLevel(this)) / 100;
      event.setRawCooldown((long) (event.getRawCooldown() - (event.getRawCooldown() * reduction)));
    }
  }

}
