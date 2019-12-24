package net.jingles.enchantments.statuseffect;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.statuseffect.StatusEffect.TickFailure;
import net.jingles.enchantments.statuseffect.container.EffectContainer;
import net.jingles.enchantments.statuseffect.container.EntityEffectContainer;
import net.jingles.enchantments.statuseffect.container.WorldEffectContainer;
import net.jingles.enchantments.statuseffect.context.ItemEffectContext;
import net.jingles.enchantments.statuseffect.effects.FeatherFallingEffect;
import net.jingles.enchantments.statuseffect.entity.EntityStatusEffect;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

public class StatusEffectManager extends BukkitRunnable implements EffectContainer<StatusEffect>, Listener {

  private final Set<StatusEffect> statusEffects = new ConcurrentSkipListSet<>();
  private final Set<EffectContainer> containers = new HashSet<>();
  private final WorldEffectContainer worldContainer;

  public StatusEffectManager(@NotNull Enchantments plugin) {
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
      TickFailure canTick = effect.canTick();

      if (canTick.isFatal()) iterator.remove();
      else if (canTick == TickFailure.SUCCESS) {
        effect.effect();
        effect.setNextExecution();
      }

    }

  }

  @Override
  @NotNull
  public Set<StatusEffect> getStatusEffects() {
    return this.statusEffects;
  }

  @NotNull
  public Set<EffectContainer> getContainers() {
    return this.containers;
  }

  @NotNull
  public Optional<EntityEffectContainer> getEntityContainer(UUID id) {
    return getContainers().stream()
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
  @NotNull
  public EntityEffectContainer getOrNewEntityContainer(@NotNull UUID id) {
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
  @NotNull
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
  @NotNull
  public FeatherFallingEffect negateFallDamage(@NotNull LivingEntity entity, @NotNull CustomEnchant source, int duration) {
    FeatherFallingEffect effect = new FeatherFallingEffect(entity, new ItemEffectContext(entity, null, source), duration);
    this.add(effect);
    return effect;
  }

  public void cancelAll() {
    getStatusEffects().forEach(StatusEffect::cancel);
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

  // The chunk events make calls to BlockEnchant#onChunkLoad() and
  // BlockEnchant#onChunkUnload for more control over how chunk
  // loading affects block enchantments.

  @EventHandler
  public void onChunkLoad(ChunkLoadEvent event) {
    Enchantments.getEnchantmentManager().loadBlockEnchants(event.getChunk());
  }

  @EventHandler
  public void onChunkUnload(ChunkUnloadEvent event) {

    Arrays.asList(event.getChunk().getEntities()).forEach(this::removeStatusEffects);

    Stream.of(event.getChunk().getTileEntities())
        .filter(state -> state instanceof TileState)
        .map(state -> (TileState) state)
        .forEach(tile -> {

          // Unload the enchantments and cancel all effects that stem from them.
          BlockEnchant.getBlockEnchants(tile.getPersistentDataContainer())
              .keySet()
              .forEach(enchant -> {
                enchant.onChunkUnload(tile);
                getEffectsBySource(enchant).forEach(StatusEffect::cancel);
              });

        });

  }

  private void removeStatusEffects(Entity entity) {
    Enchantments.getStatusEffectManager().getEntityContainer(entity.getUniqueId())
        .ifPresent(container -> container.getStatusEffects().forEach(StatusEffect::cancel));
  }

}
