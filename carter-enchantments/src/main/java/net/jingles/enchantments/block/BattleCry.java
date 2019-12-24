package net.jingles.enchantments.block;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.persistence.EnchantTeam;
import net.jingles.enchantments.util.EnchantUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Enchant(name = "Battle Cry", key = "battle_cry", maxLevel = 4, cooldown = 10,
    targetGroup = TargetGroup.BELL, timeUnit = TimeUnit.MINUTES, description = "Ringing the " +
    "enchanted bell alerts all nearby players of the battle to come, preparing them with Health " +
    "Boost 3 and Strength 3 for 30 seconds per level. This enchantment can only be triggered once " +
    "every 10 minutes.")

public class BattleCry extends BlockEnchant {

  public BattleCry(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(@NotNull TileState tile) {
    return hasEnchant(tile) && !Enchantments.getCooldownManager()
        .hasCooldown(tile, this);
  }

  @EventHandler
  public void onBellInteract(PlayerInteractEvent event) {

    Block block = event.getClickedBlock();
    if (block == null || block.getType() != Material.BELL ||
      event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

    // For whatever reason, you can't cast CraftBell to Bell lol
    TileState bell = (TileState) block.getState();
    if (!canTrigger(bell)) return;

    int duration = getLevel(bell) * (30 * 20);
    List<PotionEffect> effects = Arrays.asList(new PotionEffect(PotionEffectType.HEALTH_BOOST, duration, 3, false, false),
        new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration, 3, false, false));

    EnchantTeam team = EnchantUtils.getEnchantTeam(bell);

    bell.getWorld().getNearbyEntities(bell.getLocation(), 100, 100, 100).stream()
        .filter(team::isTeamed)
        .filter(e -> e instanceof Player)
        .map(e -> (Player) e)
        .forEach(player -> player.addPotionEffects(effects));

    bell.getWorld().playSound(bell.getLocation(), Sound.EVENT_RAID_HORN, 10f, 2f);

  }

}
