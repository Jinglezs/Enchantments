package net.jingles.enchantments.block;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.statuseffect.LocationStatusEffect;
import net.jingles.enchantments.statuseffect.context.TileEntityContext;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import java.util.ArrayList;
import java.util.stream.Stream;

@Enchant(name = "Bast's Ward", key = "basts_ward", maxLevel = 1, targetGroup = TargetGroup.SKULLS,
  cooldown = 25, description = "When a creeper targets a player near an enchanted creeper head, " +
    "a swarm of ocelots are summoned to scare it away. Additionally, any explosion caused by the " +
    "creeper is negated.")
public class BastsWard extends BlockEnchant {

  public BastsWard(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean canTrigger(TileState tile) {
    return hasEnchant(tile);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @EventHandler
  public void onCreeperTarget(EntityTargetLivingEntityEvent event) {

    if (!(event.getTarget() instanceof Player) || !(event.getEntity() instanceof Creeper))
      return;

    Location location = event.getEntity().getLocation();

    Stream.of(location.getChunk().getTileEntities())
        .filter(tileEntity -> tileEntity instanceof TileState)
        .map(tileEntity -> (TileState) tileEntity).filter(this::canTrigger)
        .filter(this::canTrigger)
        .forEach(tile -> {

          TileEntityContext context = new TileEntityContext(tile, this);
          BastsWardEffect effect = new BastsWardEffect(context, (Creeper) event.getEntity(), event.getEntity().getLocation());
          Enchantments.getStatusEffectManager().add(effect);

        });

  }

  @EventHandler
  public void onCreeperExplode(EntityExplodeEvent event) {

    boolean cancel = Enchantments.getStatusEffectManager().getWorldContainer()
      .hasEffectNearLocation(event.getLocation(), 50D, BastsWardEffect.class);

    event.setCancelled(cancel);

  }

  private static class BastsWardEffect extends LocationStatusEffect {

    private ArrayList<Ocelot> ocelots = new ArrayList<>();
    private Creeper target;

    private BastsWardEffect(TileEntityContext context, Creeper target, Location location) {
      super(context, 30 * 20, 1, location);
      this.target = target;
    }

    @Override
    public void start() {

      target.getWorld().playSound(target.getLocation(), Sound.ENTITY_CAT_HISS, 1F, 1F);

      for (int i = 0; i < 3; i++) {
        Ocelot ocelot = (Ocelot) getLocation().getWorld().spawnEntity(getLocation(), EntityType.OCELOT);
        ocelots.add(ocelot);
        ocelot.setTarget(target);
      }

    }

    @Override
    public void effect() {

      if (target == null || target.isDead() || ocelots.stream()
        .noneMatch(o -> o.getLocation().distanceSquared(target.getLocation()) < 25 * 25)) {
        this.stop();
      }

    }

    @Override
    public void stop() {
      ocelots.forEach(Ocelot::remove);
    }

  }

}