package net.jingles.enchantments.enchant;

import net.jingles.enchantments.Enchantments;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
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
  private Reflections reflections;

  public EnchantmentManager(Enchantments plugin) {
    if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
    loadClasses(plugin.getDataFolder());
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

    //Gets all classes in the data folder annotated with HeroInfo and adds them to the set
    //of found Hero classes.
    reflections.getTypesAnnotatedWith(Enchant.class).forEach(clazz ->
        enchantmentClasses.add((Class<? extends CustomEnchant>) clazz));

   System.out.println("Loaded " + enchantmentClasses.size() + " custom enchantments");

  }

}
