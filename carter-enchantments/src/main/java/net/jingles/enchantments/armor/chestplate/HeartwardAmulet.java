package net.jingles.enchantments.armor.chestplate;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

@Enchant(name = "Heartward Amulet", key = "heartward_amulet", levelRequirement = 30, enchantChance = 0.30,
  maxLevel = 1, targetItem = EnchantmentTarget.ARMOR_TORSO, description = "Upon death, the wearer's items are " +
  "not dropped, but placed inside a chest at their current location. The wearer will respawn with a paper that " +
  "contains the coordinates of their death location. This enchantment is removed from the item upon use.")

public class HeartwardAmulet extends CustomEnchant {

  public HeartwardAmulet(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(Inventory inventory, Event event) {
    ItemStack chestplate = getItem(inventory);
    return chestplate != null && hasEnchantment(chestplate);
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    if (!canTrigger(event.getEntity().getInventory(), event)) return;

    Player player = event.getEntity();

    ItemStack chestplate = getItem(player.getInventory());
    InventoryUtils.removeEnchantLore(chestplate, this);
    chestplate.removeEnchantment(this);

    Block block = player.getWorld().getBlockAt(player.getLocation().add(0, 1, 0));
    block.setType(Material.CHEST);
    block.getState().update();

    Chest chest = (Chest) block.getState();
    int chestSize = chest.getBlockInventory().getSize();

    ItemStack[] items = Stream.of(player.getInventory().getContents())
        .filter(Objects::nonNull).filter(item -> item.getType() != Material.AIR)
        .toArray(ItemStack[]::new);

    if (items.length > chestSize) {

      ItemStack[] setOne = Arrays.copyOfRange(items, 0, chestSize);
      ItemStack[] setTwo = Arrays.copyOfRange(items, chestSize, items.length);

      Block block2 = block.getRelative(BlockFace.UP);
      block2.setType(Material.CHEST);
      block2.getState().update();

      chest.getBlockInventory().setContents(setOne);
      ((Chest) block2.getState()).getBlockInventory().setContents(setTwo);

    } else chest.getBlockInventory().setContents(items);

    String deathMessage = "World: %s | x: %d | y: %d | z: %d";
    player.sendMessage(String.format(deathMessage, getWorldType(block.getWorld().getEnvironment()),
        block.getX(), block.getY(), block.getZ()));

    event.setKeepLevel(true);
    event.setKeepInventory(false);
    event.setDroppedExp(0);
    event.getDrops().clear();
  }

  private String getWorldType(World.Environment environment) {
    switch (environment) {
      case NORMAL: return "Overworld";
      case NETHER: return "Nether";
      case THE_END: return "The End";
      default: return "Overworld";
    }
  }

}
