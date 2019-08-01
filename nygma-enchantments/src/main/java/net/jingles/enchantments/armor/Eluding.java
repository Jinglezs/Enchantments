package net.jingles.enchantments.armor;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Enchant(name = "Eluding", key = "eluding", targetItem = EnchantmentTarget.ARMOR_FEET, cooldown = 2,
    levelRequirement = 20, enchantChance = 0.30, description = "When the player is attacked while under half health, " +
        "there is a 25% (+5% per level) chance to grant the player a speed boost for 3 seconds per level, slow nearby " +
        "mobs for 1 seconds per level, and cause mobs within a 5 (+3 per level) block radius to forget their target.")

public class Eluding extends CustomEnchant {

  public Eluding(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) { return false; }

  @Override
  public boolean canTrigger(LivingEntity entity) {
    ItemStack boots = getItem(entity);
    return boots != null && hasEnchantment(boots) &&
        !Enchantments.getCooldownManager().hasCooldown(entity, this);
  }

  @EventHandler
  public void onPlayerDamage(EntityDamageByEntityEvent event) {

    if (!(event.getEntity() instanceof Player) || ((Player) event.getEntity()).getHealth() > 10) return;

    Player player = (Player) event.getEntity();
    if (!canTrigger(player)) return;

    int level = getLevel(getItem(player));
    double probability = 0.25 + ((level * 5) / 100D);
    // Max level can be 5, equals to a .5 chance every time hit - if other conditions met

    if (Math.random() >= probability) return;

    int speedDuration = level * 3;
    PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED, speedDuration * 20, 2);
    //duration multiplied by 20 because its in ticks

    if (player.hasPotionEffect(speedEffect.getType())) {
      player.removePotionEffect(speedEffect.getType());
    }

    player.addPotionEffect(speedEffect);

    int mobFilterRadius = 5 + (3 * level);
    PotionEffect slownessEffect = new PotionEffect(PotionEffectType.SLOW, level * 20, 4);

    player.getNearbyEntities(mobFilterRadius, mobFilterRadius, mobFilterRadius).stream()
        .filter(entity -> entity instanceof Mob)
        .map(entity -> (Mob) entity)
        .filter(mob -> mob.getTarget() != null && mob.getTarget().equals(player))
        .forEach(mob -> {
          mob.addPotionEffect(slownessEffect);
          mob.setTarget(null);
        });

    TextComponent message = new TextComponent("Elusive speed boost applied for " + speedDuration + " seconds");
    message.setColor(ChatColor.GOLD);
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);

    addCooldown(player);
  }
}