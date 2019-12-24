package net.jingles.enchantments.weapon;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.statuseffect.context.ItemEffectContext;
import net.jingles.enchantments.statuseffect.entity.PotionStatusEffect;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Enchant(name = "Blood Lust", key = "blood_lust", maxLevel = 3, targetItem = EnchantmentTarget.WEAPON,
  description = "Upon killing a mob or player, the owner receives speed and power effects that " +
    "last for 10 seconds per level and have an amplifier equal to the enchantment level.")
public class BloodLust extends CustomEnchant {

  public BloodLust(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(LivingEntity entity) {
    ItemStack weapon = getItem(entity);
    return weapon != null && hasEnchantment(weapon);
  }

  @EventHandler
  public void onEntityKill(EntityDeathEvent event) {

    if (event.getEntity().getKiller() == null)
      return;

    Player player = event.getEntity().getKiller();
    if (!canTrigger(player))
      return;

    ItemStack item = getItem(player);
    int level = getLevel(item);
    int duration = (level * 10) * 20;

    PotionEffect effect = new PotionEffect(PotionEffectType.SPEED, duration, level);
    PotionEffect effect1 = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration, level);

    ItemEffectContext context = new ItemEffectContext(player, item, this);
    Enchantments.getStatusEffectManager().add(new BloodLustEffect(context, effect, player));
    Enchantments.getStatusEffectManager().add(new BloodLustEffect(context, effect1, player));

  }

  private static class BloodLustEffect extends PotionStatusEffect {

    private BloodLustEffect(ItemEffectContext context, PotionEffect potionEffect, LivingEntity target) {
      super(potionEffect, target, context, 1);
    }

    @Override
    public void start() {
      getTarget().getWorld().playSound(getTarget().getLocation(), Sound.ENTITY_FOX_AGGRO, 1F, 1F);
    }

    @Override
    public void effect() {
    }

  }

}