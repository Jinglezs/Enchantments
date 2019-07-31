package net.jingles.enchantments.statuseffect;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.statuseffect.container.EffectContainer;
import net.jingles.enchantments.statuseffect.container.EntityEffectContainer;
import net.jingles.enchantments.statuseffect.container.WorldEffectContainer;
import net.jingles.enchantments.statuseffect.effects.FeatherFallingEffect;
import net.jingles.enchantments.statuseffect.entity.EntityStatusEffect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class StatusEffectManager extends BukkitRunnable implements EffectContainer<StatusEffect>, Listener {

  private final Set<StatusEffect> statusEffects = new ConcurrentSkipListSet<>();
  private final Set<EffectContainer> containers = new HashSet<>();
  private final WorldEffectContainer worldContainer;

  public StatusEffectManager(Enchantments plugin) {
    this.worldContainer = new WorldEffectContainer();
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    runTaskTimer(plugin, 0, 1);
  }

  @Override
  public void run() {

    // Synchronized due to concurrent additions/deletions
    Iterator<StatusEffect> iterator = getStatusEffects().iterator();
    while (iterator.hasNext()) {

      StatusEffect effect = iterator.next();
      if (!effect.canTick().isFatal()) {
        effect.effect();
        effect.setNextExecution();
      } else {
        effect.stop();
        iterator.remove();
      }

    }

  }

  @Override
  public Set<StatusEffect> getStatusEffects() {
    return this.statusEffects;
  }

  public Set<EffectContainer> getContainers() {
    return this.containers;
  }

  public Optional<EntityEffectContainer> getEntityContainer(UUID id) {
    return containers.stream()
        .filter(container -> container instanceof EntityEffectContainer)
        .map(container -> (EntityEffectContainer) container)
        .filter(container -> container.getOwner().equals(id))
        .findFirst();
  }

  /**
   * Gets the entity's effect container or creates a new one if not present.
   *
   * @param id the entity's unique id
   * @return the entity's container
   */
  public EntityEffectContainer getOrNewEntityContainer(UUID id) {
    Optional<EntityEffectContainer> container = getEntityContainer(id);

    if (!container.isPresent()) {
      EntityEffectContainer newContainer = new EntityEffectContainer(id);
      containers.add(newContainer);
      return newContainer;
    }

    return container.get();
  }

  /**
   * Gets the container responsible for all Location based
   * status effects that are not attached to entities.
   *
   * @return the world container
   */
  public WorldEffectContainer getWorldContainer() {
    return this.worldContainer;
  }

  public void add(StatusEffect effect) {
    //Creates a new entity container if not exists.
    if (effect instanceof EntityStatusEffect) {
      getOrNewEntityContainer(((EntityStatusEffect) effect).getTargetId());
    }

    getStatusEffects().add(effect);
    effect.start();
  }

  /**
   * Creates a Feather Falling Effect, which negates fall damage.
   * This method automatically starts the effect.
   *
   * @param entity   the target entity
   * @param source   the custom enchant requesting this effect
   * @param duration duration of the effect in ticks
   * @return new Feather Falling Effect.
   */
  public FeatherFallingEffect negateFallDamage(LivingEntity entity, CustomEnchant source, int duration) {
    FeatherFallingEffect effect = new FeatherFallingEffect(entity, source, duration);
    this.add(effect);
    return effect;
  }

  // Events that remove status effects when the entity becomes nullable:

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    removeStatusEffects(event.getPlayer());
  }

  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {
    removeStatusEffects(event.getEntity());
  }

  @EventHandler
  public void onEntityDespawn(ChunkUnloadEvent event) {
    Arrays.asList(event.getChunk().getEntities()).forEach(this::removeStatusEffects);
  }

  private void removeStatusEffects(Entity entity) {
    Enchantments.getStatusEffectManager().getEntityContainer(entity.getUniqueId())
        .ifPresent(container -> container.getStatusEffects().forEach(StatusEffect::stop));
  }

}
