package net.jingles.enchantments;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.jingles.enchantments.enchants.CustomEnchant;
import net.jingles.enchantments.util.RomanNumerals;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Commands extends BaseCommand {

  @Dependency
  private Enchantments plugin;

  private static final String TITLE = ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "Enchantments" +
          ChatColor.DARK_GRAY + "] " + ChatColor.RESET;

  private static final String ERROR = TITLE + ChatColor.RED;

  @HelpCommand
  @CommandAlias("enchantments help")
  @CatchUnknown @Default
  public void onEnchantHelpCommand(CommandSender sender) {
    //Sends the CommandSender the automagykally generated help message.
    getCommandHelp().showHelp();
  }

  @CommandAlias("enchant")
  @CommandCompletion("enchantments")
  @Conditions("operator")
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
  @CommandCompletion("enchantments")
  @Conditions("operator")
  @Syntax("<enchantment name>")
  @Description("Removes the given custom enchantment from the item held in the executor's main (right) hand.")
  public void onDisenchant(@Conditions("holdingItem") Player player, ItemStack item, CustomEnchant enchant) {

    ItemMeta meta = item.getItemMeta();

    if (!meta.hasEnchant(enchant)) {
      player.sendMessage(ERROR + "This item does not have " + enchant.getName());
      return;
    }

    meta.removeEnchant(enchant);

    if (meta.hasLore()) {
      List<String> lore = meta.getLore();
      lore.removeIf(line -> line.contains(enchant.getName()));
      meta.setLore(lore);
    }

    item.setItemMeta(meta);
    player.sendMessage(TITLE + "Successfully disenchanted the item.");
  }

  @CommandAlias("enchantments list")
  @Description("Shows the executor a complete list of registered custom enchantment names.")
  public void onEnchantmentList(CommandSender sender) {
    String enchants = Enchantments.REGISTERED.stream().map(CustomEnchant::getName).collect(Collectors.joining(ChatColor.WHITE + ", " + ChatColor.AQUA));
    sender.sendMessage(TITLE + "Registered custom enchantments: " + ChatColor.AQUA + enchants);
  }

  public void onEnchantmentInfo() {
    //TODO: Nyghoe do this for me plez. Examples above hoE
  }

}
