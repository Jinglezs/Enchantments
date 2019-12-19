package net.jingles.enchantments.block;

import java.util.HashSet;
import java.util.Optional;
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
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.statuseffect.LocationStatusEffect;
import net.jingles.enchantments.statuseffect.StatusEffectManager;
import net.jingles.enchantments.statuseffect.entity.EntityStatusEffect;
import net.jingles.enchantments.enchant.TargetGroup;

@Enchant(name = "Lockable", key = "lockable", maxLevel = 1, targetGroup = TargetGroup.CONTAINER,
  description = "When a container with Lockable is first opened, the owner will set a password. To access " +
    "the container, you must open it while holding an item whose name is equivalent to the key.")
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

    if (manager.getEntityContainer(player.getUniqueId())
      .map(con -> con.hasEffect(LockableListenEffect.class))
      .orElse(false)) return;

      event.setCancelled(true);

    // Set new password if container doesn't have a lock
    if (!container.isLocked()) {

      manager.add(new LockableListenEffect(player, Action.NEW_PASSWORD, container));
      return;

    // Update the container's lock  
    } else if (player.isSneaking()) {

      manager.add(new LockableListenEffect(player, Action.CHANGE_PASSWORD, container));

    // Allow Spigot to do the rest for us.  
    } else event.setCancelled(false);
    
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

      } else if (action == Action.CHANGE_PASSWORD) {

        String[] split = input.split(" ");
        String original = split[0], changed = split[1];

        if (container.getLock().equals(original)) {

          container.setLock(changed);
          container.update();

          player.sendMessage(ChatColor.GREEN + "Your new password is \"" + changed + "\"");
          rememberPlayer(player, container.getLocation());
          
        } else player.sendMessage(ChatColor.RED + "Incorrect password");

      }

      this.cancel();

    }

    public void acceptInput(String input) {
      this.input = input;
    }

    private void rememberPlayer(Player player, Location location) {
      Enchantments.getStatusEffectManager().getWorldContainer()
        .getEffect(location, LockableRememberEffect.class)
        .ifPresent(effect -> effect.rememberedPlayers.add(player.getUniqueId()));
    }

  }

  private enum Action {

    NEW_PASSWORD(ChatColor.RED + "This Lockable container is currently unprotected. Please enter a new password in chat."),
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