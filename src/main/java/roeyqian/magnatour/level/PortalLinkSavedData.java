/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.level;

// Java Standard
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Mojang
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

// Magnatour
import roeyqian.magnatour.Magnatour;

/** Persistent, bidirectional links between endpoints of custom horizontal portals. */
public class PortalLinkSavedData extends SavedData {

  private static final Codec<PortalLinkSavedData> CODEC = RecordCodecBuilder.create(instance ->
      instance.group(
          PortalLink.CODEC.listOf().fieldOf("links").forGetter(PortalLinkSavedData::links)
      ).apply(instance, PortalLinkSavedData::new)
  );

  private static final SavedDataType<PortalLinkSavedData> TYPE = new SavedDataType<>(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "portal_links"),
      PortalLinkSavedData::new,
      CODEC,
      DataFixTypes.LEVEL
  );

  private final Map<Endpoint, Endpoint> links = new HashMap<>();

  public PortalLinkSavedData() {}

  private PortalLinkSavedData(
      List<PortalLink> links
  ) {
    for (PortalLink link : links) {
      this.links.put(link.source(), link.destination());
    }
  }

  public static PortalLinkSavedData get(
      MinecraftServer server
  ) {
    return server.overworld().getDataStorage().computeIfAbsent(TYPE);
  }

  public Optional<Endpoint> getDestination(
      Endpoint source
  ) {
    return Optional.ofNullable(this.links.get(source));
  }

  public void link(
      Endpoint first,
      Endpoint second
  ) {
    this.unlink(first);
    this.unlink(second);
    this.links.put(first, second);
    this.links.put(second, first);
    this.setDirty();
  }

  public void unlink(
      Endpoint endpoint
  ) {
    boolean removed = this.links.remove(endpoint) != null;
    removed |= this.links.entrySet().removeIf(entry -> entry.getValue().equals(endpoint));
    if (removed) this.setDirty();
  }

  private List<PortalLink> links() {
    List<PortalLink> savedLinks = new ArrayList<>();
    this.links.forEach((source, destination) -> savedLinks.add(new PortalLink(source, destination)));
    return savedLinks;
  }

  public record Endpoint(
      ResourceKey<Level> dimension,
      BlockPos pos
  ) {

    private static final Codec<Endpoint> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(Endpoint::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(Endpoint::pos)
        ).apply(instance, Endpoint::new)
    );

  }

  private record PortalLink(
      Endpoint source,
      Endpoint destination
  ) {

    private static final Codec<PortalLink> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Endpoint.CODEC.fieldOf("source").forGetter(PortalLink::source),
            Endpoint.CODEC.fieldOf("destination").forGetter(PortalLink::destination)
        ).apply(instance, PortalLink::new)
    );

  }

}
