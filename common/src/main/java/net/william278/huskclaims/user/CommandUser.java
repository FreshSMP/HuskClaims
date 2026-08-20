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

package net.william278.huskclaims.user;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface CommandUser {

    boolean isOnCooldown(float cooldownTime);

    @NotNull
    Audience getAudience();

    boolean hasPermission(@NotNull String permission, boolean isDefault);

    boolean hasPermission(@NotNull String permission);

    default void sendMessage(@NotNull Component component) {
        getAudience().sendMessage(component);
    }

    default void sendMessage(@NotNull MineDown mineDown) {
        this.sendMessage(format(mineDown));
    }

    /**
     * Format a {@link MineDown} message into a {@link Component}, falling back to the plain, un-formatted message
     * if it couldn't be parsed - a badly formatted locale, or one using a formatting feature the server's Adventure
     * version doesn't support, should never break the command a user is running.
     *
     * @param mineDown the message to format
     * @return the formatted component
     */
    @NotNull
    static Component format(@NotNull MineDown mineDown) {
        try {
            return mineDown.toComponent();
        } catch (Throwable e) {
            return Component.text(mineDown.message());
        }
    }

}
