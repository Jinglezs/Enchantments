package net.jingles.enchantments.block;

import com.google.common.primitives.Ints;
import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.statuseffect.LocationStatusEffect;
import net.jingles.enchantments.statuseffect.PersistentEffect;
import net.jingles.enchantments.statuseffect.context.TileEntityContext;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

@Enchant(name = "Liquid Tank", key = "liquid_tank", hasPersistence = true, targetGroup = TargetGroup.SIGN,
    description = "Right clicking this sign with a liquid item allows its contents to be emptied into the tank. Sneak-right " +
        "clicking with the correct item will retrieve the liquid. The liquid that the tank holds is determined by the first " +
        "liquid to be placed in it, and the tank can hold 30 units per level.")
public class LiquidTank extends BlockEnchant {

  private static final String FORMAT = ChatColor.BOLD + "Capacity: " + ChatColor.RESET + "%d/%d";

  public LiquidTank(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean canTrigger(@Nullable TileState tile) {
    return hasEnchant(tile);
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
        !(event.getClickedBlock().getState() instanceof Sign)) return;

    Sign sign = (Sign) event.getClickedBlock().getState();
    if (!canTrigger(sign)) return;

    Optional<LiquidTankEffect> optionalEffect = Enchantments.getStatusEffectManager()
        .getWorldContainer()
        .getEffect(sign.getLocation(), LiquidTankEffect.class);

    if (!optionalEffect.isPresent()) return;

    event.setCancelled(true);

    LiquidTankEffect effect = optionalEffect.get();
    effect.handleExchange(event.getPlayer(), sign);

  }

  private enum Exchange {

    EXPERIENCE("experience", ChatColor.GREEN + "Experience", Material.AIR, Material.AIR),

    WATER("water", ChatColor.DARK_AQUA + "Water", Material.WATER_BUCKET, Material.BUCKET),

    LAVA("lava", ChatColor.DARK_RED + "Lava", Material.LAVA_BUCKET, Material.BUCKET),

    MILK("milk", ChatColor.WHITE + "Milk", Material.MILK_BUCKET, Material.BUCKET),

    DRAGON_BREATH("dragon_breath", ChatColor.LIGHT_PURPLE + "Dragon Breath", Material.DRAGON_BREATH, Material.GLASS_BOTTLE),

    HONEY("honey", ChatColor.GOLD + "Honey", Material.HONEY_BOTTLE, Material.GLASS_BOTTLE);

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

    private final NamespacedKey effectKey = Enchantments.createKey("liquid_tank_effect");
    private final int maxAmount;

    private Exchange exchange = null;
    private int amount = 0;

    private LiquidTankEffect(TileEntityContext context) {
      super(context, Integer.MAX_VALUE, Integer.MAX_VALUE, context.getTrigger().getLocation());
      this.maxAmount = ((BlockEnchant) context.getSource()).getLevel(context.getTrigger()) * 30;
    }

    @Override
    public void start() {

      Sign sign = (Sign) ((TileEntityContext) getContext()).getTrigger();
      sign.setEditable(true);

      sign.setLine(0, ChatColor.BOLD + "-----");
      sign.setLine(3, ChatColor.BOLD + "-----");

      if (exchange == null) {

        sign.setLine(1, ChatColor.BOLD + "Empty Liquid");
        sign.setLine(2, ChatColor.BOLD + "Tank");

      } else {

        //TODO: Newly placed Liquid Tanks are always considered new.
        //  meaning this vvv is never reflected in game.

        sign.setLine(1, exchange.getName() + " Tank");
        sign.setLine(2, String.format(FORMAT, amount, maxAmount));

      }

      sign.update();

    }

    @Override
    public void effect() {
    }

    @Override
    public void serialize(PersistentDataContainer container) {
      if (exchange == null) return;
      container.set(effectKey, PersistentDataType.STRING, exchange.getId());
      container.set(effectKey, PersistentDataType.INTEGER, amount);
    }

    @Override
    public void deserialize(PersistentDataContainer container) {
      if (!container.has(effectKey, PersistentDataType.STRING)) return;
      this.exchange = Exchange.getExchange(container.get(effectKey, PersistentDataType.STRING));
      this.amount = container.getOrDefault(effectKey, PersistentDataType.INTEGER, 0);
    }

    /**
     * Gets the Exchange type that this tank is set to use.
     *
     * @return the Exchange type
     */
    public Exchange getExchange() {
      return this.exchange;
    }

    /**
     * Gets the maximum amount of liquid this tank can hold.
     *
     * @return the max amount
     */
    public int getMaxAmount() {
      return this.maxAmount;
    }

    public int getAmount() {
      return this.amount;
    }

    /**
     * Places the given amount of liquid in the tank. To remove liquid,
     * pass a negative number as the amount.
     *
     * @param amount the amount to give or take
     */
    public void giveAmount(int amount) {

      int newAmount = this.amount + amount;
      this.amount = Ints.constrainToRange(newAmount, 0, maxAmount);
      // Constrains the range so 0 <= amount <= maxAmount

    }

    /**
     * Handles an exchange between the given player and the tank
     * @param player the player
     */
    public void handleExchange(Player player, Sign sign) {

      Material held = player.getInventory().getItemInMainHand().getType();

      if (exchange == null) {
        exchange = Exchange.getExchange(held);
      }

      // If the exchange is Experience, remove if sneaking, add otherwise.
      // In any other case, remove if the item it's the empty version, add
      // if it's the full version, and do not change if it's neither.
      int change = exchange == Exchange.EXPERIENCE ? (player.isSneaking() ? -1 : 1) :
          (held == exchange.empty ? -1 : held == exchange.filled ? 1 : 0);

      // Ensure the amount stays within the tank's capacity
      if (amount + change < 0 || amount + change > maxAmount) return;

      if (exchange != Exchange.EXPERIENCE) {

        if (change == 0) return;

        Material replacement = change == -1 ? exchange.empty : exchange.filled;
        player.getInventory().setItemInMainHand(new ItemStack(replacement, 1));

      } else {

        if (change < 0 && player.getExpToLevel() < 1) return;
        player.giveExpLevels(-1 * change);

      }

      giveAmount(amount);

      //TODO: Sign is not updating the capacity

      sign.setLine(1, exchange.getName() + " Tank");
      sign.setLine(2, String.format(FORMAT, amount, maxAmount));
      sign.update();

    }

  }

}
