package net.jingles.enchantments.util;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.Inventory;

import java.util.stream.Stream;

public class InventoryUtils {

  public static void removeItem(Inventory inventory, Material material, int amount) {
    Stream.of(inventory.getContents())
        .filter(item -> item.getType() == material)
        .findFirst().ifPresent(item -> {
          int newAmount = item.getAmount() - amount;
          if (newAmount < 1) inventory.remove(item);
          else item.setAmount(newAmount);
    });
  }


  public static void removeItem(Inventory inventory, Tag<Material> tag, int amount) {
    Stream.of(inventory.getContents())
        .filter(item -> tag.isTagged(item.getType()))
        .findFirst().ifPresent(item -> {
          int newAmount = item.getAmount() - amount;
          if (newAmount < 1) inventory.remove(item);
          else item.setAmount(newAmount);
    });

  }

}
