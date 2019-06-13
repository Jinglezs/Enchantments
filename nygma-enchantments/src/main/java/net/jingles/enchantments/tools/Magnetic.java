package net.jingles.enchantments.tools;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.enchant.TargetGroup;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.Map;

@Enchant(name = "Magnetic", key = "magnetic", levelRequirement = 30, maxLevel = 1, enchantChance = 40,
        targetItem = EnchantmentTarget.TOOL, targetGroup = TargetGroup.DIGGING,
        description = "Allows the player to instantly pick up the drops from any block " +
                "that is broken that has the enchantment on it.")

//TODO:
//   - Fix compatibility with Molten Core
//   - Add support for Backpacks
//   - Fix the terrible description or get Carter to do it cause I suck at doing them :>

public class Magnetic extends CustomEnchant {

    public Magnetic(NamespacedKey key) {
        super(key);
    }

    @Override
    public boolean conflictsWith(Enchantment enchant) { return false; }

    @Override
    public boolean canTrigger(Inventory inventory, Event event) {
        ItemStack tool = getItem(inventory);
        return tool != null && hasEnchantment(tool);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        if (!canTrigger(event.getPlayer().getInventory(), event)) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Collection<ItemStack> drops = block.getDrops();

        event.setCancelled(true);
        block.setType(Material.AIR);
        block.getState().update();

        drops.forEach(item -> tryAddItems(player, block.getLocation(), item));
    }

    @EventHandler
    public void onItemDrop(ItemSpawnEvent event) {

        NamespacedKey identifier = Enchantments.getEnchantmentManager().getEnchantmentKey("molten_core");
        if (identifier == null) return;

        Item item = event.getEntity();
        if (!item.getPersistentDataContainer().has(identifier, PersistentDataType.STRING)) return;

        Player player = Bukkit.getPlayer(item.getPersistentDataContainer().get(identifier, PersistentDataType.STRING));
        if (player == null) return;

        if (canTrigger(player.getInventory(), event)) {
            event.setCancelled(true);
            tryAddItems(player, event.getLocation(), item.getItemStack());
        }
    }

    private void tryAddItems(Player player, Location location, ItemStack items) {
        final Map<Integer, ItemStack> overflow = player.getInventory().addItem(items);
        if (!overflow.isEmpty()) {
            player.getWorld().dropItemNaturally(location, items);
        }
    }
}
