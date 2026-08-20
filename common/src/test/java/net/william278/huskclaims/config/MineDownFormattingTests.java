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

package net.william278.huskclaims.config;

import de.exlll.configlib.YamlConfigurations;
import de.themoep.minedown.adventure.MineDown;
import de.themoep.minedown.adventure.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the MineDown formatting of locales.
 * <p>
 * Minecraft 26.2 ships Adventure 5, which replaced the {@code ClickEvent.Action} enum with typed action classes;
 * anything reading it as an enum (as MineDown used to) blows up with a {@link NoSuchMethodError} the moment a
 * message with a click event is sent.
 */
@DisplayName("MineDown Formatting Tests")
public class MineDownFormattingTests {

    @ParameterizedTest(name = "{1} Locales")
    @DisplayName("Test All Locales Format")
    @MethodSource("provideLocaleFiles")
    public void testAllLocalesFormat(@NotNull File file, @SuppressWarnings("unused") @NotNull String keyName) {
        final Locales locales = YamlConfigurations.load(file.toPath(), Locales.class);
        assertFalse(locales.locales.isEmpty(), "No locales were loaded from " + file.getName());
        locales.locales.keySet().forEach(key -> assertDoesNotThrow(
                () -> locales.getLocale(key).orElseThrow().toComponent(),
                "Locale %s in %s could not be formatted".formatted(key, file.getName())
        ));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("Test Click Event Actions")
    @CsvSource({
            "run_command=/huskclaims about, run_command, /huskclaims about",
            "suggest_command=/claim 10, suggest_command, /claim 10",
            "open_url=https://william278.net, open_url, https://william278.net",
            "open_file=/home/server/logs/latest.log, open_file, /home/server/logs/latest.log",
            "copy_to_clipboard=Some text, copy_to_clipboard, Some text",
            "change_page=4, change_page, 4"
    })
    public void testClickEventActions(@NotNull String definition, @NotNull String action, @NotNull String value) {
        final Component component = new MineDown("[Click me](%s)".formatted(definition)).toComponent();
        final ClickEvent clickEvent = component.clickEvent();
        assertNotNull(clickEvent, "No click event was created for " + definition);
        assertEquals(action, Util.actionName(clickEvent.action()));
        assertEquals(value, Util.clickEventValue(clickEvent));
    }

    @DisplayName("Test Command Click Event Shorthand")
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "/claim, run_command, /claim",
            "https://william278.net, open_url, https://william278.net"
    })
    public void testClickEventShorthand(@NotNull String definition, @NotNull String action, @NotNull String value) {
        final Component component = new MineDown("[Click me](%s)".formatted(definition)).toComponent();
        final ClickEvent clickEvent = component.clickEvent();
        assertNotNull(clickEvent, "No click event was created for " + definition);
        assertEquals(action, Util.actionName(clickEvent.action()));
        assertEquals(value, Util.clickEventValue(clickEvent));
    }

    @DisplayName("Test Click Event Replacements")
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "run_command=/group %name%, /group William",
            "suggest_command=/untrust %name%, /untrust William"
    })
    public void testClickEventReplacements(@NotNull String definition, @NotNull String value) {
        final Component component = new MineDown("[Click me](%s)".formatted(definition))
                .replace("name", "William").toComponent();
        final ClickEvent clickEvent = component.clickEvent();
        assertNotNull(clickEvent, "No click event was created for " + definition);
        assertEquals(value, Util.clickEventValue(clickEvent));
    }

    @NotNull
    private static Stream<Arguments> provideLocaleFiles() {
        final URL url = MineDownFormattingTests.class.getClassLoader().getResource("locales");
        assertNotNull(url, "locales folder is missing");

        return Stream.of(Objects.requireNonNull(new File(url.getPath()).listFiles(
                file -> file.getName().endsWith("yml")
        ))).map(file -> Arguments.of(file, file.getName().replace(".yml", "")));
    }

}
