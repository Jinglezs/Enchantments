package net.jingles.enchantments;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.*;
import net.jingles.enchantments.cooldown.CooldownManager;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.persistence.DataType;
import net.jingles.enchantments.persistence.EnchantTeam;
import net.jingles.enchantments.statuseffect.StatusEffectManager;
import net.jingles.enchantments.statuseffect.container.EntityEffectContainer;
import net.jingles.enchantments.statuseffect.container.WorldEffectContainer;
import net.jingles.enchantments.util.EnchantUtils;
import net.jingles.enchantments.util.RomanNumerals;
import org.bukkit.ChatColor;
import org.bukkit.block.TileState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@CommandAlias("enchantments")
public class Commands extends BaseCommand {

  // --- Frequently used formatting ---

  private static final String TITLE = ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "Enchantments" +
      ChatColor.DARK_GRAY + "] " + ChatColor.RESET;

  private static final String LABELLED = ChatColor.GREEN + "%s" + ChatColor.DARK_GRAY + ": " + ChatColor.RESET + "%s\n";

  private static final String HEADER = ChatColor.DARK_GRAY + "+-------+ " + ChatColor.AQUA +
      "%s " + ChatColor.DARK_GRAY + "+-------+\n" + ChatColor.RESET;

  //Replacements: Name, cooldown, time unit
  private static final String INFO = ChatColor.GOLD + " - " + ChatColor.AQUA + "%s " + ChatColor.GOLD + ": "
      + ChatColor.RED + "%s %s";

  // -----------------------------------

  @Dependency
  private CooldownManager cooldownManager;

  @Dependency
  private StatusEffectManager statusEffectManager;

  @HelpCommand
  public void onEnchantHelpCommand(CommandHelp help) {
    //Sends the CommandSender the automagykally generated help message.
    help.showHelp();
  }

  @CommandAlias("enchant")
  @Conditions("operator")
  @CommandCompletion("@range:1-20 @enchantments")
  @Syntax("<level> <enchantment name>")
  @Description("Applies the custom enchantment with the given level to the item held in the executor's main (right) hand.")
  public void onAddEnchant(@Conditions("holdingItem") Player player, ItemStack item, int level, CustomEnchant enchant) {

    ItemMeta meta = item.getItemMeta();
    //Do nothing if the item already has the enchant with the same level.
    if (meta.hasEnchant(enchant) && meta.getEnchantLevel(enchant) == level) return;

    //Add the enchantment name and level to the item's lore
    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
    String enchantment = enchant.getName() + " " + RomanNumerals.toRoman(level);

    lore.add(enchantment);
    meta.setLore(lore);
    meta.addEnchant(enchant, level, true);
    item.setItemMeta(meta);

    player.sendMessage(TITLE + "Successfully added " + enchantment + " to the item.");
  }

  @CommandAlias("disenchant")
  @Conditions("operator")
  @CommandCompletion("@enchantments")
  @Syntax("<enchantment name>")
  @Description("Removes the given custom enchantment from the item held in the executor's main (right) hand.")
  public void onDisenchant(@Conditions("holdingItem") Player player, @Conditions("hasCustomEnchants") ItemStack item, CustomEnchant enchant) {

    ItemMeta meta = item.getItemMeta();
    meta.removeEnchant(enchant);

    if (meta.hasLore()) {
      List<String> lore = meta.getLore();
      lore.removeIf(line -> line.contains(enchant.getName()));
      meta.setLore(lore);
    }

    item.setItemMeta(meta);
    player.sendMessage(TITLE + "Successfully disenchanted the item.");
  }

