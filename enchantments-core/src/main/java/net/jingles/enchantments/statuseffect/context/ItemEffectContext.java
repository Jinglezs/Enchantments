package net.jingles.enchantments.statuseffect.context;

import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.statuseffect.PersistentEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

public class ItemEffectContext implements EffectContext {

  private final LivingEntity trigger;
  private final ItemStack item;
  private final CustomEnchant source;
  private final PersistentDataContainer container;

  public ItemEffectContext(LivingEntity trigger, ItemStack item, CustomEnchant source) {

    this.trigger = trigger;
    this.item = item;
    this.source = source;

    this.container = (item == null || item.getItemMeta() == null) ? null :
        item.getItemMeta().getPersistentDataContainer();

  }

  @Override
  public CustomEnchant getSource() {
    return this.source;
  }

  public LivingEntity getTrigger() {
    return this.trigger;
  }

  public ItemStack getItem() {
    return this.item;
  }

  @Override
  public PersistentDataContainer getContainer() {
    return this.container;
  }

  @Override
  public void serialize(PersistentEffect effect) {

    if (item == null || item.getItemMeta() == null) return;

    ItemMeta meta = item.getItemMeta();
    effect.serialize(meta.getPersistentDataContainer());
    item.setItemMeta(meta);

  }

  @Override
  public void deserialize(PersistentEffect effect) {
    if (item == null || item.getItemMeta() == null) return;
    effect.deserialize(item.getItemMeta().getPersistentDataContainer());
  }

}
