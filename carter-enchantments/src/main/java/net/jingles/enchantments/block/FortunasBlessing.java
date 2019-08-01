package net.jingles.enchantments.block;

import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Stream;

@Enchant(name = "Fortuna's Blessing", key = "fortunas_blessing", enchantChance = 0.45, maxLevel = 1,
    targetGroup = TargetGroup.BANNER, description = "While this banner is placed, there is a 45% " +
    "chance that mobs killed within the same chunk will double their drops/experience and the probability " +
    "of their equipment being dropped is increased by 50%.")

public class FortunasBlessing extends BlockEnchant {

  public FortunasBlessing(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean canTrigger(TileState tile) {
    return hasEnchant(tile);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {

    LivingEntity entity = event.getEntity();

    // Gets nearby TileStates with this enchantment
    Optional<TileState> tileState = Stream.of(entity.getLocation().getChunk().getTileEntities())
        .filter(tileEntity -> tileEntity instanceof TileState)
        .map(tileEntity -> (TileState) tileEntity)
        .filter(this::canTrigger)
        .findAny();

    if (!tileState.isPresent()) return;

    // Increase equipment drop chance
    EntityEquipment equipment = entity.getEquipment();
    if (equipment != null && !(entity instanceof Player)) {
      increaseEquipmentDropChances(equipment);
    }

    // Drop every item again ecks dee

    if (Math.random() < 0.45) {
      entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1F, 1F);
      event.getDrops().forEach(item -> entity.getWorld().dropItemNaturally(entity.getLocation(), item));
      event.setDroppedExp(event.getDroppedExp() * 2);
    }

  }

  private void increaseEquipmentDropChances(EntityEquipment equipment) {

    equipment.setItemInMainHandDropChance(getDropChance(equipment.getItemInMainHandDropChance()));
    equipment.setItemInOffHandDropChance(getDropChance(equipment.getItemInOffHandDropChance()));
    equipment.setHelmetDropChance(getDropChance(equipment.getHelmetDropChance()));
    equipment.setChestplateDropChance(getDropChance(equipment.getHelmetDropChance()));
    equipment.setLeggingsDropChance(getDropChance(equipment.getLeggingsDropChance()));
    equipment.setBootsDropChance(getDropChance(equipment.getBootsDropChance()));

  }

  private float getDropChance(float original) {
    return Math.min(1F, original + (original * 0.5F));
  }

}
