/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry;

// Java Standard
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

// Fabric
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

// Minecraft
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;

// Magnatour
import roeyqian.magnatour.Magnatour;

public interface StuffHelper {

  static Identifier id(
      String path
  ) {
    return Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, path);
  }

  static <T extends AbstractContainerMenu> MenuType<T> register(
      String path,
      MenuType.MenuSupplier<T> factory
  ) {
    return Registry.register(
        BuiltInRegistries.MENU,
        Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, path),
        new MenuType<>(factory, FeatureFlags.VANILLA_SET)
    );
  }

  static <T extends Entity> EntityType<T> register(
      ResourceKey<EntityType<?>> key,
      EntityType.EntityFactory<T> factory,
      MobCategory category,
      float width, float height
  ) {
    return Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        key,
        EntityType.Builder.of(factory, category).sized(width, height).build(key)
    );
  }

  static ResourceKey<EntityType<?>> entityKey(
      String path
  ) {
    return ResourceKey.create(Registries.ENTITY_TYPE, id(path));
  }

  @SuppressWarnings("DataFlowIssue")
  static <T extends LivingEntity> void registerAttributes(
      EntityType<T> type,
      AttributeSupplier.Builder attributes
  ) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(attributes, "attributes");
    FabricDefaultAttributeRegistry.register(type, attributes);
  }

  static Block registerBase(
      String name, String type,
      Function<BlockBehaviour.Properties, Block> factory,
      BlockBehaviour.Properties properties
  ) {
    if (Objects.equals(type, "supreme")) {
      return register(
          name, factory, properties.strength(64.0F, 2400.0F),
          setting -> setting.rarity(Rarity.RARE)
      );
    } else {
      return register(
          name, factory, properties.strength(256.0F, 3600000.0F),
          setting -> setting.rarity(Rarity.EPIC)
      );
    }
  }

  static Item registerConsumableItem(
      String name,
      int stacks,
      Function<Item.Properties, Item> factory,
      Item.Properties settings
  ) {
    return registerItem(name, factory, settings.stacksTo(stacks));
  }

  static Block registerCrop(
      String name, String type,
      Function<BlockBehaviour.Properties, Block> factory,
      BlockBehaviour.Properties properties
  ) {
    if (Objects.equals(type, "supreme")) {
      return register(
          name, factory, properties
              .noCollision()
              .randomTicks()
              .instabreak()
              .sound(SoundType.CROP)
              .pushReaction(PushReaction.POPPED),
          setting -> setting.rarity(Rarity.RARE)
      );
    } else {
      return register(
          name, factory, properties
              .noCollision()
              .randomTicks()
              .instabreak()
              .sound(SoundType.CROP)
              .pushReaction(PushReaction.POPPED),
          setting -> setting.rarity(Rarity.EPIC)
      );
    }
  }

  static Item registerDurableItem(
      String name,
      Function<Item.Properties, Item> factory,
      Item.Properties settings
  ) {
    return registerItem(name, factory, settings.stacksTo(1));
  }

  static <T extends AbstractContainerMenu, D> MenuType<T> registerExtended(
      String path,
      ExtendedMenuType.ExtendedFactory<T, D> factory,
      StreamCodec<? super RegistryFriendlyByteBuf, D> codec
  ) {
    return Registry.register(
        BuiltInRegistries.MENU,
        Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, path),
        new ExtendedMenuType<>(factory, codec)
    );
  }

  static void registerFuel(
      int time,
      Item fuel
  ) {
    DefaultItemComponentEvents.MODIFY.register(context -> context.modify(fuel, builder ->
        builder.set(DataComponents.COOKING_FUEL, new CookingFuel(
            new ResolvableInt.Constant(time),
            ResolvableFloat.fromKey(ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER)
        ))
    ));
  }

  static Block registerGrass(
      String name, String type,
      Function<BlockBehaviour.Properties, Block> factory,
      BlockBehaviour.Properties properties
  ) {
    if (Objects.equals(type, "supreme")) {
      return register(
          name, factory, properties
              .randomTicks()
              .strength(16.0F, 20.0F)
              .sound(SoundType.GRASS),
          setting -> setting.rarity(Rarity.RARE)
      );
    } else {
      return register(
          name, factory, properties
              .randomTicks()
              .strength(64.0F, 3600000.0F)
              .sound(SoundType.GRASS),
          setting -> setting.rarity(Rarity.EPIC)
      );
    }
  }

  static Block registerGravel(
      String name, String type,
      Function<BlockBehaviour.Properties, Block> factory,
      BlockBehaviour.Properties properties
  ) {
    if (Objects.equals(type, "supreme")) {
      return register(
          name, factory, properties
              .randomTicks()
              .strength(16.0F, 20.0F)
              .sound(SoundType.GRAVEL),
          setting -> setting.rarity(Rarity.RARE)
      );
    } else {
      return register(
          name, factory, properties
              .randomTicks()
              .strength(64.0F, 3600000.0F)
              .sound(SoundType.GRAVEL),
          setting -> setting.rarity(Rarity.EPIC)
      );
    }
  }

  static Block registerLeaves(
      String name, String type,
      Function<BlockBehaviour.Properties, Block> factory,
      BlockBehaviour.Properties properties
  ) {
    if (Objects.equals(type, "supreme")) {
      return register(
          name, factory, properties
              .strength(8.0F, 5.0F)
              .randomTicks()
              .sound(SoundType.GRASS)
              .noOcclusion()
              .isValidSpawn(Blocks::ocelotOrParrot)
              .isSuffocating(Blocks::never)
              .isViewBlocking((state, level, pos, shape) -> false)
              .ignitedByLava()
              .pushReaction(PushReaction.POPPED)
              .isRedstoneConductor(Blocks::never),
          setting -> setting.rarity(Rarity.RARE)
      );
    } else {
      return register(
          name, factory, properties
              .strength(32.0F, 3600000.0F)
              .randomTicks()
              .sound(SoundType.GRASS)
              .noOcclusion()
              .isValidSpawn(Blocks::ocelotOrParrot)
              .isSuffocating(Blocks::never)
              .isViewBlocking((state, level, pos, shape) -> false)
              .ignitedByLava()
              .pushReaction(PushReaction.POPPED)
              .isRedstoneConductor(Blocks::never),
          setting -> setting.rarity(Rarity.EPIC)
      );
    }
  }

  static Block registerPortableContainer(
      String name, String type,
      Function<BlockBehaviour.Properties, Block> factory,
      BlockBehaviour.Properties properties,
      BiFunction<Block, Item.Properties, BlockItem> itemFactory
  ) {
    boolean supreme = Objects.equals(type, "supreme");
    return register(
        name, factory,
        properties.strength(supreme ? 64.0F : 256.0F, supreme ? 2400.0F : 3600000.0F),
        setting -> setting.rarity(supreme ? Rarity.RARE : Rarity.EPIC),
        itemFactory
    );
  }

  static Block registerPortal(
      String name,
      Function<BlockBehaviour.Properties, Block> factory
  ) {
    return register(
        name,
        factory,
        BlockBehaviour.Properties.of()
            .noCollision()
            .noLootTable()
            .strength(-1.0F, -1.0F)
            .sound(SoundType.GLASS),
        setting -> setting.rarity(Rarity.COMMON)
    );
  }

  static Block registerSapling(
      String name, String type,
      Function<BlockBehaviour.Properties, Block> factory,
      BlockBehaviour.Properties properties
  ) {
    if (Objects.equals(type, "supreme")) {
      return register(
          name, factory, properties
              .strength(8.0F, 5.0F)
              .noCollision()
              .randomTicks()
              .instabreak()
              .sound(SoundType.GRASS)
              .pushReaction(PushReaction.POPPED),
          setting -> setting.rarity(Rarity.RARE)
      );
    } else {
      return register(
          name, factory, properties
              .strength(32.0F, 3600000.0F)
              .noCollision()
              .randomTicks()
              .instabreak()
              .sound(SoundType.GRASS)
              .pushReaction(PushReaction.POPPED),
          setting -> setting.rarity(Rarity.EPIC)
      );
    }
  }

  static Block registerWood(
      String name, String type,
      Function<BlockBehaviour.Properties, Block> factory,
      BlockBehaviour.Properties properties
  ) {
    if (Objects.equals(type, "supreme")) {
      return register(
          name, factory, properties
              .strength(32.0F, 400.0F)
              .sound(SoundType.WOOD)
              .ignitedByLava(),
          setting -> setting.rarity(Rarity.RARE)
      );
    } else {
      return register(
          name, factory, properties
              .strength(128.0F, 3600000.0F)
              .sound(SoundType.WOOD)
              .ignitedByLava(),
          setting -> setting.rarity(Rarity.EPIC)
      );
    }
  }

  static Block registerWoodBlockOnly(
      String name, String type,
      Function<BlockBehaviour.Properties, Block> factory,
      BlockBehaviour.Properties properties
  ) {
    Identifier id = Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, name);
    ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
    return Blocks.register(
        key, factory,
        properties.strength(Objects.equals(type, "supreme") ? 32.0F : 128.0F,
                Objects.equals(type, "supreme") ? 400.0F : 3600000.0F)
            .sound(SoundType.WOOD).ignitedByLava().requiresCorrectToolForDrops()
    );
  }

  static Block registerWoodDoor(
      String name, String type,
      Function<BlockBehaviour.Properties, Block> factory,
      BlockBehaviour.Properties properties
  ) {
    Identifier id = Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, name);
    ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
    ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
    Block block = Blocks.register(
        blockKey, factory,
        properties.strength(Objects.equals(type, "supreme") ? 32.0F : 128.0F,
                Objects.equals(type, "supreme") ? 400.0F : 3600000.0F)
            .sound(SoundType.WOOD).ignitedByLava().requiresCorrectToolForDrops()
    );
    BlockItem item = new DoubleHighBlockItem(
        block, new Item.Properties().setId(itemKey)
            .rarity(Objects.equals(type, "supreme") ? Rarity.RARE : Rarity.EPIC)
    );
    item.registerBlocks(Item.BY_BLOCK, item);
    Registry.register(BuiltInRegistries.ITEM, itemKey, item);
    return block;
  }

  static Item registerWoodSignItem(
      String name, String type,
      Block standing, Block wall,
      boolean hanging
  ) {
    Identifier id = Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, name);
    ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
    Item.Properties properties = new Item.Properties().setId(key)
        .rarity(Objects.equals(type, "supreme") ? Rarity.RARE : Rarity.EPIC);
    BlockItem item = hanging
        ? new HangingSignItem(standing, wall, properties)
        : new StandingAndWallBlockItem(standing, wall, Direction.DOWN, properties);
    item.registerBlocks(Item.BY_BLOCK, item);
    Registry.register(BuiltInRegistries.ITEM, key, item);
    return item;
  }

  private static Block register(
      String name,
      Function<BlockBehaviour.Properties, Block> factory,
      BlockBehaviour.Properties blockSettings,
      UnaryOperator<Item.Properties> itemModifier
  ) {
    return register(name, factory, blockSettings.requiresCorrectToolForDrops(), itemModifier, BlockItem::new);
  }

  private static Block register(
      String name,
      Function<BlockBehaviour.Properties, Block> factory,
      BlockBehaviour.Properties blockSettings,
      UnaryOperator<Item.Properties> itemModifier,
      BiFunction<Block, Item.Properties, BlockItem> itemFactory
  ) {
    Identifier id = Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, name);
    ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
    ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

    Block block = Blocks.register(blockKey, factory, blockSettings);
    Item.Properties itemProperties = itemModifier.apply(new Item.Properties().setId(itemKey));

    BlockItem blockItem = itemFactory.apply(block, itemProperties);
    blockItem.registerBlocks(Item.BY_BLOCK, blockItem);
    Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

    return block;
  }

  private static Item registerItem(
      String name,
      Function<Item.Properties, Item> factory,
      Item.Properties settings
  ) {
    ResourceKey<Item> key = ResourceKey.create(
        Registries.ITEM,
        Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, name)
    );
    Item item = factory.apply(settings.setId(key));
    if (item instanceof BlockItem blockItem) blockItem.registerBlocks(Item.BY_BLOCK, item);
    return Registry.register(BuiltInRegistries.ITEM, key, item);
  }

}
