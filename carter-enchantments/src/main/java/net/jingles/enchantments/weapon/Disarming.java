package net.jingles.enchantments.weapon;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

@Enchant(name = "Disarming", key = "disarming", cooldown = 15, targetItem = EnchantmentTarget.WEAPON, maxLevel = 3,
  description = "Upon hitting another player, there is a 10% (+10% per level) chance for their weapon to " +
      "be knocked out of their hand with extreme force. However, the weapon will be glowing to make it easier to find.")

public class Disarming extends CustomEnchant {

  public Disarming(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(LivingEntity entity) {
    ItemStack sword = getItem(entity);
    return sword != null && hasEnchantment(sword) &&
        !Enchantments.getCooldownManager().hasCooldown(entity, this);
  }

  @EventHandler
  public void onEntityHit(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

    Player attacker = (Player) event.getDamager();
    Player attacked = (Player) event.getEntity();
    if (!canTrigger(attacker)) return;

    int level = getLevel(getItem(attacker));
    if (Math.random() > (0.10 + ((level * 10) / 100D))) return;

    ItemStack held = attacked.getInventory().getItemInMainHand();
    if (held.getType() == Material.AIR) return;
    attacked.getInventory().remove(held);

    Item item = attacked.getWorld().dropItemNaturally(attacked.getLocation(), held);
    item.setVelocity(item.getVelocity().multiply(1.5).setY(1.15));
    item.setGlowing(true);

    addCooldown(attacker);
  }

}
