package net.jingles.enchantments.tools;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.statuseffect.container.EntityEffectContainer;
import net.jingles.enchantments.statuseffect.context.ItemEffectContext;
import net.jingles.enchantments.statuseffect.entity.EntityStatusEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Enchant(name = "Sensitivity", key = "sensitivity", maxLevel = 7, targetGroup = TargetGroup.PICKAXES, cooldown = 25,
    description = "While mining underground, the enchanted pickaxe is sensitive to the position of nearby ores. " +
        "The sensed ore will glow in reaction to the pickaxe's presence for 30 seconds. The rarity of ores that " +
        "can be sensed by this enchantment is dependent on the enchantment level, ranging from 1 (coal ore) to " +
        "7 (diamond ore).")

public class Sensitivity extends CustomEnchant {

  private static final Map<Material, Integer> ORES = new HashMap<>();

  public Sensitivity(NamespacedKey key) {
    super(key);
    ORES.put(Material.DIAMOND_ORE, 7);
    ORES.put(Material.EMERALD_ORE, 6);
    ORES.put(Material.GOLD_ORE, 5);
    ORES.put(Material.IRON_ORE, 4);
    ORES.put(Material.LAPIS_ORE, 3);
    ORES.put(Material.REDSTONE_ORE, 2);
    ORES.put(Material.COAL_ORE, 1);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(@NotNull LivingEntity entity) {

    if (!hasEnchantment(getItem(entity))) return false;

    Optional<EntityEffectContainer> container = Enchantments.getStatusEffectManager()
        .getEntityContainer(entity.getUniqueId());

    return !container.isPresent() || !container.get().hasEffect(SensitivityEffect.class);
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {

    Player player = event.getPlayer();
    Location location = player.getLocation();

    // This should only be true if the player is not below a block
    if (!canTrigger(event.getPlayer()) || player.getWorld()
        .rayTraceBlocks(player.getEyeLocation(), new Vector(0, 1, 0), 30) == null) return;

    ItemStack item = getItem(player);
    int level = getLevel(item);

    getNearbyBlocks(location, 30).stream()
        .filter(block -> ORES.containsKey(block.getType()) && ORES.get(block.getType()) <= level)
        .sorted(Comparator.comparingDouble(block -> block.getLocation().distanceSquared(player.getLocation())))
        .max(Comparator.comparing(block -> ORES.get(block.getType())))
        .ifPresent(block -> {
          ItemEffectContext context = new ItemEffectContext(player, item, this);
          Enchantments.getStatusEffectManager().add(new SensitivityEffect(context, player, block));
        });

  }

  private List<Block> getNearbyBlocks(Location location, int radius) {
    List<Block> blocks = new ArrayList<>();
    for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
      for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
        for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
          blocks.add(location.getWorld().getBlockAt(x, y, z));
        }
      }
    }
    return blocks;
  }

  private class SensitivityEffect extends EntityStatusEffect {

    private final Block block;
    private final Material targetType;
    private final Shulker shulker;

    private SensitivityEffect(ItemEffectContext context, Player target, Block block) {
      super(target, context, 600, 5);
      this.block = block;
      this.targetType = block.getType();
      this.shulker = (Shulker) getTarget().getWorld().spawnEntity(block.getLocation(), EntityType.SHULKER);
    }

    @Override
    public void start() {

      LivingEntity player = getTarget();
      player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1F, 1F);

      addCooldown(player);
      shulker.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, getMaxTicks(), 3, false, false));
      shulker.setGlowing(true);
      shulker.setInvulnerable(true);
      shulker.setAI(false);

    }

    @Override
    public void effect() {

      Block targeted = getTarget().getTargetBlockExact(5);

      if (block.getType() != targetType || (targeted != null && targeted.equals(block))) {
        this.stop();
      }

    }

    @Override
    public void stop() {
      super.stop();
      shulker.remove();
    }

  }

}
