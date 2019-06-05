package net.jingles.enchantments;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.jingles.enchantments.enchants.CustomEnchant;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Enchantments extends JavaPlugin {

  public static final Set<CustomEnchant> REGISTERED = new HashSet<>();
  public static MetadataValue METADATA;

  @Override
  public void onEnable() {

    enableRegistrations();

    //Register all custom enchantment classes, which are annotated with @Enchant
    Reflections reflections = new Reflections("net.jingles.enchantments.enchants");
    reflections.getTypesAnnotatedWith(Enchant.class).forEach(enchant -> {

      Enchant annotation = enchant.getAnnotation(Enchant.class);
      NamespacedKey key = new NamespacedKey(this, annotation.key());
      CustomEnchant enchantment;

      try {

        enchantment = (CustomEnchant) enchant.getConstructor(NamespacedKey.class).newInstance(key);
        getServer().getPluginManager().registerEvents(enchantment, this);
        REGISTERED.add(enchantment);

        Enchantment.registerEnchantment(enchantment);

      } catch (IllegalArgumentException | NoSuchMethodException | InstantiationException | InvocationTargetException |
              IllegalAccessException | IllegalStateException e) {
        //Only thrown when a duplicate enchantment is registered (plugin reload).
        if (!(e instanceof IllegalArgumentException)) e.printStackTrace();
      }

    });

    registerCommands();
    getServer().getPluginManager().registerEvents(new EnchantListener(), this);
    METADATA = new FixedMetadataValue(this, 0);
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

    //----- ARGUMENT COMPLETIONS -----

    //Command completion for CustomEnchant names
    manager.getCommandCompletions().registerAsyncCompletion("enchantments", handler ->
            REGISTERED.stream().map(CustomEnchant::getName).collect(Collectors.toList()));

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
      Optional<CustomEnchant> optional = Enchantments.REGISTERED.stream()
              .filter(customEnchant -> customEnchant.getKeyName().equals(key))
              .findAny();

      CustomEnchant enchant = optional.orElseGet(() -> Enchantments.REGISTERED.stream()
              .filter(customEnchant -> customEnchant.getName().equalsIgnoreCase(key.replace("_", " ")))
              .findAny().orElse(null));

      if (enchant == null) throw new InvalidCommandArgument("A custom enchantment with that name could not be found.");
      return enchant;
    });

    manager.registerCommand(new Commands());
  }

}
