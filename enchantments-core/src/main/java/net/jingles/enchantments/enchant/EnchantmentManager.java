package net.jingles.enchantments.enchant;

import net.jingles.enchantments.Enchantments;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
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
import java.util.stream.Stream;

public final class EnchantmentManager {

  private final Set<Class<? extends CustomEnchant>> enchantmentClasses = new HashSet<>();
  private final Set<CustomEnchant> registered = new HashSet<>();

  private final Enchantments plugin;
  private Reflections reflections;

  public EnchantmentManager(Enchantments plugin) {
    this.plugin = plugin;
    if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
  }

  @NotNull
  public Set<CustomEnchant> getRegisteredEnchants() {
    return this.registered;
  }

  public void loadClasses(File start) {
    
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

  /**
   * Gets all registered CustomEnchants that extend BlockEnchant
   * @return registered BlockEnchants
   */
  @NotNull
  public Set<BlockEnchant> getRegisteredBlockEnchants() {
    return getRegisteredEnchants().stream()
        .filter(enchant -> enchant instanceof BlockEnchant)
        .map(enchant -> (BlockEnchant) enchant)
        .collect(Collectors.toSet());
  }

  public void loadBlockEnchants(World world) {
    for (Chunk chunk : world.getLoadedChunks())
      loadBlockEnchants(chunk);
  }

  public void loadBlockEnchants(Chunk chunk) {
    Stream.of(chunk.getTileEntities())
        .filter(state -> state instanceof TileState)
        .map(state -> (TileState) state)
        .forEach(tile -> BlockEnchant.getBlockEnchants(tile.getPersistentDataContainer())
            .keySet().forEach(enchant -> enchant.onChunkLoad(tile)));
  }

  public NamespacedKey createKey(String key) {
    return new NamespacedKey(plugin, key);
  }

}
