package net.jingles.enchantments.tools;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.persistence.EnchantTeam;
import net.jingles.enchantments.statuseffect.container.EntityEffectContainer;
import net.jingles.enchantments.statuseffect.context.ItemEffectContext;
import net.jingles.enchantments.statuseffect.entity.EntityStatusEffect;
import net.jingles.enchantments.util.EnchantUtils;
import net.jingles.enchantments.util.ParticleUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Enchant(name = "Mjolnir", key = "mjolnir", levelRequirement = 30, maxLevel = 1, cooldown = 15, enchantChance = 0.10,
    targetItem = EnchantmentTarget.TOOL, targetGroup = TargetGroup.AXES, description = "Allows the " +
    "user to right click while looking at a block to summon lightning. Alternatively, the user may " +
    "hold crouch and right click to charge a brief flight, which lasts 2 seconds for every second it " +
    "is charged, for a maximum of 10 seconds (20 seconds of flight time). If the player hits the ground " +
    "before the flight duration ends, nearby entities are knocked up and are dealt 10 damage.")

public class Mjolnir extends CustomEnchant {

  public Mjolnir(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(@NotNull LivingEntity entity) {
    ItemStack axe = getItem(entity);

    if (axe == null || !hasEnchantment(axe) ||
        Enchantments.getCooldownManager().hasCooldown(entity, this)) return false;

    Optional<EntityEffectContainer> container = Enchantments.getStatusEffectManager()
        .getEntityContainer(entity.getUniqueId());

    return !container.isPresent() || (!container.get().hasEffect(MjolnirChargeEffect.class)
        && !container.get().hasEffect(MjolnirFlightEffect.class));
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_AIR ||
        !canTrigger(event.getPlayer())) return;

    Player player = event.getPlayer();

    if (!player.isSneaking()) {

      Block targeted = player.getTargetBlockExact(300, FluidCollisionMode.ALWAYS);
      if (targeted != null) {
        LightningStrike lightning = targeted.getWorld().strikeLightning(targeted.getLocation());
        lightning.getWorld().playSound(lightning.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 3, 1);
        lightning.getWorld().playSound(lightning.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 3, 1);
        addCooldown(player);
      }

    } else {
      ItemEffectContext context = new ItemEffectContext(player, getItem(player), this);
      Enchantments.getStatusEffectManager().add(new MjolnirChargeEffect(context, player));
    }

  }

  @EventHandler
  public void onGlideToggle(EntityToggleGlideEvent event) {
    if (!(event.getEntity() instanceof Player)) return;

    Optional<EntityEffectContainer> container = Enchantments.getStatusEffectManager()
        .getEntityContainer(event.getEntity().getUniqueId());

    if (container.isPresent() && container.get().hasEffect(MjolnirFlightEffect.class)) {
      event.setCancelled(true);
    }

  }

  private class MjolnirChargeEffect extends EntityStatusEffect {

    private final Player player;
    private final Particle.DustOptions options;
    private final String charging = "Flight Charge: %d seconds";

    private MjolnirChargeEffect(ItemEffectContext context, Player player) {
      super(player, context, 10 * 20, 20);
      this.player = player;
      this.options = new Particle.DustOptions(Color.YELLOW, 1);
    }

    @Override
    public void effect() {

      int duration = getTicks() / 20;

      TextComponent component = new TextComponent(String.format(charging, duration * 2));
      component.setColor(ChatColor.AQUA);
      player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);

      ParticleUtil.sphere(player.getLocation().add(0, 1, 0), 2, Particle.REDSTONE, options);
      player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 0.5F);

      if (!player.isSneaking() || duration > 10) {
        MjolnirFlightEffect effect = new MjolnirFlightEffect((ItemEffectContext) getContext(), player, duration * 2 * 20);
        Enchantments.getStatusEffectManager().add(effect);
        this.stop();
      }

    }

  }

  private class MjolnirFlightEffect extends EntityStatusEffect {

    private final Player player;
    private final Particle.DustOptions options;
    private final String remaining = "%d seconds of flight remaining";

    private MjolnirFlightEffect(ItemEffectContext context, Player player, int maxTicks) {
      super(player, context, maxTicks, 1);
      this.player = player;
      options = new Particle.DustOptions(Color.WHITE, 3);
    }

    @Override
    public void start() {
      Enchantments.getStatusEffectManager().negateFallDamage(player, Mjolnir.this, getMaxTicks() + 20);
      player.getWorld().playSound(player.getLocation(), Sound.ITEM_ELYTRA_FLYING, 1, 1);
      player.setGliding(true);
    }

    @Override
    public void effect() {
      //Cancel flight is player touches the ground after launching.
      if (getTicks() > 20 && player.isOnGround()) {
        this.stop();
        return;
      }

      TextComponent component = new TextComponent(String.format(remaining, (getMaxTicks() - getTicks()) / 20));
      component.setColor(ChatColor.GOLD);
      player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);

      player.setGliding(true);
      player.setVelocity(player.getEyeLocation().getDirection().multiply(1.35));
      player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation(), 1, options);
    }

    @Override
    public void stop() {
      if (player.isOnGround()) seismicSmash();
      addCooldown(player);

      player.setGliding(false);
      player.stopSound(Sound.ITEM_ELYTRA_FLYING);
      super.stop();
    }

    private void seismicSmash() {

      player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 2, 1);

      EnchantTeam team = EnchantUtils.getEnchantTeam(player);

      player.getWorld().getNearbyEntities(player.getLocation(), 10, 10, 10).stream()
          .filter(entity -> entity instanceof LivingEntity && !entity.equals(player))
          .filter(entity -> !team.isTeamed(entity))
          .map(entity -> (LivingEntity) entity)
          .forEach(entity -> {
            entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_GRASS_BREAK, 2, 1);
            entity.damage(10F, player);
            entity.setVelocity(entity.getVelocity().setY(1.25));
          });

      BlockData data = player.getLocation().subtract(0, 1, 0).getBlock().getBlockData();

      for (int i = 0; i < 10; i++) {
        FallingBlock block = player.getWorld().spawnFallingBlock(player.getLocation().add(0, 1, 0), data);
        block.setDropItem(false);
        block.setHurtEntities(false);

        float x = (float) -1 + (float) (Math.random() * 2);
        float y = (float) 0.5;
        float z = (float) -0.3 + (float) (Math.random() * 2);

        block.setVelocity(new Vector(x, y, z));
      }

    }

  }

}