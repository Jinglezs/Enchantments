package net.jingles.enchantments.enchants.armor;

import net.jingles.enchantments.Enchant;
import net.jingles.enchantments.enchants.CustomEnchant;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Enchant(name = "Puppet Master", key = "puppet_master", levelRequirement = 30, maxLevel = 3,
        targetItem = EnchantmentTarget.ARMOR_HEAD, description = "When the wearer attacks a mob, " +
        "there is a 25% (+15% per level) chance that nearby mobs capable of attacking will target the victim.")

public class PuppetMaster extends CustomEnchant {

  public PuppetMaster(NamespacedKey key) {
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
  public void onEntityAttack(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof LivingEntity)) return;

    Player player = (Player) event.getDamager();
    if (!canTrigger(player.getInventory(), event)) return;

    int level = getItem(player.getInventory()).getItemMeta().getEnchantLevel(this);
    double probability = 0.25 + ((level * 15) / 100D);

    if (Math.random() >= probability) return;

    player.getNearbyEntities(25, 25, 25).stream()
            .filter(entity -> entity instanceof Mob)
            .map(entity -> (Mob) entity)
            .forEach(entity -> entity.setTarget((LivingEntity) event.getEntity()));

  }

}
