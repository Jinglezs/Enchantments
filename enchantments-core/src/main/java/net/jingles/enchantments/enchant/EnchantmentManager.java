package net.jingles.enchantments.enchant;

import net.jingles.enchantments.Enchantments;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class EnchantmentManager {

  private final Set<Class<? extends CustomEnchant>> enchantmentClasses = new HashSet<>();
  private final Set<CustomEnchant> registered = new HashSet<>();
  private final NamespacedKey fallDamage;

  private final Enchantments plugin;
  private Reflections reflections;

  public EnchantmentManager(Enchantments plugin) {
    this.plugin = plugin;
    if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
    loadClasses(plugin.getDataFolder());

    fallDamage = new NamespacedKey(plugin, "fall_damage");
  }

  public Set<CustomEnchant> getRegisteredEnchants() {
    return this.registered;
  }

  private void loadClasses(File start) {
    
    try { //Collects the URLs of all non-yaml files in the data folder
      List<URL> classFiles = Files.walk(start.toPath(), Integer.MAX_VALUE)
          .map(file -> {
            try {
              return file.toUri().toURL();
            } catch (MalformedURLException e) {
              throw new IllegalStateException("Error loading hero class file: " + file.getFileName().toString());
            }
          })
          .filter(url -> !url.getFile().endsWith(".yml"))
          .collect(Collectors.toList());

      //Creates the class loader to be used by Reflections to load the classes
      URLClassLoader loader = new URLClassLoader(classFiles.toArray(new URL[]{}), getClass().getClassLoader());

      //Create reflections instance with the URLs and class loader.
      reflections = new Reflections(new ConfigurationBuilder().addUrls(classFiles).addClassLoader(loader)
          .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner()));

    } catch (IOException e) {
      System.out.println("Error loading hero class files!");
    }

    //Gets all classes in the data folder annotated with Enchant and adds them to the set
    //of found CustomEnchant classes.
    reflections.getTypesAnnotatedWith(Enchant.class).forEach(clazz ->
        enchantmentClasses.add((Class<? extends CustomEnchant>) clazz));

   System.out.println("Loaded " + enchantmentClasses.size() + " custom enchantments");
   instantiateClasses();
  }

  private void instantiateClasses() {

    enchantmentClasses.forEach(enchant -> {

      Enchant annotation = enchant.getAnnotation(Enchant.class);
      NamespacedKey key = new NamespacedKey(plugin, annotation.key());
      CustomEnchant enchantment;

      try {

        enchantment = enchant.getConstructor(NamespacedKey.class).newInstance(key);
        plugin.getServer().getPluginManager().registerEvents(enchantment, plugin);
        registered.add(enchantment);

        Enchantment.registerEnchantment(enchantment);

      } catch (IllegalArgumentException | NoSuchMethodException | InstantiationException | InvocationTargetException |
          IllegalAccessException | IllegalStateException e) {
        //Only thrown when a duplicate enchantment is registered (plugin reload).
        if (!(e instanceof IllegalArgumentException)) e.printStackTrace();
      }

    });

  }

  public CustomEnchant getEnchantmentByKey(String key) {
    return getRegisteredEnchants().stream()
        .filter(customEnchant -> customEnchant.getKeyName().equalsIgnoreCase(key))
        .findFirst().orElse(null);
  }

  public NamespacedKey newNamespacedKey(String key) {
    return new NamespacedKey(plugin, key);
  }

  public NamespacedKey getEnchantmentKey(String key) {
    CustomEnchant enchant = getEnchantmentByKey(key);
    return enchant != null ? enchant.getKey() : null;
  }

  // This key is used in unity with the Persistent Data API to
  // negate fall damage with only a single listener.
  public NamespacedKey getFallDamageKey() {
    return this.fallDamage;
  }

}
