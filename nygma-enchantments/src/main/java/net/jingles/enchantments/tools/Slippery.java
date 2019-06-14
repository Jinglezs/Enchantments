package net.jingles.enchantments.tools;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Enchant(name = "Slippery", key = "slippery", levelRequirement = 10, enchantChance = 0.20, maxLevel = 3,
        targetItem = EnchantmentTarget.ALL, targetGroup = TargetGroup.NON_WEARABLE, cursed = true,
        description = "Pretend this is a good enchantment description for once :D")

//TODO:
//   - Uhm make it work would be nice
//   - description -> (Drop probability: 15% + 5% per additional level)

public class Slippery extends CustomEnchant {

    public Slippery(NamespacedKey key) {
        super(key);
    }

    @Override
    public boolean conflictsWith(Enchantment enchant) { return false; }

    @Override
    public boolean canTrigger(Inventory inventory, Event event) {
        ItemStack item = getItem(inventory);
        return item != null && hasEnchantment(item);
    }

    @EventHandler
    public void onMainHandSwitch(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item == null || !canTrigger(player.getInventory(), event)) return;
        tryDrop(player, item);
    }

    @EventHandler
    public void onSwitchToOffhand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getOffHandItem();
        if (item == null || !canTrigger(player.getInventory(), event)) return;
        tryDrop(player, item);
    }

    private void tryDrop(Player player, ItemStack item) {

        if (!player.getInventory().contains(item)) return;

        int level = getItem(player.getInventory()).getItemMeta().getEnchantLevel(this);
        double probability = 0.10 + ((level * 5) / 100D);
        if (Math.random() >= probability) return;

        player.getInventory().removeItem(item);
        player.getWorld().dropItemNaturally(player.getLocation(), item);

        TextComponent dropMessage = new TextComponent("Nice grip, m8");
        dropMessage.setColor(ChatColor.RED);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, dropMessage);
    }
}
