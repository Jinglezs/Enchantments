package net.jingles.enchantments.weapon;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Enchant(name = "Decapitation", key = "decapitation", levelRequirement = 15, maxLevel = 4,
        targetItem = EnchantmentTarget.WEAPON, enchantChance = 0.15, description = "Upon killing a mob that is " +
        "capable of dropping a head, there is 10% chance per level that the mob's head will be added to its drops")

public class Decapitation extends CustomEnchant {

  private final Map<EntityType, Material> DROPPABLE = new HashMap<>();

  public Decapitation(NamespacedKey key) {
    super(key);
    DROPPABLE.put(EntityType.ZOMBIE, Material.ZOMBIE_HEAD);
    DROPPABLE.put(EntityType.SKELETON, Material.SKELETON_SKULL);
    DROPPABLE.put(EntityType.WITHER_SKELETON, Material.WITHER_SKELETON_SKULL);
    DROPPABLE.put(EntityType.CREEPER, Material.CREEPER_HEAD);
    DROPPABLE.put(EntityType.ENDER_DRAGON, Material.DRAGON_HEAD);
    DROPPABLE.put(EntityType.PLAYER, Material.PLAYER_HEAD);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment enchant) {
    return false;
  }

  @Override
  public boolean canTrigger(@NotNull LivingEntity entity) {
    ItemStack weapon = getItem(entity);
    return weapon != null && hasEnchantment(weapon);
  }


  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {

    if (event.getEntity().getKiller() == null) return;

    Player player = event.getEntity().getKiller();

    if (!canTrigger(player) || !DROPPABLE.containsKey(event.getEntityType())) return;

    int level = getLevel(getItem(player));
    double probability = (10 * level) / 100D;
    if (Math.random() >= probability) return;

    ItemStack droppedHead = new ItemStack(DROPPABLE.get(event.getEntityType()));

    if (event.getEntity() instanceof Player) {
      SkullMeta skullMeta = (SkullMeta) droppedHead.getItemMeta();
      skullMeta.setOwningPlayer((Player) event.getEntity());
      droppedHead.setItemMeta(skullMeta);
    }

    event.getDrops().add(droppedHead);

    TextComponent message = new TextComponent("Successfully decapitated target");
    message.setColor(ChatColor.RED);
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);
  }

}
