package net.jingles.enchantments.armor.leggings;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.statuseffect.EntityStatusEffect;
import net.jingles.enchantments.statuseffect.container.EntityEffectContainer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

@Enchant(name = "Thiccness", key = "thiccness", targetItem = EnchantmentTarget.ARMOR_LEGS, cooldown = 3,
    enchantChance = 0.45, description = "Allows the player to use the strength of their thicc thighs to " +
    "supercharge the power of their jump, increasing their jump height dramatically.")

public class Thiccness extends CustomEnchant {

  public Thiccness(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(Player player) {

    ItemStack leggings = getItem(player.getInventory());
    if (leggings == null || !hasEnchantment(leggings) ||
        Enchantments.getCooldownManager().hasCooldown(player, this))
      return false;

    Optional<EntityEffectContainer> container = Enchantments.getStatusEffectManager()
        .getEntityContainer(player.getUniqueId());

    return !container.isPresent() || !container.get().hasEffect(ThiccnessChargeEffect.class);
  }

  @EventHandler
  public void onPlayerCrouch(PlayerToggleSneakEvent event) {

    if (!canTrigger(event.getPlayer())) return;

    Player player = event.getPlayer();

    if (player.isOnGround() && event.isSneaking())
      Enchantments.getStatusEffectManager().add(new ThiccnessChargeEffect(player));

  }

  private class ThiccnessChargeEffect extends EntityStatusEffect {

    private final Player player;
    private double charge;

    private ThiccnessChargeEffect(Player player) {
      super(player, Thiccness.this, Integer.MAX_VALUE, 5);
      this.player = player;
    }

    @Override
    public void effect() {
      if (charge < 350) charge += 5.5;

      TextComponent message = new TextComponent("Thiccness Power: " + charge + "%");
      message.setColor(ChatColor.GREEN);
      player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);

      if (!player.isSneaking()) this.stop();
    }

    @Override
    public void stop() {
      TextComponent message = new TextComponent("RELEASE THE THICCNESS");
      message.setColor(ChatColor.RED);
      player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);

      //Negate fall damage for a maximum of 20 seconds.
      Enchantments.getStatusEffectManager().negateFallDamage(player, Thiccness.this, 20 * 20);
      player.setVelocity(player.getVelocity().setY((charge / 75)));

      addCooldown(player);

      super.stop();
    }

  }

}