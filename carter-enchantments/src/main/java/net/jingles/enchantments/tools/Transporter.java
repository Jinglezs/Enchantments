package net.jingles.enchantments.tools;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

@Enchant(name = "Transporter", key = "transporter", enchantChance = 0.7, levelRequirement = 15,
    targetGroup = TargetGroup.LEAD, description = "Leading an entity while sneaking causes them to " +
    "be trapped within the lead, allowing the user to easily transport the entity to a new location. " +
    "Trapped entities can be released by right clicking a block.")

public class Transporter extends CustomEnchant {

  private final NamespacedKey entityType;
  private final NamespacedKey customName;

  public Transporter(NamespacedKey key) {
    super(key);
    this.entityType = Enchantments.getEnchantmentManager().createKey("entity_type");
    this.customName = Enchantments.getEnchantmentManager().createKey("custom_name");
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(@NotNull LivingEntity entity) {
    return ((Player) entity).isSneaking() && hasEnchantment(getItem(entity));
  }

  @EventHandler
  public void onEntityInteract(PlayerInteractEvent event) {

    if (event.getItem() == null || !canTrigger(event.getPlayer())) return;

    Player player = event.getPlayer();
    ItemStack item = event.getItem();
    ItemMeta meta = item.getItemMeta() != null ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
    PersistentDataContainer container = meta.getPersistentDataContainer();

    event.setCancelled(true);

    if (container.has(entityType, PersistentDataType.STRING) && event.getClickedBlock() != null) {

      EntityType type = EntityType.valueOf(container.get(entityType, PersistentDataType.STRING));
      Entity entity = player.getWorld().spawnEntity(event.getClickedBlock().getLocation().add(0, 1, 0), type);

      if (container.has(customName, PersistentDataType.STRING)) {
        String name = container.get(customName, PersistentDataType.STRING);
        entity.setCustomNameVisible(true);
        entity.setCustomName(name);
      }

    }

  }

  @EventHandler
  public void onEntityInteract(PlayerInteractEntityEvent event) {

    if (!canTrigger(event.getPlayer())) return;

    // Prevent the player from leashing the entity
    event.setCancelled(true);

    Entity entity = event.getRightClicked();
    ItemStack item = getItem(event.getPlayer());

    ItemMeta meta = item.getItemMeta();
    PersistentDataContainer container = meta.getPersistentDataContainer();

    container.set(entityType, PersistentDataType.STRING, entity.getType().name());

    if (entity.getCustomName() != null) {
      container.set(customName, PersistentDataType.STRING, entity.getCustomName());
    }

    item.setItemMeta(meta);
    entity.remove();

  }


}
