package net.jingles.enchantments.statuseffect.context;

import net.jingles.enchantments.enchant.BlockEnchant;
import net.jingles.enchantments.enchant.CustomEnchant;
import net.jingles.enchantments.statuseffect.PersistentEffect;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;

public class TileEntityContext implements EffectContext {

  private final BlockEnchant source;
  private final TileState trigger;

  public TileEntityContext(TileState trigger, BlockEnchant source) {
    this.trigger = trigger;
    this.source = source;
  }

  public TileState getTrigger() {
    return this.trigger;
  }

  @Override
  public CustomEnchant getSource() {
    return this.source;
  }

  @Override
  public PersistentDataContainer getContainer() {
    return trigger.getPersistentDataContainer();
  }

  @Override
  public void serialize(PersistentEffect effect) {
    //TODO:
  }

  @Override
  public void deserialize(PersistentEffect effect) {
    //TODO:
  }

}
