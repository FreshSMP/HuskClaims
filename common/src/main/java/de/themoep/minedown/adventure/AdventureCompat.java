package de.themoep.minedown.adventure;

/*
 * Copyright (c) 2020 Max Lee (https://github.com/Phoenix616)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextFormat;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;

import java.util.UUID;

/**
 * Holds every call into parts of the Adventure API that only exist on newer versions of it.
 * <p>
 * HuskClaims runs on Minecraft 1.17 all the way up to 26.2, which means the Adventure version provided by the
 * server (or bundled by the adventure platform on legacy servers) can be anything from 4.9 to 5.x. Keeping these
 * calls out of {@link MineDownParser} means the parser never resolves classes the running Adventure version
 * doesn't have; this class is only ever loaded when a message actually uses one of the newer features.
 *
 * @see Util#createClickEvent(net.kyori.adventure.text.event.ClickEvent.Action, String, net.kyori.adventure.nbt.api.BinaryTagHolder)
 */
final class AdventureCompat {

    /**
     * Whether the running Adventure version supports shadow colors (Adventure 4.18+, Minecraft 1.21.4+)
     */
    static final boolean SHADOW_SUPPORTED = Util.hasClass("net.kyori.adventure.text.format.ShadowColor");

    /**
     * Whether the running Adventure version supports object components - player heads, sprites and atlases
     * (Adventure 4.25+ / 5.x, Minecraft 1.21.9+)
     */
    static final boolean OBJECTS_SUPPORTED = Util.hasClass("net.kyori.adventure.text.object.ObjectContents");

    private AdventureCompat() {
    }

    /**
     * Create a shadow color from a text color
     *
     * @param color The color to base the shadow on
     * @param alpha The alpha value of the shadow
     * @return The shadow color, or <code>null</code> if this Adventure version has no shadow color support
     */
    static Object shadowColor(TextFormat color, int alpha) {
        if (!SHADOW_SUPPORTED) {
            return null;
        }
        return ShadowColor.shadowColor((TextColor) color, alpha);
    }

    /**
     * Parse a shadow color from a hex string
     *
     * @param hexString The hex string to parse
     * @return The shadow color, or <code>null</code> if it could not be parsed or isn't supported
     */
    static Object shadowFromHexString(String hexString) {
        if (!SHADOW_SUPPORTED) {
            return null;
        }
        return ShadowColor.fromHexString(hexString);
    }

    /**
     * Apply a shadow color to a component builder
     *
     * @param builder The builder to apply the shadow to
     * @param shadow  The shadow color, as returned by one of the methods in this class
     */
    static void applyShadow(ComponentBuilder<?, ?> builder, Object shadow) {
        if (!SHADOW_SUPPORTED || !(shadow instanceof ShadowColor)) {
            return;
        }
        builder.shadowColor((ShadowColor) shadow);
    }

    /**
     * Create a new player head object contents builder
     *
     * @return The builder
     * @throws IllegalArgumentException If this Adventure version has no object component support
     */
    static Object playerHead() {
        requireObjects("player_head");
        return ObjectContents.playerHead();
    }

    /**
     * Set the player uuid of a player head
     *
     * @param playerHead The player head builder
     * @param id         The uuid of the player
     */
    static void playerHeadId(Object playerHead, UUID id) {
        ((PlayerHeadObjectContents.Builder) playerHead).id(id);
    }

    /**
     * Set the texture key of a player head
     *
     * @param playerHead The player head builder
     * @param texture    The texture key
     */
    static void playerHeadTexture(Object playerHead, Key texture) {
        ((PlayerHeadObjectContents.Builder) playerHead).texture(texture);
    }

    /**
     * Set the player name of a player head
     *
     * @param playerHead The player head builder
     * @param name       The name of the player
     */
    static void playerHeadName(Object playerHead, String name) {
        ((PlayerHeadObjectContents.Builder) playerHead).name(name);
    }

    /**
     * Set whether a player head should be rendered with its hat layer
     *
     * @param playerHead The player head builder
     * @param hat        Whether to render the hat layer
     */
    static void playerHeadHat(Object playerHead, boolean hat) {
        ((PlayerHeadObjectContents.Builder) playerHead).hat(hat);
    }

    /**
     * Add a profile property to a player head
     *
     * @param playerHead The player head builder
     * @param name       The name of the property
     * @param value      The value of the property
     * @param signature  The signature of the property, may be null
     */
    static void playerHeadProperty(Object playerHead, String name, String value, String signature) {
        final PlayerHeadObjectContents.Builder builder = (PlayerHeadObjectContents.Builder) playerHead;
        builder.profileProperty(signature != null
                ? PlayerHeadObjectContents.property(name, value, signature)
                : PlayerHeadObjectContents.property(name, value));
    }

    /**
     * Build a player head object component
     *
     * @param playerHead The player head builder
     * @return The built component
     */
    static Component playerHeadComponent(Object playerHead) {
        requireObjects("player_head");
        return Component.object(((PlayerHeadObjectContents.Builder) playerHead).build());
    }

    /**
     * Build a sprite object component
     *
     * @param atlas  The atlas the sprite is from, may be null
     * @param sprite The sprite key
     * @return The built component
     */
    static Component spriteComponent(Key atlas, Key sprite) {
        requireObjects("sprite");
        if (atlas != null) {
            return Component.object(ObjectContents.sprite(atlas, sprite));
        }
        if (sprite.value().startsWith("item/")) {
            return Component.object(ObjectContents.sprite(Key.key(sprite.namespace(), "items"), sprite));
        }
        return Component.object(ObjectContents.sprite(sprite));
    }

    private static void requireObjects(String feature) {
        if (!OBJECTS_SUPPORTED) {
            throw new IllegalArgumentException(feature + " requires Adventure 4.25 or newer (Minecraft 1.21.9+)!");
        }
    }

}
