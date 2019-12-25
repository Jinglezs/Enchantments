package net.jingles.enchantments.block;

import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.jingles.enchantments.util.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

@Enchant(name = "Enchanter", key = "enchanter", levelRequirement = 30, maxLevel = 1,
    enchantChance = 0.7, targetItem = EnchantmentTarget.ALL, targetGroup = TargetGroup.LECTERN,
    description = "Allows player to receive a specific enchantment with a random level by writing its name in " +
        "a book and quill, placing it on the enchanted Lectern, and removing it.")

public class Enchanter extends BlockEnchant {

  public Enchanter(NamespacedKey key) {
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

  @EventHandler
  public void onBookRemove(PlayerTakeLecternBookEvent event) {

    if (!canTrigger(event.getLectern())) return;

    ItemStack book = event.getBook();
    BookMeta bookMeta = (BookMeta) book.getItemMeta();

    // Don't want to casually erase books... oops
    if (bookMeta.getPageCount() > 1) return;

    // Why the hell are pages 1-indexed instead of 0-indexed???
    // Someone please explain this logic >:VVVVVV
    String enchantName = bookMeta.getPage(1).trim();

    Stream.of(Enchantment.values())
        .filter(enchant -> enchant.getName().equalsIgnoreCase(enchantName))
        .findAny()
        .ifPresent(enchant -> {

          int level = ThreadLocalRandom.current().nextInt(enchant.getStartLevel(), enchant.getMaxLevel() + 1);
          int requiredExp = 3;

          book.setType(Material.ENCHANTED_BOOK);
          EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
          meta.addStoredEnchant(enchant, level, true);

          if (CustomEnchant.isCustomEnchant(enchant)) {
            requiredExp = ((CustomEnchant) enchant).getLevelRequirement();
            InventoryUtils.addEnchantLore(meta, Collections.singletonMap(enchant, level));
          }

          Player player = event.getPlayer();
          int xp = player.getLevel();

          if (xp < 10 || xp < requiredExp) {
            player.sendMessage(ChatColor.RED + "You can't afford this enchantment.");
            return;
          }

          // Add the enchantment to the book
          book.setItemMeta(meta);

          // Deduct the cost from the player's xp.
          int cost = Math.min(xp, ThreadLocalRandom.current().nextInt(10, requiredExp));
          player.setLevel(xp - cost);
          player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);

        });

  }

}
