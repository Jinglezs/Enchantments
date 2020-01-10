package net.jingles.enchantments.weapon;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.statuseffect.context.ItemEffectContext;
import net.jingles.enchantments.statuseffect.entity.PotionStatusEffect;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

@Enchant(name = "Disorientation", key = "disorientation", enchantChance = 0.25, cooldown = 10,
    targetItem = EnchantmentTarget.WEAPON, description = "Striking an opponent causes them to " +
    "become disoriented, forcing their head in a random direction for a maximum of 5 seconds. Hostile " +
    "mobs will also lose their target.")

public class Disorientation extends CustomEnchant {

  public Disorientation(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(@NotNull LivingEntity entity) {
    return hasEnchantment(getItem(entity)) && !Enchantments.getCooldownManager()
        .hasCooldown(entity, this);
  }

  @EventHandler
  public void onEntityDamage(EntityDamageByEntityEvent event) {

    if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof LivingEntity)) return;

    Player attacker = (Player) event.getDamager();
    if (!canTrigger(attacker)) return;

    ItemStack item = getItem(attacker);
    int duration = getLevel(item) * 20;

    PotionEffect effect = new PotionEffect(PotionEffectType.CONFUSION, duration, 3, false, true);
    ItemEffectContext context = new ItemEffectContext(attacker, item, this);
    Enchantments.getStatusEffectManager().add(new DisorientationEffect(context, effect, (LivingEntity) event.getEntity()));

  }

  private static class DisorientationEffect extends PotionStatusEffect {

    private DisorientationEffect(ItemEffectContext context, PotionEffect potionEffect, LivingEntity target) {
      super(potionEffect, target, context, 1);
    }

    @Override
    public void effect() {

      if (getTarget() instanceof Mob) {
        ((Mob) getTarget()).setTarget(null);
      }

      float yaw = ThreadLocalRandom.current().nextInt(360);
      float pitch = ThreadLocalRandom.current().nextInt(-90, 90);

      Location location = getTarget().getLocation();
      location.setYaw(yaw);
      location.setPitch(pitch);

      getTarget().teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);

    }

  }

}
