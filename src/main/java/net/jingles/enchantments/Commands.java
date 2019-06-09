package net.jingles.enchantments;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import net.jingles.enchantments.cooldown.CooldownManager;
import net.jingles.enchantments.enchants.CustomEnchant;
import net.jingles.enchantments.util.RomanNumerals;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@CommandAlias("enchantments")
public class Commands extends BaseCommand {

  private static final String TITLE = ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "Enchantments" +
          ChatColor.DARK_GRAY + "] " + ChatColor.RESET;

  @Dependency
  private CooldownManager cooldownManager;

  @HelpCommand
  public void onEnchantHelpCommand(CommandHelp help) {
    //Sends the CommandSender the automagykally generated help message.
    help.showHelp();
  }

  @CommandAlias("enchant") @Conditions("operator")
  @CommandCompletion("@nothing @range:1-20 @enchantments")
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

  @CommandAlias("disenchant") @Conditions("operator")
  @CommandCompletion("@nothing @enchantments")
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
  @Description("Shows the executor a complete list of registered custom enchantment names.")
  public void onEnchantmentList(CommandSender sender) {
    String enchants = Enchantments.REGISTERED.stream().map(CustomEnchant::getName)
            .collect(Collectors.joining(ChatColor.WHITE + ", " + ChatColor.GOLD));
    sender.sendMessage(TITLE + "Registered custom enchantments: " + ChatColor.GOLD + enchants);
  }

  @Subcommand("info")
  @Syntax("<enchantment name>")
  @CommandCompletion("@enchantments")
  @Description("Shows the executor information about the provided enchantment")
  public void onEnchantmentInfo(CommandSender sender, CustomEnchant enchant) {
    sender.sendMessage(TITLE + enchant.getDescription());
  }

  private static final String COOLDOWN_HEADER = ChatColor.DARK_GRAY + "+-------+ " + ChatColor.AQUA +
      "Enchantment Cooldowns " + ChatColor.DARK_GRAY + "+-------+\n" + ChatColor.RESET;

  //Replacements: Name, cooldown, time unit
  private static final String COOLDOWN_INFO = ChatColor.GOLD + " - " + ChatColor.AQUA + "%s " + ChatColor.GOLD + ": "
      + ChatColor.RED + "%d %s";

  @CommandAlias("cooldowns")
  public void onCooldownInfo(Player player) {

    String cooldownMessage = cooldownManager.getCooldowns().stream()
        .filter(entry -> player.getUniqueId().equals(entry.getRow()))
        .map(entry -> {
          long remaining = entry.getCol().getTimeUnit().convert(entry.getValue() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
          return String.format(COOLDOWN_INFO, entry.getCol().getName(), (int) remaining, entry.getCol().getTimeUnit().name().toLowerCase());
        })
        .collect(Collectors.joining("\n"));

    player.sendMessage(COOLDOWN_HEADER + (!cooldownMessage.isEmpty() ? cooldownMessage : " - You do not have any active enchantment cooldowns"));
  }

}
