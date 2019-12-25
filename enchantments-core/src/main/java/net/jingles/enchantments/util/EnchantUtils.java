package net.jingles.enchantments.util;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.persistence.DataType;
import net.jingles.enchantments.persistence.EnchantTeam;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class EnchantUtils {

  public static Set<CustomEnchant> getCustomEnchants(ItemStack item) {
    return item.getEnchantments().keySet().stream()
        .filter(e -> e instanceof CustomEnchant)
        .map(e -> (CustomEnchant) e)
        .collect(Collectors.toSet());
  }

  public static EnchantTeam getEnchantTeam(PersistentDataHolder holder) {
    return holder.getPersistentDataContainer().getOrDefault(Enchantments.TEAM_KEY,
        DataType.ENCHANT_TEAM, new EnchantTeam(new HashSet<>(), true));
  }

  public static UUID getOwner(PersistentDataHolder holder) {
    return holder.getPersistentDataContainer().get(Enchantments.OWNER_KEY, DataType.UUID);
  }

}
