/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.server;

// Java Standard
import java.util.Map;
import java.util.concurrent.Executor;

// Minecraft
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface UniverseAnnihilationServerAccessor {

  @Accessor("executor") Executor magnatour$getExecutor();

  @Accessor("levels") Map<ResourceKey<Level>, ServerLevel> magnatour$getLevels();

  @Accessor("storageSource") LevelStorageSource.LevelStorageAccess magnatour$getStorage();

}
