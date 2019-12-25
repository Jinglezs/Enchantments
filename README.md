# Enchantments
A bunch of custom enchantments because why not...

Enchantments is essentially a library that allows developers to create customized enchantments
for to all items and a small selection of blocks. It provides a few additional utilities that
can extend enchantment functionality further, such as status effects and cooldowns.

### Navigation: 
- [Basic Functionality](Basic-Functionality)
- [Block Enchantments](Block-Enchantments)
- [Creating Your Own Enchantments](Creating-Your-Own-Enchantments)
- [Cooldown System](Cooldown-System)
- [Enchantment Targets](Enchantment-Targets)
- [Status Effects](Status-Effects)

### Basic Functionality
Custom enchantments are an extension of Bukkit's Enchantment class and act like any vanilla
enchantment: items they are applied to glow and they can be applied in an enchantment table or
through anvils and enchanted books.

### Block Enchantments
Enchantments can only be applied to items, of course, but certain blocks permit persistent
data to be saved to their *tile entities*, which is how block enchantments function. Block 
enchantments only become active once the block is placed and have no effect as an item. They
are only active as long as their tile entity is currently active, which means the chunk they
are located in must be loaded. Developers can define what happens when the block is loaded
and unloaded - by default, nothing happens on chunk load and location effects originating
from the block's location are cancelled on chunk unload.

### Creating Your Own Enchantments
A custom enchantment is easy enough to make, as all you have need to do is extend CustomEnchant or
BlockEnchant and add a @Enchant annotation at the top of your class. There is no need to register 
anything, as Enchantments will find any class with the @Enchant annotation and register it automatically.
From there, you can create event listeners just as you would do in any Bukkit plugin - all
custom enchants implement Listener by default and are automatically registered. This allows you to
define what actions trigger the enchantment.

Block Enchantments extend BlockEnchant and can only be applied to blocks that also double as
a *Tile Entity*. Generally speaking, if the block is listed as a subinterface of [this class](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/TileState.html),
then it can be enchanted.

Please note that custom enchantments will not be found if you package them as part of enchantments-core.
You must create a project that depends on enchantments-core and package your classes together in
a different jar. That jar must be placed in folder named "Enchantments" in the plugins folder of
your server, which is where Enchantments scans for custom enchantment classes.

### Cooldown System
Enchantment cooldowns are persistent, but the player does not have to be online for them to expire.
Cooldowns are not saved to a database or other file, rather they are saved to the entity itself, 
meaning they are applicable to both players and tile entities (block enchantments). An enchantment's
cooldown duration is defined in the @Enchant annotation, where cooldown() refers to the amount and
timeUnit() refers to which unit of time to use, such as minutes or seconds. Cooldown durations are
limited to whole numbers, so if you want fractions of a time unit, you must use the time unit 
below it. For example, use 2500 milliseconds instead of 2.5 seconds.

### Enchantment Targets
Enchantment targets, which refers to the EnchantmentTarget and TargetGroup enums, enable enchantments to 
be applied to items that are usually not enchantable in vanilla Minecraft. They are both defined in the
@Enchant annotation, but they cannot be used interchangeably. If the TargetGroup refers to items that are
not enchantable in vanilla Minecraft, the EnchantmentTarget must be ALL. Otherwise, the TargetGroup can
be used to further refine an EnchantmentTarget. For example, if EnchantmentTarget is TOOLS, the TargetGroup
can be set to SHOVELS to limit the enchantment to only shovels, not pickaxes or axes. The EnchantmentTarget
is ALL by default, and the TargetGroup is NONE. 

### Status Effects
Since each enchantment is a *singleton*, status effects enable to developers to limit effects to and hold data 
for a specific target, which can either be an entity or a location. Status Effects are the enchantment-specific 
equivalent of a Runnable, except they are all managed by the StatusEffectManager and executed on the main thread. 
They permit the repeated execution of code and interactions between different enchantments, as they can be retrieved
by source (the enchantment that created it), by class, and even by the entity or location they are being applied to
via the manager, which can be statically accessed in the Enchantments class.

By default, status effects are stopped when an player/entity disconnects/dies and on server shutdown, 
but they have the potential to be persistent through PersistentEffects, which are simple interfaces 
that define how the effectis serialized and deserialized.

Every status effect has an EffectContext, which defines the enchantment that created it, the item or
tile entity that owns the enchantment, and a different method of (de)serialization that is unique to
each context implementation.
