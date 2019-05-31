package net.jingles.enchantments.enchants;

import net.jingles.enchantments.Enchant;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@Enchant(name = "Executioner", key = "executioner", targetItem = EnchantmentTarget.TOOL,
        description = "Increases weapon damage by 10% (+5% per level) and has a 10% (+10% per level) " +
                "to ignite the target for 1 (+2 per level) seconds.")

public class Executioner extends CustomEnchant {

  public Executioner(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(Inventory inventory, Event e) {
    ItemStack axe = getItem(inventory);
    return isAxe(axe.getType()) && hasEnchantment(axe);
  }

  private boolean isAxe(Material item) {
    return item == Material.WOODEN_AXE || item == Material.STONE_AXE || item == Material.IRON_AXE ||
            item == Material.GOLDEN_AXE || item == Material.DIAMOND_AXE;
  }

  @EventHandler
  public void onEntityDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player)) return;

    PlayerInventory inventory = ((Player) event.getDamager()).getInventory();
    if (!canTrigger(((Player) event.getDamager()).getInventory(), event)) return;

    int level = getItem(inventory).getItemMeta().getEnchantLevel(this);

    double increase = 0.10 + ((level * 5) / 100D);
    event.setDamage(event.getDamage() + (event.getDamage() * increase));

    double probability = 0.10 + ((level * 10) / 100D);
    if (Math.random() <= probability) event.getEntity().setFireTicks((1 + (2 * level)) * 20);

  }

}
