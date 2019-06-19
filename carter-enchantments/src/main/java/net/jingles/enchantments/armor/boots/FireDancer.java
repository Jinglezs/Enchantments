package net.jingles.enchantments.armor.boots;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@Enchant(name = "Fire Dancer", key = "fire_dancer", targetItem = EnchantmentTarget.ARMOR_FEET,
    levelRequirement = 20, maxLevel = 1, enchantChance = 0.30, description = "Grants the wearer " +
    "immunity to fire damage and provides a speed buff while on fire.")

public class FireDancer extends CustomEnchant {

  public FireDancer(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public boolean canEnchantItem(@NotNull ItemStack item) {
    if (item.getType().toString().contains("HORSE_ARMOR")) return true;
    else return super.canEnchantItem(item);
  }

  @Override
  public boolean canTrigger(Player player) {
    ItemStack enchanted = getItem(player.getInventory());
    return enchanted != null && hasEnchantment(enchanted);
  }

  @EventHandler
  private void onCombust(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player)) return;

    if (event.getCause() == EntityDamageEvent.DamageCause.LAVA || event.getCause() == EntityDamageEvent.DamageCause.FIRE
        || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {

      Player player = (Player) event.getEntity();
      if (!canTrigger(player)) return;

      event.setCancelled(true);
      player.setFireTicks(0);
      player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 5, 3, false, true));
    }

  }

}
