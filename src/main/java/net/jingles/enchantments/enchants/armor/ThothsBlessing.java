package net.jingles.enchantments.enchants.armor;

import net.jingles.enchantments.Enchant;
import net.jingles.enchantments.cooldown.EnchantmentCooldownEvent;
import net.jingles.enchantments.enchants.CustomEnchant;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Enchant(name = "Thoth's Blessing", key = "thoths_blessing", levelRequirement = 30, maxLevel = 3,
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
  public boolean canTrigger(Inventory inventory, Event event) {
    ItemStack helm = getItem(inventory);
    return helm != null && hasEnchantment(helm);
  }

  @EventHandler
  public void onEnchantmentAddtion(EnchantmentCooldownEvent event) {
    if (canTrigger(event.getPlayer().getInventory(), event)) {
      ItemStack helmet = getItem(event.getPlayer().getInventory());
      double reduction = (10 * helmet.getItemMeta().getEnchantLevel(this)) / 100;
      event.setRawCooldown((long) (event.getRawCooldown() - (event.getRawCooldown() * reduction)));
    }
  }

}
