package net.jingles.enchantments.persistence;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class EnchantTeam implements Serializable {

  private final HashSet<UUID> teamedEntities;
  private boolean includeTamedAnimals;

  public EnchantTeam(HashSet<UUID> teamedEntities, boolean includeTamedAnimals) {
    this.teamedEntities = teamedEntities;
    this.includeTamedAnimals = includeTamedAnimals;
  }

  private boolean isDefinitelyTeamed(UUID id) {
    return teamedEntities.contains(id);
  }

  public boolean isTeamed(Entity entity) {

    if (!includeTamedAnimals || !(entity instanceof Tameable))
      return isDefinitelyTeamed(entity.getUniqueId());

    return Optional.ofNullable(((Tameable) entity).getOwner())
        .map(owner -> isDefinitelyTeamed(owner.getUniqueId()))
        .orElse(false);

  }

  public boolean getIncludeTamedAnimals() {
    return this.includeTamedAnimals;
  }

  public void setIncludeTamedAnimals(boolean includeTamedAnimals) {
    this.includeTamedAnimals = includeTamedAnimals;
  }

  public void addTeamedEntity(UUID id) {
    teamedEntities.add(id);
  }

  public void removeTeamedEntity(UUID id) {
    teamedEntities.remove(id);
  }

}
