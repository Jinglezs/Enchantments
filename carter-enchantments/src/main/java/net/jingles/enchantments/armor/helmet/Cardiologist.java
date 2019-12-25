package net.jingles.enchantments.armor.helmet;

import net.jingles.enchantments.Enchantments;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.enchant.Enchant;
import net.jingles.enchantments.statuseffect.context.ItemEffectContext;
import net.jingles.enchantments.statuseffect.entity.EntityStatusEffect;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

@Enchant(name = "Cardiologist", key = "cardiologist", targetItem = EnchantmentTarget.ARMOR_HEAD, 
  description = "Damaging an entity causes their health to be displayed in a bossbar for 7 seconds per level.")
public class Cardiologist extends CustomEnchant {

  public Cardiologist(NamespacedKey key) {
    super(key);
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @Override
  public boolean canTrigger(LivingEntity entity) {
    ItemStack helm = getItem(entity);
    return helm != null && hasEnchantment(helm); 
  }

  @EventHandler
  public void onPlayerDamageEntity(EntityDamageByEntityEvent event) {

    Player player = null;

    if (event.getDamager() instanceof Player) {
      player = (Player) event.getDamager();
    } else if (event.getDamager() instanceof Arrow) {
      ProjectileSource source = ((Arrow) event.getDamager()).getShooter();
      if (source instanceof Player) player = (Player) source;
      else return;
    }

    if (!canTrigger(player)) return;

    LivingEntity target = (LivingEntity) event.getEntity();

    boolean hasEffect = Enchantments.getStatusEffectManager().getEntityContainer(target.getUniqueId())
      .map(container -> container.hasEffect(CardiologistEffect.class))
      .orElse(false);

    if (hasEffect) return;

    int duration = (getLevel(getItem(player)) * 7) * 20;

    ItemEffectContext context = new ItemEffectContext(player, getItem(player), this);
    Enchantments.getStatusEffectManager().add(new CardiologistEffect(context, player, target, duration));
  
  }

  private class CardiologistEffect extends EntityStatusEffect {
    
    private Player owner;
    private double maxHealth;
    private BossBar bar;

    public CardiologistEffect(ItemEffectContext context, Player owner, LivingEntity target, int duration) {
      super(target, context, duration, 1);

      this.owner = owner;

      maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
      double percent = target.getHealth() / maxHealth;

      String title = target.getCustomName() != null ? target.getCustomName() : 
        StringUtils.capitalize(target.getType().name().toLowerCase().replace("_", ""));

      this.bar = Bukkit.createBossBar(title, getBarColor(percent), BarStyle.SOLID);

      bar.setVisible(true);
      bar.setProgress(percent);
      bar.addPlayer(owner);

    }

    @Override
    public void effect() {

      if (!canTrigger(owner) || getTarget() == null || getTarget().isDead()) {
        this.stop();
        return;
      }

      double percent = getTarget().getHealth() / maxHealth;
      bar.setColor(getBarColor(percent));
      bar.setProgress(percent);

    }

    @Override
    public void stop() {
      bar.removeAll();
    }

    private BarColor getBarColor(double percent) {
      if (percent < 0.25) return BarColor.RED;
      else if (percent < 0.5) return BarColor.YELLOW;
      else return BarColor.GREEN;
    }

  }

}