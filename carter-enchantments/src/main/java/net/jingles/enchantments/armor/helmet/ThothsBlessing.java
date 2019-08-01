package net.jingles.enchantments.armor.helmet;

import net.jingles.enchantments.cooldown.EnchantmentCooldownEvent;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Enchant(name = "Thoth's Blessing", key = "thoths_blessing", levelRequirement = 30, maxLevel = 3, enchantChance = 0.25,
    targetItem = EnchantmentTarget.ARMOR_HEAD, description = "While a helmet with this enchantment is worn, any " +
    "new custom enchantment cooldowns added to the wearer will be reduced by 10% per level.")

public class ThothsBlessing extends CustomEnchant {

  public ThothsBlessing(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(LivingEntity entity) {
    ItemStack helm = getItem(entity);
    return helm != null && hasEnchantment(helm);
  }

  @EventHandler
  public void onEnchantmentAddtion(EnchantmentCooldownEvent event) {

    if (!(event.getHolder() instanceof LivingEntity)) return;

    LivingEntity holder = (LivingEntity) event.getHolder();

    if (canTrigger(holder)) {
      double reduction = (10 * getLevel(getItem(holder))) / 100;
      event.setRawCooldown((long) (event.getRawCooldown() - (event.getRawCooldown() * reduction)));
    }

  }

}
