package net.jingles.enchantments.armor.helmet;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.statuseffect.context.ItemEffectContext;
import net.jingles.enchantments.statuseffect.entity.EntityStatusEffect;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Enchant(name = "Thinking Cap", key = "thinking_cap", cursed = true, enchantChance = 0.1,
  targetItem = EnchantmentTarget.ARMOR_HEAD, description = "The wearer will randomly demonstrate " +
    "how hard they are thinking to everyone.")
public class ThinkingCap extends CustomEnchant {

  public ThinkingCap(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(@NotNull LivingEntity entity) {
    return hasEnchantment(getItem(entity));
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {

    if (!canTrigger(event.getPlayer())) return;

    Player player = event.getPlayer();
    ItemEffectContext context = new ItemEffectContext(player, getItem(player), this);
    Enchantments.getStatusEffectManager().add(new ThinkingCapEffect(context));

  }

  private static class ThinkingCapEffect extends EntityStatusEffect {

    private static final List<String> MESSAGES = Arrays.asList("hmmmmm...", "interesting...",
        "what if...", "thonking...");

    double chance = 0.15;

    private ThinkingCapEffect(ItemEffectContext context) {
      super(context.getTrigger(), context, Integer.MAX_VALUE, 100);
    }

    @Override
    public void effect() {

      ItemEffectContext context = (ItemEffectContext) getContext();
      ItemStack helmet = context.getTrigger().getEquipment().getHelmet();

      if (!context.getItem().equals(helmet)) {
        this.stop();
        return;
      }

      if (Math.random() > chance) return;

      char[] message = MESSAGES.get(ThreadLocalRandom.current().nextInt(MESSAGES.size())).toCharArray();

      for (int i = 0; i < message.length; i++) {
        if (i % 2 == 0) message[i] = Character.toUpperCase(message[i]);
      }

      String edited = new String(message);
      ((Player) context.getTrigger()).chat(edited);

    }

  }

}
