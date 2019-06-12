package net.jingles.enchantments.weapon;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Enchant(name = "Egg Hunter", key = "egg_hunter", levelRequirement = 30, maxLevel = 3,
  targetItem = EnchantmentTarget.WEAPON, description = "Upon killing any living entity, " +
    "there is a 10% chance per level that its spawn egg will be added to the drops.")

public class EggHunter extends CustomEnchant {

  private final Map<EntityType, Material> SPAWN_EGGS = new HashMap<>();

  public EggHunter(NamespacedKey key) {
    super(key);

    Stream.of(Material.values())
        .filter(material -> material.name().endsWith("_SPAWN_EGG"))
        .forEach(material -> {
          try {
            EntityType type = EntityType.valueOf(material.name().replace("_SPAWN_EGG", ""));
            SPAWN_EGGS.put(type, material);
          } catch (IllegalArgumentException e) {
            // There is no EntityType with that name!
          }
        });
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(Inventory inventory, Event event) {
    ItemStack sword = getItem(inventory);
    return sword != null && hasEnchantment(sword);
  }

  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {
    if (event.getEntity().getKiller() == null) return;

    Player player = event.getEntity().getKiller();

    if (!SPAWN_EGGS.containsKey(event.getEntityType()) || !canTrigger(player.getInventory(), event)) return;

    int level = getItem(player.getInventory()).getItemMeta().getEnchantLevel(this);
    double probability = (10 * level) / 100D;

    if (Math.random() <= probability)
      event.getDrops().add(new ItemStack(SPAWN_EGGS.get(event.getEntityType()), 1));
  }

}
