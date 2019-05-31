package net.jingles.enchantments;

import net.jingles.enchantments.enchants.CustomEnchant;
import net.jingles.enchantments.util.RomanNumerals;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Commands implements CommandExecutor {

  private static final String TITLE = ChatColor.DARK_GRAY + "[" + ChatColor.LIGHT_PURPLE + "Enchantments" +
          ChatColor.DARK_GRAY + "] " + ChatColor.RESET;

  private static final String ERROR = TITLE + ChatColor.RED;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {

    if (args.length < 1) {
      sender.sendMessage(ERROR + "Incorrect arguments.");
      return false;
    }

    if (args[0].equalsIgnoreCase("enchant")) {

      if (args.length < 3) {
        sender.sendMessage(ERROR + "Incorrect arguments: /enchantments enchant <level> <enchantment name>");
        return false;
      }

      ItemStack held = getHeldItem(sender);

      if (held == null) {
        sender.sendMessage(ERROR + "You must be holding an item to enchant.");
        return false;
      }

      CustomEnchant enchant = getCustomEnchant(Arrays.copyOfRange(args, 2, args.length));

      if (enchant == null) {
        sender.sendMessage(ERROR + "An enchantment with that name could not be found.");
        return false;
      }

      if (!enchant.canEnchantItem(held)) {
        sender.sendMessage(ERROR + "That enchantment can not be added to this item.");
        return false;
      }

      int level;

      try {
        level = Integer.valueOf(args[1]);
      } catch (NumberFormatException e) {
        sender.sendMessage(ERROR + "That is not a valid enchantment level...");
        return false;
      }

      ItemMeta meta = held.getItemMeta();
      meta.addEnchant(enchant, level, true);

      List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
      lore.add(enchant.getName() + " " + RomanNumerals.toRoman(level));
      meta.setLore(lore);

      held.setItemMeta(meta);
      sender.sendMessage(TITLE + "Successfully added the enchantment to the item!");

    } else if (args[0].equalsIgnoreCase("info")) {

      try {
        CustomEnchant enchant = getCustomEnchant(Arrays.copyOfRange(args, 1, args.length));
        sender.sendMessage(TITLE + enchant.getDescription());
      } catch (NullPointerException | IllegalArgumentException e) {
        sender.sendMessage(ERROR + "An enchantment with that name could not be found.");
      }

    } else if (args[0].equalsIgnoreCase("unenchant")) {

      ItemStack held = getHeldItem(sender);

      if (held == null) {
        sender.sendMessage(ERROR + "You must be holding an item to enchant.");
        return false;
      }

      CustomEnchant enchant = getCustomEnchant(Arrays.copyOfRange(args, 1, args.length));

      if (enchant == null) {
        sender.sendMessage(ERROR + "An enchantment with that name could not be found.");
        return false;
      }

      ItemMeta meta = held.getItemMeta();

      if (meta == null || !meta.hasEnchant(enchant)) {
        sender.sendMessage(ERROR + "This tem does not have that enchantment!");
        return false;
      }

      meta.removeEnchant(enchant);

      if (meta.getLore() != null) {
        List<String> lore = meta.getLore();
        lore.removeIf(line -> line.contains(enchant.getName()));
        meta.setLore(lore);
      }

      held.setItemMeta(meta);
      sender.sendMessage(TITLE + "Successfully removed enchantment.");

    } else if (args[0].equalsIgnoreCase("list")) {

      String list = ChatColor.AQUA + Enchantments.REGISTERED.stream()
              .map(CustomEnchant::getName).collect(Collectors.joining(", "));
      sender.sendMessage(TITLE + "Registered custom enchantments: " + list);

    }

    return true;
  }

  private ItemStack getHeldItem(CommandSender sender) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(ERROR + "Only players can use this command!");
    } else {

      Player player = (Player) sender;
      ItemStack held = player.getInventory().getItemInMainHand();

      if (held.getType() == Material.AIR) {
        player.sendMessage(ERROR + "You must be holding an item to enchant.");
      } else return held;

    }

    return null;
  }

  private CustomEnchant getCustomEnchant(String... args) {
    String name = String.join("_", args).toLowerCase();
    return Enchantments.REGISTERED.stream()
            .filter(enchant -> enchant.getKeyName().equals(name))
            .findAny().orElse(Enchantments.REGISTERED.stream()
                    .filter(enchant -> enchant.getName().equalsIgnoreCase(name.replace("_", " ")))
                    .findAny().orElse(null));
  }

}
