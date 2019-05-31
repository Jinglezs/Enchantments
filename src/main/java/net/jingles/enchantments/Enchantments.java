package net.jingles.enchantments;

import net.jingles.enchantments.enchants.CustomEnchant;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

public class Enchantments extends JavaPlugin {

  public static final Set<CustomEnchant> REGISTERED = new HashSet<>();
  public static MetadataValue METADATA;

  @Override
  public void onEnable() {

    enableRegistrations();

    //Register all custom enchantment classes, which are annotated with @Enchant
    Reflections reflections = new Reflections("net.jingles.enchantments.enchants");
    reflections.getTypesAnnotatedWith(Enchant.class).forEach(enchant -> {

      Enchant annotation = enchant.getAnnotation(Enchant.class);
      NamespacedKey key = new NamespacedKey(this, annotation.key());
      CustomEnchant enchantment;

      try {

        enchantment = (CustomEnchant) enchant.getConstructor(NamespacedKey.class).newInstance(key);
        getServer().getPluginManager().registerEvents(enchantment, this);
        REGISTERED.add(enchantment);

        Enchantment.registerEnchantment(enchantment);

      } catch (IllegalArgumentException | NoSuchMethodException | InstantiationException | InvocationTargetException |
              IllegalAccessException | IllegalStateException e) {
        //Only thrown when a duplicate enchantment is registered (plugin reload).
        if (!(e instanceof IllegalArgumentException)) e.printStackTrace();
      }

    });

    getServer().getPluginManager().registerEvents(new EnchantListener(), this);
    getCommand("enchantments").setExecutor(new Commands());
    METADATA = new FixedMetadataValue(this, 0);
  }

  //Ignore what Spigot has to say and forcefully enable enchantment registration
  //Has to be done using reflection because the fields are private.
  @SuppressWarnings("unchecked") //The type of the field is guaranteed, so it is safe to cast.
  private void enableRegistrations() {
    try {

      Field field = Enchantment.class.getDeclaredField("acceptingNew");
      field.setAccessible(true);
      field.set(null, true);

    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
      getLogger().warning("Could not enable Enchantments! The server is not accepting enchantment registrations.");
      setEnabled(false);
    }
  }

}
