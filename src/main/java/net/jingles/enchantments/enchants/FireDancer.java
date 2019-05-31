package net.jingles.enchantments.enchants;

import net.jingles.enchantments.Enchant;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Enchant(name = "Fire Dancer", key = "fire_dancer", targetItem = EnchantmentTarget.ARMOR_FEET,
        levelRequirement = 20, maxLevel = 1, horseArmor = true, description = "Grants the wearer immunity " +
        "to fire damage and provides a speed buff while on fire.")

public class FireDancer extends CustomEnchant {

  public FireDancer(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public boolean canEnchantItem(ItemStack item) {
    if (item.getType().toString().contains("HORSE_ARMOR")) return true;
    else return super.canEnchantItem(item);
  }

  @Override
  public boolean canTrigger(Inventory inventory, Event e) {

    EntityDamageEvent event = (EntityDamageEvent) e;
    ItemStack enchanted = getItem(inventory);

    if (enchanted == null || !hasEnchantment(enchanted)) return false;

    return event.getCause() == EntityDamageEvent.DamageCause.LAVA || event.getCause() == EntityDamageEvent.DamageCause.FIRE
            || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK;
  }

  @EventHandler
  private void onCombust(EntityDamageEvent event) {
    LivingEntity entity = (event.getEntity() instanceof Player) ? (Player) event.getEntity() :
            (event.getEntity() instanceof Horse) ? (Horse) event.getEntity() : null;

    if (entity == null || !canTrigger(((InventoryHolder) entity).getInventory(), event)) return;

    event.setCancelled(true);
    entity.setFireTicks(0);
    entity.getPassengers().forEach(e -> e.setFireTicks(0));

    Block block = entity.getLocation().getBlock();
    if (entity.getLocation().getBlock().isLiquid() || block.getLocation().clone().add(0, 1, 0).getBlock().isLiquid()) {
      entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 5, 3, false, true));
    } else entity.removePotionEffect(PotionEffectType.SPEED);
  }

}
