/*
 * This file is part of HuskClaims, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskclaims.util;

import io.papermc.lib.PaperLib;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.CompletableFuture;

/**
 * Async chunk loading and teleportation, using the server's own API where it provides one.
 * <p>
 * PaperLib picks its implementations by parsing the Minecraft version out of {@link org.bukkit.Bukkit#getVersion()}
 * with a pattern that only matches the old {@code 1.x.y} scheme. On Minecraft 26.2 that parse fails, PaperLib decides
 * the server is ancient and falls back to loading chunks and teleporting on the calling thread - which, on Folia-based
 * servers, trips a {@code TickThread} check and fails the command outright.
 * <p>
 * Looking the methods up directly means the modern paths are used whenever the server actually has them, no matter
 * how it happens to name its version; PaperLib remains the fallback for servers that don't.
 */
public final class PaperApiCompat {

    private static final MethodHandle GET_CHUNK_AT_ASYNC = findMethod(
            World.class, "getChunkAtAsync", CompletableFuture.class, int.class, int.class
    );
    private static final MethodHandle TELEPORT_ASYNC = findMethod(
            Entity.class, "teleportAsync", CompletableFuture.class, Location.class
    );

    private PaperApiCompat() {
    }

    /**
     * Load the chunk at a location, off the main thread where the server supports it.
     * <p>
     * The returned future is completed on the thread that owns the chunk, so it is safe to touch the chunk in a
     * callback chained onto it.
     *
     * @param location the location to load the chunk at
     * @return a future that completes with the loaded chunk
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static CompletableFuture<Chunk> getChunkAtAsync(@NotNull Location location) {
        final World world = location.getWorld();
        if (GET_CHUNK_AT_ASYNC == null || world == null) {
            return PaperLib.getChunkAtAsync(location);
        }
        try {
            return (CompletableFuture<Chunk>) GET_CHUNK_AT_ASYNC.invoke(
                    world, location.getBlockX() >> 4, location.getBlockZ() >> 4
            );
        } catch (Throwable e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Teleport an entity, asynchronously where the server supports it.
     * <p>
     * Must be called from the thread that owns the entity; on Folia-based servers a plain teleport cannot move an
     * entity between regions, so the async API is the only way to do this.
     *
     * @param entity   the entity to teleport
     * @param location where to teleport it to
     */
    public static void teleportAsync(@NotNull Entity entity, @NotNull Location location) {
        if (TELEPORT_ASYNC == null) {
            PaperLib.teleportAsync(entity, location);
            return;
        }
        try {
            final Object ignored = TELEPORT_ASYNC.invoke(entity, location);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to teleport %s".formatted(entity.getName()), e);
        }
    }

    private static MethodHandle findMethod(@NotNull Class<?> owner, @NotNull String name,
                                           @NotNull Class<?> returnType, @NotNull Class<?>... parameters) {
        try {
            return MethodHandles.publicLookup().findVirtual(owner, name, MethodType.methodType(returnType, parameters));
        } catch (NoSuchMethodException | IllegalAccessException | RuntimeException ignored) {
            return null;
        }
    }

}
