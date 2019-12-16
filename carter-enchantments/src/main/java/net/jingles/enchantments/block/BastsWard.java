package net.jingles.enchantments.block;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.statuseffect.LocationStatusEffect;

@Enchant(name = "Bast's Ward", key = "basts_ward", maxLevel = 1, targetGroup = TargetGroup.SKULLS,
  cooldown = 25, timeUnit = TimeUnit.SECONDS, description = "When a creeper targets a player near " +
    "an enchanted creeper head, a swarm of ocelots are summoned to scare it away. Additionally, " +
    "any explosion caused by the creeper is negated.")
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

    if (Stream.of(location.getChunk().getTileEntities()).filter(tileEntity -> tileEntity instanceof TileState)
        .map(tileEntity -> (TileState) tileEntity).filter(this::canTrigger).noneMatch(e -> canTrigger(e)))
      return;

    BastsWardEffect effect = new BastsWardEffect((Creeper) event.getEntity(), event.getEntity().getLocation());
    Enchantments.getStatusEffectManager().add(effect);

  }

  @EventHandler
  public void onCreeperExplode(EntityExplodeEvent event) {

    boolean cancel = Enchantments.getStatusEffectManager().getWorldContainer()
      .hasEffectNearLocation(event.getLocation(), 50D, BastsWardEffect.class);

    event.setCancelled(cancel);

  }

  private class BastsWardEffect extends LocationStatusEffect {

    private ArrayList<Ocelot> ocelots = new ArrayList<>();
    private Creeper target;

    public BastsWardEffect(Creeper target, Location location) {
      super(BastsWard.this, 30 * 20, 1, location);
      this.target = target;
    }

    @Override
    public void start() {

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
        this.cancel();
      }

    }

    @Override
    public void stop() {
      ocelots.forEach(Ocelot::remove);
    }

  }

}