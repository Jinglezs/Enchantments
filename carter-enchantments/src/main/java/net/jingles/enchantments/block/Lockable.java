package net.jingles.enchantments.block;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Container;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.statuseffect.LocationStatusEffect;
import net.jingles.enchantments.statuseffect.StatusEffectManager;
import net.jingles.enchantments.statuseffect.entity.EntityStatusEffect;

public class Lockable extends BlockEnchant {

  public Lockable(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean canTrigger(TileState tile) {
    return hasEnchant(tile);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public void onChunkLoad(TileState tile) {
    // Enable lockable containers to remember who has entered the password.
    Enchantments.getStatusEffectManager().add(new LockableRememberEffect(tile.getLocation()));
  }

  @EventHandler
  public void onChestOpen(PlayerInteractEvent event) {

    // Ensure a container block was right clicked
    if (event.getClickedBlock() == null || !(event.getClickedBlock().getState() instanceof Container)
      || event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;

    Container container = (Container) event.getClickedBlock().getState();
    boolean isBlockPlacement = event.isBlockInHand() && event.getPlayer().isSneaking();

    // Do not want to interfere with block placement or non-enchanted containers.
    if (isBlockPlacement || !canTrigger(container)) return;

    Player player = event.getPlayer();
    StatusEffectManager manager = Enchantments.getStatusEffectManager();

    // Request a new password
    if (!container.isLocked()) {
      event.setCancelled(true);
      manager.add(new LockableListenEffect(player, Action.NEW_PASSWORD, container));
      return;
    }

    LockableRememberEffect remember = manager.getWorldContainer()
      .getEffect(container.getLocation(), LockableRememberEffect.class).orElse(null);

    // Do nothing if the player has already entered the password
    if (remember != null && remember.rememberedPlayers.contains(player.getUniqueId())) return;  

    event.setCancelled(true);

    Action action = player.isSneaking() ? Action.CHANGE_PASSWORD : Action.ENTER_PASSWORD;
    manager.add(new LockableListenEffect(player, action, container));

  }

  @EventHandler
  public void onPasswordEnter(AsyncPlayerChatEvent event) {
    Enchantments.getStatusEffectManager().getEntityContainer(event.getPlayer().getUniqueId())
      .flatMap(container -> container.getEffectBySource(this, LockableListenEffect.class))
      .ifPresent(effect -> effect.acceptInput(event.getMessage()));
  }

  private class LockableListenEffect extends EntityStatusEffect {

    private final Container container;
    private final Action action;
    private String input;

    public LockableListenEffect(Player target, Action action, Container container) {
      super(target, Lockable.this, 20 * 20, 1);
      this.action = action;
      this.container = container;
    }
    
    @Override
    public void start() {
      ((Player) getTarget()).sendMessage(action.message);
    }

    @Override
    public void effect() {

      if (input == null) return;

      Player player = (Player) getTarget();
     
      if (action == Action.NEW_PASSWORD) {

        container.setLock(input);
        container.update();

        player.sendMessage(ChatColor.GREEN + "Your new password is \"" + input + "\"");
        player.openInventory(container.getInventory());

      } else if (action == Action.ENTER_PASSWORD) {

        if (input.equals(container.getLock())) {

          player.sendMessage(ChatColor.GREEN + "Password accepted. This chest will remember " +
            "you until the next restart");
          player.openInventory(container.getInventory());  

        } else player.sendMessage(ChatColor.RED + "Incorrect password");

      } else if (action == Action.CHANGE_PASSWORD) {

        String[] split = input.split(" ");
        String original = split[0], changed = split[1];

        if (container.getLock().equals(original)) {

          container.setLock(changed);
          container.update();

          player.sendMessage(ChatColor.GREEN + "Your new password is \"" + changed + "\"");
          player.openInventory(container.getInventory());
          
        } else player.sendMessage(ChatColor.RED + "Incorrect password");

      }

      this.cancel();

    }

    public void acceptInput(String input) {
      this.input = input;
    }

  }

  private enum Action {

    NEW_PASSWORD(ChatColor.RED + "This Lockable container is currently unprotected. Please enter a new password in chat."),
    ENTER_PASSWORD(ChatColor.RED + "This container is locked. Please enter the exact password in chat."),
    CHANGE_PASSWORD(ChatColor.GREEN + "Enter the current password followed by the new password separated by a space.");

    private String message;
    Action(String message) {
      this.message = message;
    }

  }

  private class LockableRememberEffect extends LocationStatusEffect {

    private HashSet<UUID> rememberedPlayers = new HashSet<>();

    public LockableRememberEffect(Location location) {
      super(Lockable.this, Integer.MAX_VALUE, Integer.MAX_VALUE, location);
    }

    @Override
    public void effect() {
    }

    @Override
    public void stop() {
      rememberedPlayers.clear();
    }

  }

}