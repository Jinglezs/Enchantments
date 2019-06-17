package net.jingles.enchantments.armor;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.statuseffect.EntityStatusEffect;
import net.jingles.enchantments.statuseffect.container.EntityEffectContainer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Enchant(name = "Luminosity", key = "luminosity", cooldown = 5, enchantChance = 0.5,
  targetItem = EnchantmentTarget.ARMOR_FEET, description = "Replaces the block under the " +
    "wearer's feet with glowstone every 5 seconds.")

public class Luminosity extends CustomEnchant {

  public Luminosity(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(Inventory inventory, Event event) {
    Player player = ((PlayerMoveEvent) event).getPlayer();
    ItemStack boots = getItem(inventory);

    if (boots == null || !hasEnchantment(boots)) return false;

    EntityEffectContainer container = Enchantments.getStatusEffectManager()
        .getOrNewEntityContainer(player.getUniqueId());

    return player.isOnGround() && !container.hasEffect(LuminosityEffect.class);
  }

  @EventHandler
  public void onPlayerVoid(PlayerMoveEvent event) {
    if (event.getTo().getBlock().equals(event.getFrom().getBlock()) ||
      !canTrigger(event.getPlayer().getInventory(), event)) return;

    Player player = event.getPlayer();
    Enchantments.getStatusEffectManager().add(new LuminosityEffect(player));
  }

  private class LuminosityEffect extends EntityStatusEffect {

    private final Player player;
    private final Location location;
    private Material previous;

    public LuminosityEffect(Player player) {
      super(player, Luminosity.this, 5 * 20, 1);
      this.player = player;
      this.location = player.getLocation().clone().subtract(0, 1, 0);
    }

    @Override
    public void start() {
      this.previous = location.getBlock().getType();
      player.sendBlockChange(location, Material.GLOWSTONE.createBlockData());
    }

    @Override
    public void effect() {
    }

    @Override
    public void stop() {
      player.sendBlockChange(location, previous.createBlockData());
      super.stop();
    }

  }

}
