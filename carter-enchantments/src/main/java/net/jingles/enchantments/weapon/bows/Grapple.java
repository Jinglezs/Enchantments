package net.jingles.enchantments.weapon.bows;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.util.InventoryUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@Enchant(name = "Grapple", key = "grapple", targetItem = EnchantmentTarget.BOW, maxLevel = 1, cooldown = 2,
    enchantChance = 0.50, description = "Allows the user to launch an arrow by left clicking. When it hits a " +
    "target, the user is quickly propelled in its direction.")

public class Grapple extends CustomEnchant {

  public Grapple(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(LivingEntity entity) {
    ItemStack bow = getItem(entity);
    return bow != null && hasEnchantment(bow);
  }

  @EventHandler
  public void onBowLeftClick(PlayerInteractEvent event) {
    if (event.getAction() != Action.LEFT_CLICK_AIR ||
        !canTrigger(event.getPlayer())) return;

    Player player = event.getPlayer();

    if (!player.getInventory().contains(Material.ARROW)) return;

    Location eye = player.getEyeLocation();
    Arrow arrow = player.getWorld().spawnArrow(eye.add(eye.getDirection()), eye.getDirection(), 3F, 0F);
    arrow.getPersistentDataContainer().set(getKey(), PersistentDataType.INTEGER, 1);
    arrow.setShooter(player);

    InventoryUtils.removeItem(player.getInventory(), Tag.ITEMS_ARROWS, 1);
    Enchantments.getCooldownManager().addCooldown(player,
        this, getCooldown(), getTimeUnit());
  }

  @EventHandler
  public void onArrowHit(ProjectileHitEvent event) {
    if (!(event.getEntity() instanceof Arrow) ||
        !(event.getEntity().getShooter() instanceof Player)) return;

    Player player = (Player) event.getEntity().getShooter();
    Arrow arrow = (Arrow) event.getEntity();

    if (!arrow.getPersistentDataContainer().has(getKey(), PersistentDataType.INTEGER) ||
        !canTrigger(player)) return;

    if (player.getLocation().distance(arrow.getLocation()) > 75) return;

    Enchantments.getStatusEffectManager().negateFallDamage(player, this, 100);
    Vector difference = arrow.getLocation().toVector().subtract(player.getLocation().toVector());
    player.setVelocity(difference.multiply(0.15));
  }

}
