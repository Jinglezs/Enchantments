package net.jingles.enchantments.enchants.armor;

import net.jingles.enchantments.Enchant;
import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchants.CustomEnchant;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Enchant(name = "Eluding", key = "eluding", targetItem = EnchantmentTarget.ARMOR_FEET, cooldown = 2, levelRequirement = 20,
        description = "Grants the player a chance to be given a 3 second (multiplied by 2 per additional level) speed boost if hit by any entity while under half health")

//TODO: fix the enchantment description so it's not lame af and awkward sounding
//TODO: raise level requirement & cool-down?
//TODO: Some stuff needs maybe general tweaking, like the probability chance, etc.

public class Eluding extends CustomEnchant {

    public Eluding(NamespacedKey key) {
        super(key);
    }

    @Override
    public boolean conflictsWith(Enchantment other) { return false; }

    @Override
    public boolean canTrigger(Inventory inventory, Event event) {
        ItemStack boots = getItem(inventory);
        return boots != null && hasEnchantment(boots) &&
                !Enchantments.getCooldownManager().hasCooldown(((Player) inventory.getHolder()).getUniqueId(), this);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Player)) { return; }

        Player player = (Player) event.getEntity();

        if (player.getHealth() > 10) return;
        if (!canTrigger(player.getInventory(), event)) return;

        int level = getItem(player.getInventory()).getItemMeta().getEnchantLevel(this);
        double probability = 0.25 + ((level * 5) / 100D); // Max level can be 5, equals to a .5 chance every time hit - if other conditions met

        if (Math.random() >= probability) return;

        int speedDuration = level * 3;
        PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED, speedDuration * 20, 2); //duration multiplied by 20 because its in ticks

        if (player.hasPotionEffect(speedEffect.getType())) {
            player.removePotionEffect(speedEffect.getType());
        }

        player.addPotionEffect(speedEffect);

        int mobFilterRadius = 5 + (3 * level);
        PotionEffect slownessEffect = new PotionEffect(PotionEffectType.SLOW, level * 20, 4);

        player.getNearbyEntities(mobFilterRadius, mobFilterRadius, mobFilterRadius).stream()
                .filter(entity -> entity instanceof Mob)
                .map(entity -> (Mob) entity)
                .filter(entity -> entity.getTarget() != null && entity.getTarget().equals(player))
                .forEach(entity -> {
                    entity.addPotionEffect(slownessEffect);
                    entity.setTarget(null);
                });

        TextComponent message = new TextComponent("Elusive speed boost applied for " + speedDuration + " seconds");
        message.setColor(ChatColor.GOLD);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);
    }
}
