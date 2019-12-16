package net.jingles.enchantments.block;

import java.util.stream.Stream;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.statuseffect.LocationStatusEffect;

//TODO: Add mob head target group.
@Enchant(name = "Bast's Ward", key = "basts_ward", maxLevel = 1, targetGroup = TargetGroup.TILE_ENTITY,
  description = "When a creeper targets a player near an enchanted creeper head, a swarm of ocelots " +
    "are summoned to scare it away.")
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

    if (!(event.getTarget() instanceof Player) || !(event.getEntity() instanceof Creeper)) return;

    Location location = event.getEntity().getLocation();

    if (Stream.of(location.getChunk().getTileEntities())
        .filter(tileEntity -> tileEntity instanceof TileState)
        .map(tileEntity -> (TileState) tileEntity)
        .filter(this::canTrigger)
        .noneMatch(e -> canTrigger(e))) return;

     for (int i = 0; i < 3; i++) {
      BastsWardEffect effect = new BastsWardEffect((Creeper) event.getEntity(), event.getEntity().getLocation());
      Enchantments.getStatusEffectManager().add(effect);
     }   

  }

  private class BastsWardEffect extends LocationStatusEffect {

    private LivingEntity target;
    private Ocelot ocelot;

    public BastsWardEffect(LivingEntity target, Location location) {
      super(BastsWard.this, 30 * 20, 1, location);
      this.target = target;
    }

    @Override
    public void start() {
      this.ocelot = (Ocelot) getLocation().getWorld().spawnEntity(getLocation(), EntityType.OCELOT);
      ocelot.setTarget(target);
    }

    @Override
    public void effect() {

      if (target == null || target.isDead() || target.getLocation().distanceSquared(ocelot.getLocation()) > 100)
        this.cancel();

    }

    @Override
    public void stop() {
      ocelot.remove();
    }
    
  }

}