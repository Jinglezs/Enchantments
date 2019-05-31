package net.jingles.enchantments.enchants;

import net.jingles.enchantments.Enchant;
import net.jingles.enchantments.Enchantments;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CustomEnchant extends Enchantment implements Listener {

  public CustomEnchant(NamespacedKey key) {
    super(key);
  }

  public String getKeyName() {
    return this.getClass().getAnnotation(Enchant.class).key();
  }

  public String getDescription() {
    return this.getClass().getAnnotation(Enchant.class).description();
  }

  public int getLevelRequirement() {
    return this.getClass().getAnnotation(Enchant.class).levelRequirement();
  }

  @Override
  public String getName() {
    return this.getClass().getAnnotation(Enchant.class).name();
  }

  @Override
  public int getMaxLevel() {
    return this.getClass().getAnnotation(Enchant.class).maxLevel();
  }

  @Override
  public int getStartLevel() {
    return this.getClass().getAnnotation(Enchant.class).startingLevel();
  }

  @Override
  public EnchantmentTarget getItemTarget() {
    return this.getClass().getAnnotation(Enchant.class).targetItem();
  }

  @Override
  public boolean isTreasure() {
    return this.getClass().getAnnotation(Enchant.class).treasure();
  }

  @Override
  public boolean isCursed() {
    return this.getClass().getAnnotation(Enchant.class).cursed();
  }

  @Override
  public boolean canEnchantItem(ItemStack itemStack) {
    return getItemTarget().includes(itemStack);
  }

  public boolean hasEnchantment(ItemStack item) {
    return item.getItemMeta() != null && item.getItemMeta().hasEnchant(this);
  }

  public abstract boolean conflictsWith(Enchantment other);

  public abstract boolean canTrigger(Inventory inventory, Event event);

  public ItemStack getItem(Inventory inventory) {

    if (inventory instanceof HorseInventory) return ((HorseInventory) inventory).getArmor();
    PlayerInventory player = (PlayerInventory) inventory;

    switch (getItemTarget()) {
      case ALL:
        return Stream.of(player.getContents())
                .filter(this::hasEnchantment)
                .findFirst().orElse(null);

      case ARMOR:
        return Stream.of(player.getArmorContents())
                .filter(this::hasEnchantment)
                .findFirst().orElse(null);

      case ARMOR_HEAD: return player.getHelmet();
      case ARMOR_TORSO: return player.getChestplate();
      case ARMOR_LEGS: return player.getLeggings();
      case ARMOR_FEET: return player.getBoots();

      case TOOL:
      case BOW:
      case WEAPON:
      case TRIDENT:
      case CROSSBOW:
      case FISHING_ROD:
        return player.getItemInMainHand().getType() == Material.AIR ?
                player.getItemInOffHand() : player.getItemInMainHand();

      case WEARABLE:
        return (player.getChestplate() != null && player.getChestplate().getType() == Material.ELYTRA) ?
                player.getChestplate() : player.getHelmet();

      case BREAKABLE: //return item.getMaxDurability() > 0 && item.getMaxStackSize() == 1
        return Stream.of(player.getContents())
                .filter(item -> item.getMaxStackSize() == 1)
                .filter(item -> {
                  ItemMeta meta = item.getItemMeta();
                  return meta instanceof Damageable && ((Damageable) meta).hasDamage();
                })
                .filter(this::hasEnchantment)
                .findFirst().orElse(null);

      default: return null;
    }
  }

  public static Set<Enchantment> getEnchantmentsByItemType(EnchantmentTarget target) {
    return Enchantments.REGISTERED.stream()
            .filter(enchantment -> enchantment.getItemTarget() == target)
            .collect(Collectors.toSet());
  }

  public static Set<Enchantment> getApplicableEnchants(ItemStack item) {
    return Enchantments.REGISTERED.stream()
            .filter(enchantment -> enchantment.canEnchantItem(item))
            .collect(Collectors.toSet());
  }

}