  @Subcommand("list")
  @CommandCompletion("@targetGroups")
  @Description("Shows the executor a complete list of registered custom enchantment names.")
  public void onEnchantmentList(CommandSender sender, String type) {

    type = type.trim().toLowerCase();
    String enchants;

    if (type.equals("all")) {

      enchants = Enchantments.getEnchantmentManager().getRegisteredEnchants().stream()
        .map(CustomEnchant::getName)
        .collect(Collectors.joining(ChatColor.WHITE + ", " + ChatColor.GOLD));

    } else if (type.equals("hand")) {

      ItemStack item = sender instanceof Player ? ((Player) sender).getInventory().getItemInMainHand() : null;

      if (item == null) throw new InvalidCommandArgument("You must be holding an item in your main hand.");
      
      enchants = Enchantments.getEnchantmentManager().getRegisteredEnchants().stream() 
        .filter(enchant -> enchant.canEnchantItem(item))
        .map(CustomEnchant::getName)
        .collect(Collectors.joining(ChatColor.WHITE + ", " + ChatColor.GOLD));

    } else {

      TargetGroup target;

      try {
        target = TargetGroup.valueOf(type.toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new InvalidCommandArgument("That is not a valid target group.");
      }

      enchants = Enchantments.getEnchantmentManager().getRegisteredBlockEnchants().stream()
        .filter(enchant -> enchant.getTargetGroup() == target)
        .map(CustomEnchant::getName)
        .collect(Collectors.joining(ChatColor.WHITE + ", " + ChatColor.GOLD));

    }

    sender.sendMessage(TITLE + "Applicable custom enchantments: " + ChatColor.GOLD + enchants);
  }

  @Subcommand("info")
  @Syntax("<enchantment name>")
  @CommandCompletion("@enchantments")
  @Description("Shows the executor information about the provided enchantment")
  public void onEnchantmentInfo(CommandSender sender, CustomEnchant enchant) {

    StringBuilder info = new StringBuilder();

    // General info
    info.append(String.format(LABELLED, "\nEnchant Chance", String.valueOf(enchant.getEnchantChance())))
    .append(String.format(LABELLED, "Max Level", enchant.getMaxLevel()))
    .append(String.format(LABELLED, "Level Requirement", enchant.getLevelRequirement()))
    // Cooldown info
    .append(String.format(LABELLED, "\nCooldown", String.valueOf(enchant.getCooldown())))
    .append(String.format(LABELLED, "Time Unit", enchant.getTimeUnit().name()))
    // Target info
    .append(String.format(LABELLED, "Target Item", enchant.getItemTarget().name()))
    .append(String.format(LABELLED, "Target Group", enchant.getTargetGroup().name()))
    // Description
    .append(String.format(LABELLED, "\nDescription", enchant.getDescription()))
    .append("\n");

    sender.sendMessage(String.format(HEADER, enchant.getName()) + info.toString());
  }

  @CommandAlias("cooldowns")
  public void onCooldownInfo(Player player) {

    String cooldownMessage = cooldownManager.getCooldowns(player).entrySet().stream()
        .map(entry -> {
          long remaining = TimeUnit.MILLISECONDS.toSeconds(entry.getValue() - System.currentTimeMillis());
          return String.format(INFO, entry.getKey().getName(), remaining, TimeUnit.SECONDS.name().toLowerCase());
        })
        .collect(Collectors.joining("\n"));

    player.sendMessage(String.format(HEADER, "Enchantment Cooldowns") +
        (!cooldownMessage.isEmpty() ? cooldownMessage : " - You do not have any active enchantment cooldowns"));
  }

  private static final String EFFECTS_HEADER = ChatColor.DARK_GRAY + "+-------+ " + ChatColor.AQUA +
      "Current Status Effects " + ChatColor.DARK_GRAY + "+-------+\n" + ChatColor.RESET;

  @CommandAlias("statusEffects")
  @Description("Shows the executor all status effects being applied to them")
  public void onEffectInfo(Player sender) {

    java.util.Optional<EntityEffectContainer> container = statusEffectManager.getEntityContainer(sender.getUniqueId());

    String effectsMessage = (!container.isPresent() || container.get().getStatusEffects().isEmpty()) ? " - You do not have any active status effects" :
        container.get().getStatusEffects().stream()
            .map(effect -> String.format(INFO, effect.getClass().getSimpleName(), effect.getTicksLeft() / 20, "seconds"))
            .collect(Collectors.joining("\n"));

    sender.sendMessage(EFFECTS_HEADER + effectsMessage);
  }

  @CommandAlias("locationEffects")
  @Syntax("<radius>")
  @Description("Displays information about location status effects within the given radius")
  public void onLocationEffectInfo(Player player, double radius) {

    WorldEffectContainer container = statusEffectManager.getWorldContainer();

    String effectsMessage = container.getEffectsWithinRadius(player.getLocation(), radius, radius, radius).stream()
        .map(effect -> String.format(INFO, effect.getClass().getSimpleName(), effect.getTicksLeft() / 20, "seconds"))
        .collect(Collectors.joining("\n"));

    player.sendMessage(EFFECTS_HEADER + (!effectsMessage.isEmpty() ? effectsMessage : " - There are no active location status effects"));
  }

  @CommandAlias("edit-team")
  @Syntax("<add | remove | toggle-tameables> <name of the entity/flag>")
  @Description("Allows the sender to add entities to their enchantment team or toggle team flags.")
  public void onEditTeamCommand(Player player, String action, PersistentDataHolder holder, @Optional String name) {

    UUID owner = holder.getPersistentDataContainer()
        .getOrDefault(Enchantments.OWNER_KEY, DataType.UUID, player.getUniqueId());

    if (!player.getUniqueId().equals(owner))
      throw new InvalidCommandArgument("You must own this enchantment to edit it's team.");

    EnchantTeam team = EnchantUtils.getEnchantTeam(holder);

    if (action.equalsIgnoreCase("toggle-tameables")) {

      team.setIncludeTamedAnimals(!team.getIncludeTamedAnimals());

    } else {

      if (name == null) throw new InvalidCommandArgument("You must provide a named entity to add or remove");

      java.util.Optional<LivingEntity> entity = player.getWorld().getLivingEntities().stream()
          .filter(e -> e.getCustomName() != null && e.getCustomName().equalsIgnoreCase(name))
          .findAny();

      if (entity.isPresent()) {

        if (action.equalsIgnoreCase("add")) team.addTeamedEntity(entity.get().getUniqueId());
        else team.removeTeamedEntity(entity.get().getUniqueId());

      } else throw new InvalidCommandArgument("An entity with that name could not be found.");

      if (holder instanceof TileState) {
        ((TileState) holder).update(true);
      }

    }

  }

}