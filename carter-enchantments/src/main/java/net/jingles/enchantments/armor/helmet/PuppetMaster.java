package net.jingles.enchantments.armor.helmet;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@Enchant(name = "Puppet Master", key = "puppet_master", levelRequirement = 30, maxLevel = 3, enchantChance = 0.25,
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
  public boolean canTrigger(LivingEntity entity) {
    return hasEnchantment(getItem(entity));
  }

  @EventHandler
  public void onEntityAttack(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof LivingEntity) || !(event.getEntity() instanceof LivingEntity)) return;

    LivingEntity damager = (LivingEntity) event.getDamager();
    if (!canTrigger(damager)) return;

    int level = getLevel(getItem(damager));
    double probability = 0.25 + ((level * 15) / 100D);

    if (Math.random() >= probability) return;

    damager.getNearbyEntities(25, 25, 25).stream()
            .filter(entity -> entity instanceof Mob)
            .map(entity -> (Mob) entity)
            .forEach(entity -> entity.setTarget((LivingEntity) event.getEntity()));

  }

}
