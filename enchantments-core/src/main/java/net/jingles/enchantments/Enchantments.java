package net.jingles.enchantments;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.jingles.enchantments.cooldown.CooldownManager;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.EnchantmentManager;
import net.jingles.enchantments.projectile.ProjectileManager;
import net.jingles.enchantments.statuseffect.StatusEffectManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.stream.Collectors;

public class Enchantments extends JavaPlugin {

  private static CooldownManager cooldownManager;
  private static ProjectileManager projectileManager;
  private static EnchantmentManager enchantmentManager;
  private static StatusEffectManager statusEffectManager;

  @Override
  public void onEnable() {
    enableRegistrations();
    registerCommands();
    getServer().getPluginManager().registerEvents(new EnchantListener(), this);
  }

  //Ignore what Spigot has to say and forcefully enable enchantment registration
  //Has to be done using reflection because the fields are private.
  @SuppressWarnings("unchecked") //The type of the field is guaranteed, so it is safe to cast.
  private void enableRegistrations() {
    try {

      Field field = Enchantment.class.getDeclaredField("acceptingNew");
      field.setAccessible(true);
      field.set(null, true);

    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
      getLogger().warning("Could not enable Enchantments! The server is not accepting enchantment registrations.");
      setEnabled(false);
    }
  }

  private void registerCommands() {

    BukkitCommandManager manager = new BukkitCommandManager(this);
    manager.enableUnstableAPI("help");

    //----- COMMAND DEPENDENCY INJECTION -----

    enchantmentManager = new EnchantmentManager(this);
    cooldownManager = new CooldownManager(this);
    projectileManager = new ProjectileManager();
    statusEffectManager = new StatusEffectManager(this);

    manager.registerDependency(EnchantmentManager.class, enchantmentManager);
    manager.registerDependency(CooldownManager.class, cooldownManager);
    manager.registerDependency(ProjectileManager.class, projectileManager);
    manager.registerDependency(StatusEffectManager.class, statusEffectManager);

    //----- ARGUMENT COMPLETIONS -----

    //Command completion for CustomEnchant names
    manager.getCommandCompletions().registerAsyncCompletion("enchantments", handler ->
            enchantmentManager.getRegisteredEnchants().stream().map(CustomEnchant::getName)
                .collect(Collectors.toList()));

    //----- CONDITIONS -----

    //ACF automatically catches these exceptions and sends the player an error message.
    manager.getCommandConditions().addCondition(Player.class, "holdingItem", (context, exeContext, player) -> {
      if (player.getInventory().getItemInMainHand().getType() == Material.AIR)
        throw new ConditionFailedException("You must be holding an item to use this command!");
    });

    manager.getCommandConditions().addCondition(ItemStack.class, "hasCustomEnchants", (context, execContext, item) -> {
      if (item.getItemMeta() == null || item.getItemMeta().getEnchants().isEmpty())
        throw new ConditionFailedException("This item does not have any enchantments!");
      else if (item.getItemMeta().getEnchants().keySet().stream().noneMatch(enchant -> enchant instanceof CustomEnchant))
        throw new ConditionFailedException("This item does not have any custom enchantments.");
    });

    manager.getCommandConditions().addCondition("operator", handler -> {
      if (!handler.getIssuer().getIssuer().isOp()) throw new ConditionFailedException("Only operators can use this command!");
    });

    //----- PARAMETER CONTEXTS -----

    //Issuer Only context does not not consume any of the command arguments. Instead, it retrieves the
    //value from the CommandSender itself (in this case, the Player).
    manager.getCommandContexts().registerIssuerOnlyContext(ItemStack.class, context ->
            context.getPlayer().getInventory().getItemInMainHand());

    //Gets a CustomEnchant object from the remaining command arguments (String array)
    manager.getCommandContexts().registerContext(CustomEnchant.class, context -> {
      String key = String.join("_", context.getArgs());
      Optional<CustomEnchant> optional = enchantmentManager.getRegisteredEnchants().stream()
              .filter(customEnchant -> customEnchant.getKeyName().equals(key))
              .findAny();

      CustomEnchant enchant = optional.orElseGet(() -> enchantmentManager.getRegisteredEnchants().stream()
              .filter(customEnchant -> customEnchant.getName().equalsIgnoreCase(key.replace("_", " ")))
              .findAny().orElse(null));

      if (enchant == null) throw new InvalidCommandArgument("A custom enchantment with that name could not be found.");
      return enchant;
    });

    manager.registerCommand(new Commands());
  }

  public static CooldownManager getCooldownManager() {
    return cooldownManager;
  }

  public static ProjectileManager getProjectileManager() {
    return projectileManager;
  }

  public static EnchantmentManager getEnchantmentManager() {
    return enchantmentManager;
  }

  public static StatusEffectManager getStatusEffectManager() {
    return statusEffectManager;
  }

}
