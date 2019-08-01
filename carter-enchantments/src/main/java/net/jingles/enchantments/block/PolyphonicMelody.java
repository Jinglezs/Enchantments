package net.jingles.enchantments.block;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.statuseffect.entity.PotionStatusEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@Enchant(name = "Polyphonic Melody", key = "polyphonic_melody", enchantChance = 0.70, cooldown = 1,
    timeUnit = TimeUnit.MINUTES, targetGroup = TargetGroup.JUKEBOX, description = "While this jukebox " +
    "is playing music, entities within hearing range receive different effects based on the track being played.")

public class PolyphonicMelody extends BlockEnchant {

  public PolyphonicMelody(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean canTrigger(@NotNull TileState tile) {
    return hasEnchant(tile);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @EventHandler
  public void onJukeboxPlay(PlayerInteractEvent event) {

    Block block = event.getClickedBlock();
    ItemStack item = event.getItem();

    if (event.getAction() != Action.RIGHT_CLICK_BLOCK || block == null ||
        block.getType() != Material.JUKEBOX) return;

    Jukebox jukebox = (Jukebox) block.getState();
    if (!canTrigger(jukebox)) return;

    if (item != null && !jukebox.isPlaying() && item.getType().isRecord()) {

      if (!Enchantments.getCooldownManager().hasCooldown(jukebox, this)) {

        // The sound of jukeboxes is audible for about 65 blocks in every direction.
        jukebox.getWorld().getNearbyEntities(jukebox.getLocation(), 65, 65, 65).stream()
            .filter(entity -> entity instanceof LivingEntity)
            .map(entity -> (LivingEntity) entity)
            .forEach(entity -> {
              PotionEffect effect = new PotionEffect(getType(item.getType()), getDuration(item.getType()), 1, false);
              Enchantments.getStatusEffectManager().add(new PolyphonicStatusEffect(jukebox, effect, entity));
            });

        addCooldown(jukebox);

      }

    } else if (jukebox.isPlaying()) { // if the current disc being played is stopped

      Enchantments.getStatusEffectManager().getEffectsBySource(this).stream()
          .map(effect -> (PolyphonicStatusEffect) effect)
          .filter(effect -> effect.getJukebox().equals(jukebox))
          .forEach(PotionStatusEffect::stop);

    }

  }

  private class PolyphonicStatusEffect extends PotionStatusEffect {

    private final Jukebox jukebox;
    private final int MAX_DISTANCE = 65 * 65;

    private PolyphonicStatusEffect(Jukebox jukebox, PotionEffect potionEffect, LivingEntity target) {
      super(potionEffect, target, PolyphonicMelody.this, 20);
      this.jukebox = jukebox;
    }

    public Jukebox getJukebox() {
      return this.jukebox;
    }

    @Override
    public void effect() {

      if (getTarget().getLocation().distanceSquared(jukebox.getLocation()) > MAX_DISTANCE)
        this.stop();

    }

  }

  private PotionEffectType getType(Material disc) {

    switch (disc) {
      // Time to be scared for your life
      case MUSIC_DISC_11: return PotionEffectType.BLINDNESS;
      // Creepy asf
      case MUSIC_DISC_13: return PotionEffectType.BAD_OMEN;
      // this doesn't really correlate with anything lol
      case MUSIC_DISC_BLOCKS: return PotionEffectType.DAMAGE_RESISTANCE;
      // Upbeat
      case MUSIC_DISC_CAT: return PotionEffectType.SPEED;
      // Sounds like people working... don't ask
      case MUSIC_DISC_CHIRP: return PotionEffectType.FAST_DIGGING;
      // Sounds like someone falling into a black hole, but calmly
      case MUSIC_DISC_FAR: return PotionEffectType.SLOW_FALLING;
      // Peaceful melody
      case MUSIC_DISC_MALL: return PotionEffectType.REGENERATION;
      // Slow and somber, like you're sitting there bored asf fishing
      case MUSIC_DISC_MELLOHI: return PotionEffectType.LUCK;
      // Sounds like some dude creeping around
      case MUSIC_DISC_STAL: return PotionEffectType.INVISIBILITY;
      // Tropical stuff
      case MUSIC_DISC_STRAD: return PotionEffectType.GLOWING;
      // Kind of sounds like bubbles popping
      case MUSIC_DISC_WAIT: return PotionEffectType.WATER_BREATHING;
      // kinda sad kinda not - like you defeated the pillagers but died 34214 times
      case MUSIC_DISC_WARD: return PotionEffectType.HERO_OF_THE_VILLAGE;

      default: return PotionEffectType.ABSORPTION;
    }

  }

  // Gets the duration of the music disc in ticks
  private int getDuration(Material disc) {

    switch (disc) {

      case MUSIC_DISC_11: return 1420;
      case MUSIC_DISC_13: return 3560;
      case MUSIC_DISC_BLOCKS:return 6900;

      case MUSIC_DISC_CAT:
      case MUSIC_DISC_CHIRP:
        return 3700;

      case MUSIC_DISC_FAR: return 3480;
      case MUSIC_DISC_MALL: return 3940;
      case MUSIC_DISC_MELLOHI: return 1920;
      case MUSIC_DISC_STAL: return 3000;
      case MUSIC_DISC_STRAD: return 3760;
      case MUSIC_DISC_WAIT: return 4720;
      case MUSIC_DISC_WARD: return 5020;

      default: return 0;

    }

  }

}
