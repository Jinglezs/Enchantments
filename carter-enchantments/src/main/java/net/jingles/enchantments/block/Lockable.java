package net.jingles.enchantments.block;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Container;
import org.bukkit.block.TileState;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

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

    // The player has already been prompted for the password
    // Otherwise, do nothing if the container is already locked
    // and the player is not attempting to change the password.
    if (player.isConversing() || (container.isLocked() && !player.isSneaking())) return;

    event.setCancelled(true);

    HashMap<Object, Object> initialSessionData = new HashMap<>();
    initialSessionData.put("action", player.isSneaking() ? Action.CHANGE_PASSWORD : Action.NEW_PASSWORD);
    initialSessionData.put("container", container);

    // Construct and begin the conversation
    Conversation conversation = Enchantments.getConversationFactory()
        .withModality(false)
        .withLocalEcho(false)
        .withTimeout(30)
        .withFirstPrompt(new PasswordPrompt())
        .withInitialSessionData(initialSessionData)
        .withPrefix(context -> ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Lockable" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET)
        .buildConversation(player);

    player.beginConversation(conversation);
    
  }

  private enum Action {

    NEW_PASSWORD(ChatColor.RED + "This container is currently unprotected. Please enter a new password in chat."),
    CHANGE_PASSWORD(ChatColor.GREEN + "Enter the current password followed by the new password separated by a space.");

    private String message;
    Action(String message) {
      this.message = message;
    }

  }

  private static class PasswordPrompt implements Prompt {

    @NotNull
    @Override
    public String getPromptText(@NotNull ConversationContext context) {
      return ((Action) context.getSessionData("action")).message;
    }

    @Override
    public boolean blocksForInput(@NotNull ConversationContext context) {
      return true;
    }

    @Nullable
    @Override
    public Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {

      Action action = (Action) context.getSessionData("action");
      Container container = (Container) context.getSessionData("container");

      if (action == Action.NEW_PASSWORD) {

        container.setLock(input);
        container.update(true);
        context.getForWhom().sendRawMessage(ChatColor.GREEN + "Your new password is \"" + input + "\"");

      } else if (action == Action.CHANGE_PASSWORD) {

        String[] split = input.split(" ");
        String original = split[0], changed = split[1];

        if (container.getLock().equals(original)) {

          container.setLock(changed);
          container.update(true);
          context.getForWhom().sendRawMessage(ChatColor.GREEN + "Your new password is \"" + changed + "\"");

        } else context.getForWhom().sendRawMessage(ChatColor.RED + "Incorrect password");

      }

      return Prompt.END_OF_CONVERSATION;

    }

  }

}