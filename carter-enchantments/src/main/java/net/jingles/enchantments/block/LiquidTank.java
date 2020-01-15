package net.jingles.enchantments.block;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.persistence.DataType;
import net.jingles.enchantments.statuseffect.LocationStatusEffect;
import net.jingles.enchantments.statuseffect.PersistentEffect;
import net.jingles.enchantments.statuseffect.context.TileEntityContext;
import net.jingles.enchantments.util.InventoryUtils;
import org.bukkit.*;
import org.bukkit.block.DaylightDetector;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

@Enchant(name = "Liquid Tank", key = "liquid_tank", hasPersistence = true, targetGroup = TargetGroup.DAYLIGHT_DETECTOR,
    description = "Place a cauldron underneath this enchanted detector to create a liquid tank. Right clicking this " +
        "tank with a liquid item allows its contents to be emptied into the tank. They can be retrieved by right clicking " +
        "again with the correct item. The liquid that the tank holds is determined by the first liquid to be placed in " +
        "it, and the tank can hold 30 units per level.")
public class LiquidTank extends BlockEnchant {

  public LiquidTank(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean canTrigger(@Nullable TileState tile) {

    if (tile == null) return false;

    Material blockBelow = tile.getLocation()
        .subtract(0, 1, 0).getBlock().getType();

    return hasEnchant(tile) && blockBelow == Material.CAULDRON;
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  // Copies the persistent effect information from the item to the block
  @Override
  public void onBlockPlace(ItemMeta meta, TileState state) {

    PersistentDataContainer itemContainer = meta.getPersistentDataContainer();

    // Create the effect
    LiquidTankEffect effect = new LiquidTankEffect(new TileEntityContext(state, this));
    effect.deserialize(itemContainer);
    Enchantments.getStatusEffectManager().add(effect);

  }

  @Override
  public void onBlockBreak(ItemMeta meta, TileState state) {

    Enchantments.getStatusEffectManager()
        .getWorldContainer()
        .getEffect(state.getLocation(), LiquidTankEffect.class)
        .ifPresent(effect -> effect.serialize(meta.getPersistentDataContainer()));

  }

  @Override
  public void onChunkLoad(TileState tile) {

    if (Enchantments.getStatusEffectManager().getWorldContainer()
        .hasEffectAtLocation(tile.getLocation(), LiquidTankEffect.class)) return;

    LiquidTankEffect effect = new LiquidTankEffect(new TileEntityContext(tile, this));
    effect.deserialize(tile.getPersistentDataContainer());
    Enchantments.getStatusEffectManager().add(effect);

  }

  @EventHandler
  public void onLiquidTankInteract(PlayerInteractEvent event) {

    if (event.getAction() != Action.RIGHT_CLICK_BLOCK ||
        event.getHand() != EquipmentSlot.HAND ||
        event.getClickedBlock() == null ||
        !(event.getClickedBlock().getState() instanceof DaylightDetector)) return;

    DaylightDetector detector = (DaylightDetector) event.getClickedBlock().getState();
    if (!canTrigger(detector)) return;

    Optional<LiquidTankEffect> optionalEffect = Enchantments.getStatusEffectManager()
        .getWorldContainer()
        .getEffect(detector.getLocation(), LiquidTankEffect.class);

    if (!optionalEffect.isPresent()) return;

    event.setCancelled(true);

    LiquidTankEffect effect = optionalEffect.get();
    effect.handleExchange(event.getPlayer());

  }

  private enum Exchange {

    EMPTY("empty", ChatColor.WHITE + "Empty Liquid", null, null),

    EXPERIENCE("experience", ChatColor.GREEN + "Experience", Material.AIR, Material.AIR),

    WATER("water", ChatColor.DARK_AQUA + "Water", Material.WATER_BUCKET, Material.BUCKET),

    LAVA("lava", ChatColor.DARK_RED + "Lava", Material.LAVA_BUCKET, Material.BUCKET),

    MILK("milk", ChatColor.WHITE + "Milk", Material.MILK_BUCKET, Material.BUCKET),

    DRAGON_BREATH("dragon_breath", ChatColor.LIGHT_PURPLE + "Dragon Breath", Material.DRAGON_BREATH, Material.GLASS_BOTTLE),

    HONEY("honey", ChatColor.GOLD + "Honey", Material.HONEY_BOTTLE, Material.GLASS_BOTTLE),
    
    MUSHROOM_STEW("mushroom_stew", ChatColor.RED + "Mushroom Stew", Material.MUSHROOM_STEW, Material.BOWL),

    BEETROOT_SOUP("beetroot_soup", ChatColor.RED + "Beetroot Soup", Material.BEETROOT_SOUP, Material.BOWL),

    POTION("potion", ChatColor.LIGHT_PURPLE + "Potion", Material.POTION, Material.BOWL);

    private String id, name;
    private Material filled, empty;

    Exchange(String id, String name, Material filled, Material empty) {
      this.id = id;
      this.name = name;
      this.filled = filled;
      this.empty = empty;
    }

    public String getId() {
      return id;
    }

    public String getName() {
      return this.name;
    }

    public Material getFilled() {
      return filled;
    }

    public Material getEmpty() {
      return empty;
    }

    public static Exchange getExchange(String id) {
      return Stream.of(Exchange.values())
          .filter(exchange -> exchange.getId().equals(id))
          .findAny()
          .orElse(null);
    }

    public static Exchange getExchange(Material material) {
      return Stream.of(Exchange.values())
          .filter(exchange -> exchange.getFilled() == material)
          .findAny()
          .orElse(Exchange.EXPERIENCE);
    }

  }

  /**
   * A persistent status effect that contains information related to what type of liquid
   * this tank holds and the amount that it currently holds.
   */
  private static class LiquidTankEffect extends LocationStatusEffect implements PersistentEffect {

    private static final String TITLE = ChatColor.BOLD + "%s Tank";
    private static final String CAPACITY = ChatColor.BOLD + "Capacity: " + ChatColor.RESET + "%d/%d";

    private final NamespacedKey typeKey, amountKey, potionKey;
    private final int maxAmount;

    private PotionData data = null;
    private Exchange exchange = Exchange.EMPTY;
    private int amount = 0;

    // The hologram armor stands
    private ArmorStand title, capacity;

    private LiquidTankEffect(TileEntityContext context) {
      super(context, Integer.MAX_VALUE, Integer.MAX_VALUE, context.getTrigger().getLocation());
      this.maxAmount = ((BlockEnchant) context.getSource()).getLevel(context.getTrigger()) * 30;
      this.typeKey = Enchantments.createKey("liquid_tank_type");
      this.amountKey = Enchantments.createKey("liquid_tank_amount");
      this.potionKey = Enchantments.createKey("potion_data");
    }

    @Override
    public void start() {

      Location loc = ((TileEntityContext) getContext()).getTrigger().getLocation().add(0.5, 0.25, 0.5);

      this.capacity = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
      capacity.setSmall(true);
      capacity.setMarker(true);
      capacity.setVisible(false);
      capacity.setCustomNameVisible(true);
      capacity.setCustomName(String.format(CAPACITY, amount, maxAmount));

      this.title = (ArmorStand) loc.getWorld().spawnEntity(loc.add(0, 0.25, 0), EntityType.ARMOR_STAND);
      title.setSmall(true);
      title.setMarker(true);
      title.setVisible(false);
      title.setCustomNameVisible(true);
      title.setCustomName(String.format(TITLE, exchange != null ? exchange.name : "Empty Liquid"));

    }

    @Override
    public void effect() {
    }

    @Override
    public void stop() {
      super.stop();
      title.remove();
      capacity.remove();
      getContext().serialize(this);
    }

    @Override
    public void serialize(PersistentDataContainer container) {
      container.set(typeKey, PersistentDataType.STRING, exchange.getId());
      container.set(amountKey, PersistentDataType.INTEGER, amount);
      container.set(potionKey, DataType.POTION_DATA, data);
    }

    @Override
    public void deserialize(PersistentDataContainer container) {
      String id = container.getOrDefault(typeKey, PersistentDataType.STRING, "empty");
      this.exchange = Exchange.getExchange(id);
      this.amount = container.getOrDefault(amountKey, PersistentDataType.INTEGER, 0);
      this.data = container.get(potionKey, DataType.POTION_DATA);
    }

    /**
     * Places the given amount of liquid in the tank. To remove liquid,
     * pass a negative number as the amount.
     *
     * @param change the amount to give or take
     */
    private void giveAmount(int change) {
      int newAmount = amount + change;
      this.amount = Math.min(maxAmount, Math.max(0, newAmount));
    }

    private boolean canAddOrRemove(int change) {
      int changed = amount + change;
      return changed >= 0 && changed <= maxAmount;
    }

    /**
     * Handles an exchange between the given player and the tank
     *
     * @param player the player
     */
    private void handleExchange(Player player) {

      int index = player.getInventory().getHeldItemSlot();
      ItemStack held = player.getInventory().getItem(index);
      Material type = held == null ? Material.AIR : held.getType();

      if (exchange == Exchange.EMPTY) {
        exchange = Exchange.getExchange(type);
        title.setCustomName(String.format(TITLE, exchange.getName()));
      }

      if (exchange == Exchange.EXPERIENCE) {

        int change = player.isSneaking() ? -1 : 1;

        if (!canAddOrRemove(change) || (change == 1 && player.getLevel() < 1)) return;

        // The amount of levels to give/take is opposite of the change to the tank's capacity.
        player.giveExpLevels(-1 * change);
        giveAmount(change);

        Sound sound = change == -1 ? Sound.ENTITY_EXPERIENCE_ORB_PICKUP : Sound.ITEM_BOTTLE_EMPTY;
        player.getWorld().playSound(player.getLocation(), sound, 1F, 1F);

      } else if (exchange == Exchange.POTION) {

        int change = type == exchange.empty ? -1 : type == exchange.filled ? 1 : 0;
        if (change == 0 || !canAddOrRemove(change)) return;

        if (change == 1) {

          PotionData heldData = ((PotionMeta) held.getItemMeta()).getBasePotionData();
          if (!data.equals(heldData)) return;

          held.setType(Material.GLASS_BOTTLE);

        } else {

          held.setType(Material.POTION);

          PotionMeta meta = (PotionMeta) held.getItemMeta();
          meta.setBasePotionData(data);
          held.setItemMeta(meta);

        }
        
        giveAmount(change);

        Sound sound = change == -1 ? Sound.ITEM_BUCKET_FILL : Sound.ITEM_BUCKET_EMPTY;
        player.getWorld().playSound(player.getLocation(), sound, 1F, 1F);
  
      } else {

        // If they are retrieving liquid with the empty item, the change is negative.
        // If they are emptying an item, the change is positive.
        // Zero if they are using an item that cannot be emptied/filled.
        int change = type == exchange.empty ? -1 : type == exchange.filled ? 1 : 0;
        if (change == 0 || !canAddOrRemove(change)) return;

        InventoryUtils.removeItem(player.getInventory(), type, 1);

        Material replacement = change == -1 ? exchange.filled : exchange.empty;
        player.getInventory().addItem(new ItemStack(replacement, 1));

        giveAmount(change);

        Sound sound = change == -1 ? Sound.ITEM_BUCKET_FILL : Sound.ITEM_BUCKET_EMPTY;
        player.getWorld().playSound(player.getLocation(), sound, 1F, 1F);

      }

      if (amount == 0) {
        exchange = Exchange.EMPTY;
        title.setCustomName(String.format(TITLE, exchange.getName()));
      }

      capacity.setCustomName(String.format(CAPACITY, amount, maxAmount));

    }

  }

}
