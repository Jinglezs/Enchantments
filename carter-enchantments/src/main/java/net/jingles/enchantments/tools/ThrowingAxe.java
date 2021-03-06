package net.jingles.enchantments.tools;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.statuseffect.container.EntityEffectContainer;
import net.jingles.enchantments.statuseffect.context.ItemEffectContext;
import net.jingles.enchantments.statuseffect.entity.EntityStatusEffect;
import net.jingles.enchantments.statuseffect.entity.PotionStatusEffect;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.text.NumberFormat;

@Enchant(name = "Throwing Axe", key = "throwing_axe", targetGroup = TargetGroup.AXES, maxLevel = 1,
  cooldown = 13, description = "Shift right clicking allows the user to begin charging an axe throw. " +
    "Shift right clicking again will throw the axe with the accumulated force. The axe travels in a parabolic " +
    "motion and deals damage equivalent to the axe's normal damage multiplied by the force of the throw. " +
    "Enchantments such as sharpness do not affect the final damage.")

public class ThrowingAxe extends CustomEnchant {

  private static final PotionEffect SLOW = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 2);

  public ThrowingAxe(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return other.getClass() == Mjolnir.class;
  }

  @Override
  public boolean canTrigger(LivingEntity entity) {
    return hasEnchantment(getItem(entity));
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {

    if (!event.getPlayer().isSneaking() || event.getAction() != Action.RIGHT_CLICK_AIR ||
      !canTrigger(event.getPlayer())) return;

    Player player = event.getPlayer();  

    EntityEffectContainer container = Enchantments.getStatusEffectManager()
      .getOrNewEntityContainer(player.getUniqueId());  

    boolean isCharging = container.hasEffect(ThrowingAxeChargeEffect.class);  

    ThrowingAxeChargeEffect effect = container.getEffectsBySource(this).stream()
      .findFirst()
      .map(e -> (ThrowingAxeChargeEffect) e)
      .orElse(new ThrowingAxeChargeEffect(new ItemEffectContext(player, getItem(player), this), player));

    if (isCharging) effect.stop();
    else Enchantments.getStatusEffectManager().add(effect);  

  }

  private class ThrowingAxeChargeEffect extends PotionStatusEffect {

    private boolean throwAxe = true;
    private ItemStack axe;
    private double charge = 0.5;

    private ThrowingAxeChargeEffect(ItemEffectContext context, LivingEntity target) {
      super(SLOW, target, context, 5);
      this.axe = context.getItem();
    }

    @Override
    public void effect() {

      Player player = (Player) getTarget();

      if (!player.getInventory().getItemInMainHand().equals(axe)) {
        this.throwAxe = false;
        this.stop();
        return;
      }

      if (charge < 1.85) {
        charge += 0.15;
      }

      float pitch = Math.min(1.55F, (float) charge);
      player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5F, pitch);

      NumberFormat format = NumberFormat.getPercentInstance(); 
      TextComponent component = new TextComponent("Axe Throw Power: " + format.format(charge));
      component.setColor(ChatColor.RED);

      player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
     
    }

    @Override
    public void stop() {

      super.stop();

      if (throwAxe) {

        Location loc = getTarget().getEyeLocation();
        Item item = loc.getWorld().dropItem(loc, axe);
        item.setPickupDelay(Integer.MAX_VALUE);
        item.setGlowing(true);

        ThrowingAxeThrowEffect effect = new ThrowingAxeThrowEffect((ItemEffectContext) getContext(), (Player) getTarget(), charge, item);
        Enchantments.getStatusEffectManager().add(effect);

      }

    }

  }

  private class ThrowingAxeThrowEffect extends EntityStatusEffect {

    private final double launchForce, damage;
    private final Item axe;

    private ThrowingAxeThrowEffect(ItemEffectContext context, Player owner, double force, Item axe) {
      super(owner, context, 25 * 20, 1);
      this.launchForce = force;
      this.axe = axe;

      double defaultDamage;

      switch (axe.getItemStack().getType()) {
        case GOLDEN_AXE:
        case WOODEN_AXE:
          defaultDamage = 7;
          break;
        default: defaultDamage = 9;
      }

      this.damage = defaultDamage * force;

    }

    @Override
    public void start() {

      Player owner = (Player) getTarget();

      owner.playSound(owner.getLocation(), Sound.ITEM_TRIDENT_THROW, 1F, 1F);
      owner.getInventory().setItemInMainHand(null);

      Vector initialVelocity = owner.getEyeLocation().getDirection().normalize().multiply(launchForce);
      axe.setVelocity(initialVelocity); 

    }

    @Override
    public void effect() {
      
      if (axe.isOnGround()) {
        this.stop();
      }

      // Add gravity and drag to the item's velocity
      Vector recalculated = axe.getVelocity().multiply(0.99); // Apply drag force
      recalculated = recalculated.setY(recalculated.getY() - 0.05); // Apply gravity
      axe.setVelocity(recalculated);

      Location center = axe.getBoundingBox().getCenter().toLocation(getTarget().getWorld());
      axe.getWorld().spawnParticle(Particle.SWEEP_ATTACK, center, 1);

      // Check for collisions.
      axe.getWorld().getNearbyEntities(center, 0.85, 0.85, 0.85).stream()
        .filter(e -> !e.equals(getTarget()) && !e.equals(axe))
        .filter(e -> e instanceof LivingEntity)
        .map(e -> (LivingEntity) e)
        .findAny()
        .ifPresent(e -> {

          e.getWorld().playSound(e.getLocation(), Sound.ITEM_TRIDENT_HIT, 1F, 1F);
          e.damage(damage);

          this.stop();

        }); 
      
    }

    public void stop() {
      super.stop();
      axe.setVelocity(new Vector(0, 0, 0));
      axe.setPickupDelay(0);
    }

  }

}