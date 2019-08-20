package net.jingles.enchantments.tools;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.bukkit.StructureType.*;

@Enchant(name = "Omniscient", key = "omniscient", levelRequirement = 30, maxLevel = 1,
    targetGroup = TargetGroup.COMPASS, cooldown = 3, timeUnit = TimeUnit.MINUTES,
    description = "This compass is capable of locating nearly all of the structures within " +
        "the game. Left clicking switches which type of structure to search for, while right " +
        "clicking causes the compass to point towards the closest target structure.")

public class Omniscient extends CustomEnchant {

  private static final String NOTICE = "You are now searching for: " + ChatColor.AQUA + "%s";
  private final Map<Integer, StructureType> structures = new HashMap<>();
  private final NamespacedKey structureKey;
  private final NamespacedKey targetKey;

  public Omniscient(NamespacedKey key) {
    super(key);
    structures.put(1, STRONGHOLD);
    structures.put(2, OCEAN_MONUMENT);
    structures.put(3, SHIPWRECK);
    structures.put(4, VILLAGE);
    structures.put(5, DESERT_PYRAMID);
    structures.put(6, JUNGLE_PYRAMID);

    structureKey = Enchantments.getEnchantmentManager().createKey("structure_type");
    targetKey = Enchantments.getEnchantmentManager().createKey("has_target");
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(@NotNull LivingEntity entity) {
    return hasEnchantment(getItem(entity));
  }

  @EventHandler
  public void onCompassInteract(PlayerInteractEvent event) {

    if (!canTrigger(event.getPlayer())) return;

    Player player = event.getPlayer();
    ItemStack item = getItem(player);
    Action action = event.getAction();

    ItemMeta meta = item.getItemMeta() != null ? item.getItemMeta() :
        Bukkit.getItemFactory().getItemMeta(item.getType());
    PersistentDataContainer container = meta.getPersistentDataContainer();

    // Gets the StructureType the compass is currently set to locate.
    String value = container.get(structureKey, PersistentDataType.STRING);
    StructureType current = getStructureTypes().get(value);

    // Changes the target StructureType
    if (action == Action.LEFT_CLICK_AIR) {

      // Cycles to the next StructureType and saves it to the item
      StructureType type = getNextStructure(current);
      container.set(structureKey, PersistentDataType.STRING, type.getName());
      container.remove(targetKey);
      item.setItemMeta(meta);

      TextComponent notice = new TextComponent(String.format(NOTICE, type.getName().replace("_", " ")));
      player.spigot().sendMessage(notice);

      // Actually locates the closest structure and sets the compass' target.
    } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {

      if (Enchantments.getCooldownManager().hasCooldown(player, this) ||
        container.has(targetKey, PersistentDataType.BYTE)) return;

      Location location = player.getWorld().locateNearestStructure(player.getLocation(), current, 50, false);

      if (location == null) {

        TextComponent component = new TextComponent(ChatColor.RED + "A structure could not be found within 50 chunks!");
        player.spigot().sendMessage(component);

      } else {

        player.setCompassTarget(location);
        container.set(targetKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);

        TextComponent component = new TextComponent(ChatColor.GREEN + "Your compass' target has been updated!");
        player.spigot().sendMessage(component);

      }

    }

  }

  private StructureType getNextStructure(StructureType type) {
    int index = structures.entrySet().stream()
        .filter(entry -> entry.getValue() == type)
        .map(Map.Entry::getKey)
        .findFirst()
        .orElse(1);

    return (index + 1 > structures.size()) ? structures.get(1) : structures.get(index + 1);
  }

}
