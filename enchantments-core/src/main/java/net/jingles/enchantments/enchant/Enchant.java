package net.jingles.enchantments.enchant;

import org.bukkit.enchantments.EnchantmentTarget;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Enchant {

  //Custom enchantment information:

  String name();

  String key();

  int levelRequirement() default 1;

  String description() default "A custom enchantment";

  long cooldown() default 0;

  TimeUnit timeUnit() default TimeUnit.SECONDS;

  TargetGroup targetGroup() default TargetGroup.NONE;

  double enchantChance() default 0.35;

  boolean hasPersistence() default false;

  //Standard enchantment information:

  int startingLevel() default 1;

  int maxLevel() default 5;

  EnchantmentTarget targetItem() default EnchantmentTarget.ALL;

  boolean treasure() default false;

  boolean cursed() default false;

}
