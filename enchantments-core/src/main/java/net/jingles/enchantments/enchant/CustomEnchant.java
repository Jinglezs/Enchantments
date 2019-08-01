package net.jingles.enchantments.enchant;

import net.jingles.enchantments.Enchantments;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CustomEnchant extends Enchantment implements Listener {

  private final String name;
  private final String keyName;
  private final String description;
  private final long cooldown;
  private final TimeUnit timeUnit;
  private final TargetGroup targetGroup;
  private final int levelRequirement;
  private final double enchantChance;
  private final int maxLevel;
  private final int startLevel;
  private final EnchantmentTarget target;
  private final boolean treasure;
  private final boolean cursed;
  private final NamespacedKey cooldownKey;

  public CustomEnchant(NamespacedKey key) {
    super(key);

    Enchant annotation = this.getClass().getAnnotation(Enchant.class);
    this.name = annotation.name();
    this.keyName = annotation.key();
    this.description = annotation.description();
    this.cooldown = annotation.cooldown();
    this.timeUnit = annotation.timeUnit();
    this.targetGroup = annotation.targetGroup();
    this.levelRequirement = annotation.levelRequirement();
    this.enchantChance = annotation.enchantChance();
    this.maxLevel = annotation.maxLevel();
    this.startLevel = annotation.startingLevel();
    this.target = annotation.targetItem();
    this.treasure = annotation.treasure();
    this.cursed = annotation.cursed();
    this.cooldownKey = getCooldownKey();
  }

  public String getKeyName() {
    return this.keyName;
  }

  public String getDescription() {
    return this.description;
  }

  public int getLevelRequirement() {
    return this.levelRequirement;
  }

  public double getEnchantChance() {
    return this.enchantChance;
  }

  public long getCooldown() {
    return this.cooldown;
  }

  public TimeUnit getTimeUnit() {
    return this.timeUnit;
  }

  @NotNull
  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public int getMaxLevel() {
    return this.maxLevel;
  }

  @Override
  public int getStartLevel() {
    return this.startLevel;
  }

  @NotNull
  @Override
  public EnchantmentTarget getItemTarget() {
    return this.target;
  }

  @Override
  public boolean isTreasure() {
    return this.treasure;
  }

  @Override
  public boolean isCursed() {
    return this.cursed;
  }

  @Override
  public boolean canEnchantItem(@NotNull ItemStack itemStack) {
    return itemStack.getType() == Material.BOOK || getItemTarget().includes(itemStack) &&
        getTargetGroup().canEnchant(itemStack.getType());
  }

  public boolean hasEnchantment(ItemStack item) {
    return item != null && item.getItemMeta() != null && item.getItemMeta().hasEnchant(this);
  }

  public TargetGroup getTargetGroup() {
    return this.targetGroup;
  }

  public abstract boolean conflictsWith(Enchantment other);

  public abstract boolean canTrigger(LivingEntity entity);

  public ItemStack getItem(LivingEntity entity) {

    EntityEquipment equipment = entity.getEquipment();
    if (equipment == null) return null;

    //Why the fuck does a llama have my enchantment?
    switch (getItemTarget()) {
      case ALL: //Prioritizes hands, but also checks rest of contents

        ItemStack mainHand = equipment.getItemInMainHand();
        if (hasEnchantment(mainHand)) return mainHand;

        ItemStack offHand = equipment.getItemInOffHand();
        if (hasEnchantment(offHand)) return offHand;

        if (entity instanceof InventoryHolder) {

          ItemStack found = Stream.of(((InventoryHolder) entity).getInventory().getContents())
              .filter(Objects::nonNull)
              .filter(item -> getTargetGroup().canEnchant(item.getType()))
              .filter(this::hasEnchantment)
              .findFirst().orElse(null);

          // Unless it's wearable, we want to make sure that the player is holding the enchanted item.
          if (found != null && !EnchantmentTarget.WEARABLE.includes(found) &&
              (mainHand.equals(found) || offHand.equals(found))) return found;

        }

        return null;

      case ARMOR:
        return Stream.of(equipment.getArmorContents())
            .filter(this::hasEnchantment)
            .findFirst().orElse(null);

      case ARMOR_HEAD:
        return equipment.getHelmet();
      case ARMOR_TORSO:
        return equipment.getChestplate();
      case ARMOR_LEGS:
        return equipment.getLeggings();
      case ARMOR_FEET:
        return equipment.getBoots();

      case TOOL:
      case BOW:
      case WEAPON:
      case TRIDENT:
      case CROSSBOW:
      case FISHING_ROD:
        return equipment.getItemInMainHand().getType() == Material.AIR ?
            equipment.getItemInOffHand() : equipment.getItemInMainHand();

      case WEARABLE:
        return (equipment.getChestplate() != null && equipment.getChestplate().getType() == Material.ELYTRA) ?
            equipment.getChestplate() : equipment.getHelmet();

      case BREAKABLE: //return item.getMaxDurability() > 0 && item.getMaxStackSize() == 1

        if (entity instanceof InventoryHolder) {

          return Stream.of(((InventoryHolder) entity).getInventory().getContents())
              .filter(item -> item.getMaxStackSize() == 1)
              .filter(item -> {
                ItemMeta meta = item.getItemMeta();
                return meta instanceof Damageable && ((Damageable) meta).hasDamage();
              })
              .filter(this::hasEnchantment)
              .findFirst().orElse(null);

        } else return null;


      default:
        return null;
    }
  }

  public int getLevel(ItemStack item) {
    return item.getItemMeta() == null ? 0 : item.getItemMeta().getEnchantLevel(this);
  }

  public NamespacedKey getCooldownKey() {
    if (cooldownKey != null) return cooldownKey;
    else return (!(this instanceof BlockEnchant)) ? getKey() :
        new NamespacedKey(Enchantments.getProvidingPlugin(Enchantments.class), getKeyName() + "cooldown");
  }

  public static Set<Enchantment> getEnchantmentsByItemType(EnchantmentTarget target) {
    return Enchantments.getEnchantmentManager().getRegisteredEnchants().stream()
        .filter(enchantment -> enchantment.getItemTarget() == target)
        .collect(Collectors.toSet());
  }

  public static Set<CustomEnchant> getApplicableEnchants(ItemStack item) {
    return Enchantments.getEnchantmentManager().getRegisteredEnchants().stream()
        .filter(enchantment -> enchantment.canEnchantItem(item))
        .collect(Collectors.toSet());
  }

  public void addCooldown(LivingEntity entity) {
    Enchantments.getCooldownManager().addCooldown(entity, this, getCooldown(), getTimeUnit());
  }

  public static boolean isCustomEnchant(Enchantment enchantment) {
    return Enchantments.getEnchantmentManager().getRegisteredEnchants().stream()
        .anyMatch(enchant -> enchant.getKey().equals(enchantment.getKey()));
  }

}
