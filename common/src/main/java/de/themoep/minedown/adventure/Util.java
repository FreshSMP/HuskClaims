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
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.BuildableComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextFormat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Util {

    private static final Pattern WRAP_PATTERN = Pattern.compile(" ", Pattern.LITERAL);

    /**
     * Utility method to throw an IllegalArgumentException if the value is false
     * @param value   The value to validate
     * @param message The message for the exception
     * @throws IllegalArgumentException Thrown if the value is false
     */
    public static void validate(boolean value, String message) throws IllegalArgumentException {
        if (!value) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Apply a collection of colors/formats to a component
     * @param component The BaseComponent
     * @param formats   The collection of TextColor formats to apply
     * @return The component that was modified
     */
    public static Component applyFormat(Component component, Collection<TextDecoration> formats) {
        for (TextDecoration format : formats) {
            component.decoration(format, true);
        }
        if (!component.children().isEmpty()) {
            for (Component extra : component.children()) {
                applyFormat(extra, formats);
            }
        }
        return component;
    }

    /**
     * Apply a collection of colors/formats to a component builder
     * @param builder The ComponentBuilder
     * @param formats The collection of TextColor formats to apply
     * @return The component builder that was modified
     * @deprecated Use {@link #applyFormat(ComponentBuilder, Map)}
     */
    @Deprecated
    public static ComponentBuilder applyFormat(ComponentBuilder builder, Collection<TextDecoration> formats) {
        for (TextDecoration format : formats) {
            builder.decoration(format, true);
        }
        return builder;
    }

    /**
     * Apply a collection of colors/formats to a component builder
     * @param builder The ComponentBuilder
     * @param formats The collection of TextColor formats to apply
     * @return The component builder that was modified
     */
    public static ComponentBuilder applyFormat(ComponentBuilder builder, Map<TextDecoration, Boolean> formats) {
        for (Map.Entry<TextDecoration, Boolean> e : formats.entrySet()) {
            builder.decoration(e.getKey(), e.getValue());
        }
        return builder;
    }

    /**
     * Check whether or not a character at a certain index of a string repeats itself
     * @param string The string to check
     * @param index  The index at which to check the character
     * @return Whether or not the character at that index repeated itself
     */
    public static boolean isDouble(String string, int index) {
        return index + 1 < string.length() && string.charAt(index) == string.charAt(index + 1);
    }

    /**
     * Check whether a certain TextColor is formatting or not
     * @param format The TextColor to check
     * @return <code>true</code> if it's a format, <code>false</code> if it's a color
     */
    public static boolean isFormat(TextColor format) {
        return false;
    }

    /**
     * Get a set of TextColor formats all formats that a component includes
     * @param component    The component to get the formats from
     * @param ignoreParent Whether or not to include the parent's format (TODO: Does kyori-text not handle this?)
     * @return A set of all the format TextColors that the component includes
     */
    public static Set<TextDecoration> getFormats(Component component, boolean ignoreParent) {
        return component.decorations().entrySet().stream()
                .filter(e -> e.getValue() == TextDecoration.State.TRUE)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Get the index of the first occurrences of a not escaped character
     * @param string The string to search
     * @param chars  The characters to search for
     * @return The first unescaped index or -1 if not found
     */
    public static int indexOfNotEscaped(String string, String chars) {
        return indexOfNotEscaped(string, chars, 0);
    }

    /**
     * Get the index of the first occurrences of a not escaped character
     * @param string    The string to search
     * @param chars     The characters to search for
     * @param fromIndex Start searching from that index
     * @return The first unescaped index or {@code -1} if not found
     */
    public static int indexOfNotEscaped(String string, String chars, int fromIndex) {
        for (int i = fromIndex; i < string.length(); i++) {
            int index = string.indexOf(chars, i);
            if (index == -1) {
                return -1;
            }
            if (!isEscaped(string, index)) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Check if a character at a certain index is escaped
     * @param string The string to check
     * @param index  The index of the character in the string to check
     * @return Whether or not the character is escaped (uneven number of backslashes in front of char mains it is escaped)
     * @throws IndexOutOfBoundsException if the {@code index} argument is not less than the length of this string.
     */
    public static boolean isEscaped(String string, int index) {
        if (index - 1 > string.length()) {
            return false;
        }
        int e = 0;
        while (index > e && string.charAt(index - e - 1) == '\\') {
            e++;
        }
        return e % 2 != 0;
    }

    /**
     * Gets the proper end index of a certain definition on the same depth while ignoring escaped chars.
     * @param string    The string to search
     * @param startChar The start cahracter of the definition
     * @param endChar   The end character of the definition
     * @param fromIndex The index to start searching from (should be at the start char)
     * @return The first end index of that group  or {@code -1} if not found
     */
    public static int getUnescapedEndIndex(String string, char startChar, char endChar, int fromIndex) {
        int depth = 0;
        boolean innerEscaped = false;
        for (int i = fromIndex; i < string.length(); i++) {
            if (innerEscaped) {
                innerEscaped = false;
            } else if (string.charAt(i) == '\\') {
                innerEscaped = true;
            } else if (string.charAt(i) == startChar) {
                depth++;
            } else if (string.charAt(i) == endChar) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Wrap a string if it is longer than the line length and contains no new line.
     * Will try to wrap at spaces between words.
     * @param string        The string to wrap
     * @param lineLength    The max length of a line
     * @return The wrapped string
     */
    public static String wrap(String string, int lineLength) {
        if (string.length() <= lineLength || string.contains("\n")) {
            return string;
        }

        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        for (String s : WRAP_PATTERN.split(string)) {
            if (currentLine.length() + s.length() + 1 > lineLength) {
                int rest = lineLength - currentLine.length() - 1;
                if (rest > lineLength / 4 && s.length() > Math.min(rest * 2, lineLength / 4)) {
                    currentLine.append(" ").append(s, 0, rest);
                } else {
                    rest = 0;
                }
                lines.add(currentLine.toString());
                String restString = s.substring(rest);
                while (restString.length() >= lineLength) {
                    lines.add(restString.substring(0, lineLength));
                    restString = restString.substring(lineLength);
                }
                currentLine = new StringBuilder(restString);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(s);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        return String.join("\n", lines);
    }

    /**
     * Utility method to remove RGB colors from components. This modifies the input array!
     * @param components    The components to remove the rgb colors from
     * @return The modified components (same as input).
     */
    public static Component rgbColorsToLegacy(Component components) {
        return Component.text().append(components).mapChildrenDeep(buildableComponent
                -> buildableComponent.color() != null
                        ? (BuildableComponent) buildableComponent.color(NamedTextColor.nearestTo(buildableComponent.color()))
                        : buildableComponent
        ).build();
    }

    /**
     * Get the legacy color closest to a certain RGB color
     * @param textColor The color to get the closest legacy color for
     * @return The closest legacy color
     * @deprecated Use {@link NamedTextColor#nearestTo(TextColor)}
     */
    @Deprecated
    public static NamedTextColor getClosestLegacy(TextColor textColor) {
        return textColor != null ? NamedTextColor.nearestTo(textColor) : null;
    }

    /**
     * Get the distance between two colors
     * @param c1 Color A
     * @param c2 Color B
     * @return The distance or 0 if they are equal
     * @deprecated Doesn't use perceived brightness (HSV) but simply takes the distance between RGB. Do not rely on this, I twill look ugly!
     */
    @Deprecated
    public static double distance(Color c1, Color c2) {
        if (c1.getRGB() == c2.getRGB()) {
            return 0;
        }
        return Math.sqrt(Math.pow(c1.getRed() - c2.getRed(), 2) + Math.pow(c1.getGreen() - c2.getGreen(), 2) + Math.pow(c1.getBlue() - c2.getBlue(), 2));
    }

    /**
     * Get the text format from a string, either its name or hex code
     * @param formatString The string to get the format from
     * @return The TextFormat
     * @throws IllegalArgumentException if the format could not be found from the string
     */
    public static TextFormat getFormatFromString(String formatString) throws IllegalArgumentException {
        TextFormat format;
        if (formatString.charAt(0) == '#') {
            format = TextColor.fromCSSHexString(formatString);
        } else {
            format = NamedTextColor.NAMES.value(formatString.toLowerCase(Locale.ROOT));
            if (format == null) {
                format = TextDecoration.NAMES.value(formatString.toLowerCase(Locale.ROOT));
            }
            if (format == null) {
                // Handle legacy formatting names
                switch (formatString.toLowerCase(Locale.ROOT)) {
                    case "underline":
                        return TextDecoration.UNDERLINED;
                    case "magic":
                        return TextDecoration.OBFUSCATED;
                }
            }
        }

        if (format != null) {
            return format;
        }
        throw new IllegalArgumentException("Unknown format: " + formatString);
    }

    /**
     * Get a TextFormat from its legacy color code as kyori-text-api does not support that
     * @param code  The legacy char
     * @return      The TextFormat or null if none found with that char
     */
    public static Object getFormatFromLegacy(char code) {
        switch (code) {
            case '0': return NamedTextColor.BLACK;
            case '1': return NamedTextColor.DARK_BLUE;
            case '2': return NamedTextColor.DARK_GREEN;
            case '3': return NamedTextColor.DARK_AQUA;
            case '4': return NamedTextColor.DARK_RED;
            case '5': return NamedTextColor.DARK_PURPLE;
            case '6': return NamedTextColor.GOLD;
            case '7': return NamedTextColor.GRAY;
            case '8': return NamedTextColor.DARK_GRAY;
            case '9': return NamedTextColor.BLUE;
            case 'a': return NamedTextColor.GREEN;
            case 'b': return NamedTextColor.AQUA;
            case 'c': return NamedTextColor.RED;
            case 'd': return NamedTextColor.LIGHT_PURPLE;
            case 'e': return NamedTextColor.YELLOW;
            case 'f': return NamedTextColor.WHITE;
            case 'k': return TextDecoration.OBFUSCATED;
            case 'l': return TextDecoration.BOLD;
            case 'm': return TextDecoration.STRIKETHROUGH;
            case 'n': return TextDecoration.UNDERLINED;
            case 'o': return TextDecoration.ITALIC;
            case 'r': return TextControl.RESET;
        }
        return null;
    }

    /**
     * Get the legacy color code from its format as kyori-text-api does not support that
     * @param format    The format
     * @return          The legacy color code or null if none found with that char
     */
    public static char getLegacyFormatChar(Object format) {
        if (format == TextControl.RESET) {
            return 'r';
        } else if (format instanceof NamedTextColor) {
                if (format == NamedTextColor.BLACK) return '0';
                if (format == NamedTextColor.DARK_BLUE) return '1';
                if (format == NamedTextColor.DARK_GREEN) return '2';
                if (format == NamedTextColor.DARK_AQUA) return '3';
                if (format == NamedTextColor.DARK_RED) return '4';
                if (format == NamedTextColor.DARK_PURPLE) return '5';
                if (format == NamedTextColor.GOLD) return '6';
                if (format == NamedTextColor.GRAY) return '7';
                if (format == NamedTextColor.DARK_GRAY) return '8';
                if (format == NamedTextColor.BLUE) return '9';
                if (format == NamedTextColor.GREEN) return 'a';
                if (format == NamedTextColor.AQUA) return 'b';
                if (format == NamedTextColor.RED) return 'c';
                if (format == NamedTextColor.LIGHT_PURPLE) return 'd';
                if (format == NamedTextColor.YELLOW) return 'e';
                if (format == NamedTextColor.WHITE) return 'f';
        } else if (format instanceof TextDecoration) {
            switch ((TextDecoration) format) {
                case OBFUSCATED: return 'k';
                case BOLD: return 'l';
                case STRIKETHROUGH: return 'm';
                case UNDERLINED: return 'n';
                case ITALIC: return 'o';
            }
        } else if (format instanceof TextColor) {
            return getLegacyFormatChar(NamedTextColor.nearestTo((TextColor) format));
        }
        throw new IllegalArgumentException(format + " is not supported!");
    }    /*
     * createRainbow is adapted from the net.kyori.adventure.text.minimessage.fancy.Rainbow class
     * in adventure-text-minimessage, licensed under the MIT License.
     *
     * Copyright (c) 2018-2020 KyoriPowered
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
    /**
     * Generate a rainbow with a certain length and phase
     * @param length    The length of the rainbow
     * @param phase     The phase of the rainbow.
     * @return the colors in the rainbow
     * @deprecated Use {@link #createRainbow(long, int)}
     */
    @Deprecated
    public static List<TextColor> createRainbow(int length, int phase) {
        return createRainbow((long) length, phase);
    }

    /**
     * Generate a rainbow with a certain length and phase
     * @param length    The length of the rainbow
     * @param phase     The phase of the rainbow.
     * @return the colors in the rainbow
     */
    public static List<TextColor> createRainbow(long length, int phase) {
        List<TextColor> colors = new ArrayList<>();

        float fPhase = phase / 10f;

        float center = 128;
        float width = 127;
        double frequency = Math.PI * 2 / length;

        for (long i = 0; i < length; i++) {
            colors.add(TextColor.color(
                    (int) (Math.sin(frequency * i + 2 + fPhase) * width + center),
                    (int) (Math.sin(frequency * i + 0 + fPhase) * width + center),
                    (int) (Math.sin(frequency * i + 4 + fPhase) * width + center)
            ));
    }
        return colors;
    }

    /*
     * createGradient is adapted from the net.kyori.adventure.text.minimessage.fancy.Gradient class
     * in adventure-text-minimessage, licensed under the MIT License.
     *
     * Copyright (c) 2018-2020 KyoriPowered
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
    /**
     * Generate a gradient with certain colors
     * @param length    The length of the gradient
     * @param gradient    The colors of the gradient.
     * @return the colors in the gradient
     * @deprecated Use {@link #createGradient(long, List)}
     */
    @Deprecated
    public static List<TextColor> createGradient(int length, List<TextColor> gradient) {
        return createGradient((long) length, gradient);
    }

    /**
     * Generate a gradient with certain colors
     * @param length    The length of the gradient
     * @param gradient    The colors of the gradient.
     * @return the colors in the gradient
     */
    public static List<TextColor> createGradient(long length, List<TextColor> gradient) {
        List<TextColor> colors = new ArrayList<>();
        if (gradient.size() < 2 || length < 2) {
            if (gradient.isEmpty()) {
                return gradient;
            }
            return Collections.singletonList(gradient.get(0));
        }

        float fPhase = 0;

        float sectorLength = (float) (length - 1) / (gradient.size() - 1);
        float factorStep = 1.0f / (sectorLength);

        long index = 0;

        int colorIndex = 0;

        for (long i = 0; i < length; i++) {

            if (factorStep * index > 1) {
                colorIndex++;
                index = 0;
            }

            float factor = factorStep * (index++ + fPhase);
            // loop around if needed
            if (factor > 1) {
                factor = 1 - (factor - 1);
            }

            colors.add(TextColor.lerp(
                    factor,
                    gradient.get(colorIndex),
                    gradient.get(Math.min(gradient.size() - 1, colorIndex + 1))
            ));
        }

        return colors;
    }

    public enum TextControl {
        RESET('r');

        private char c;

        TextControl(char c) {
            this.c = c;
        }

        public char getChar() {
            return c;
        }
    }

    /**
     * Create a click event for an action and its (string) value.
     * <p>
     * Adventure 5 replaced the {@link ClickEvent.Action} enum with a set of typed action classes and removed both
     * {@code ClickEvent.Action#valueOf(String)} and {@code ClickEvent#clickEvent(Action, String)}. The action-specific
     * factory methods used here exist - with identical descriptors - on every Adventure version, so this works on
     * Adventure 4 (Minecraft 1.17 - 1.21.x) and Adventure 5 (Minecraft 26.2+) alike.
     *
     * @param action  The action to create the click event for
     * @param value   The value of the click event
     * @param payload Optional binary tag payload, only used by the {@code custom} action
     * @return The created click event
     * @throws IllegalArgumentException if the action is not supported or the value is invalid for it
     */
    public static ClickEvent createClickEvent(ClickEvent.Action action, String value, BinaryTagHolder payload) {
        switch (actionName(action)) {
            case "open_url":
                return ClickEvent.openUrl(value);
            case "open_file":
                return ClickEvent.openFile(value);
            case "run_command":
                return ClickEvent.runCommand(value);
            case "suggest_command":
                return ClickEvent.suggestCommand(value);
            case "copy_to_clipboard":
                return ClickEvent.copyToClipboard(value);
            case "change_page":
                try {
                    return ClickEvent.changePage(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(value + " is not a valid page number!", e);
                }
            case "custom":
                return createCustomClickEvent(value, payload);
            default:
                throw new IllegalArgumentException("Click event action " + actionName(action) + " is not supported!");
        }
    }

    /**
     * Create a {@code custom} click event. Requires Adventure 4.22 or newer, older versions do not know that action.
     *
     * @param key     The key of the custom action
     * @param payload The binary tag payload of the action, may be null
     * @return The created click event
     */
    private static ClickEvent createCustomClickEvent(String key, BinaryTagHolder payload) {
        try {
            return ClickEvent.custom(Key.key(key), payload != null ? payload : BinaryTagHolder.binaryTagHolder(""));
        } catch (NoSuchMethodError | NoClassDefFoundError e) {
            throw new IllegalArgumentException("Custom click events require Adventure 4.22 or newer!", e);
        }
    }

    /**
     * Get the (serialized, lower case) name of a click event action in a way that works on both Adventure 4,
     * where actions are enum constants, and Adventure 5, where they are individual classes.
     *
     * @param action The action to get the name of
     * @return The name of the action, e.g. {@code run_command}
     */
    public static String actionName(ClickEvent.Action action) {
        String name = ClickEvent.Action.NAMES.key(action);
        return (name != null ? name : action.toString()).toLowerCase(Locale.ROOT);
    }

    /**
     * Read the string value of a click event.
     * <p>
     * Adventure 4 exposes it via {@code ClickEvent#value()}, Adventure 4.22 added typed payloads and Adventure 5
     * removed {@code value()} entirely. Both are read reflectively so that this class never resolves a payload type
     * on an Adventure version that doesn't have one.
     *
     * @param event The click event to read the value of
     * @return The value of the click event, or <code>null</code> if it could not be read
     */
    public static String clickEventValue(ClickEvent event) {
        if (CLICK_EVENT_VALUE != null) {
            try {
                return (String) CLICK_EVENT_VALUE.invoke(event);
            } catch (ReflectiveOperationException | ClassCastException ignored) {
            }
        }
        final Object payload = clickEventPayload(event);
        if (payload == null) {
            return null;
        }
        Object value = invoke(payload, "value"); // ClickEvent.Payload.Text
        if (value instanceof String) {
            return (String) value;
        }
        value = invoke(payload, "integer"); // ClickEvent.Payload.Int
        if (value != null) {
            return String.valueOf(value);
        }
        value = invoke(payload, "key"); // ClickEvent.Payload.Custom
        if (value instanceof Key) {
            return ((Key) value).asString();
        }
        return null;
    }

    /**
     * Read the binary tag payload of a {@code custom} click event
     *
     * @param event The click event to read the payload of
     * @return The payload, or <code>null</code> if the event has none
     */
    public static BinaryTagHolder clickEventPayloadTag(ClickEvent event) {
        final Object payload = clickEventPayload(event);
        final Object nbt = payload != null ? invoke(payload, "nbt") : null;
        return nbt instanceof BinaryTagHolder ? (BinaryTagHolder) nbt : null;
    }

    private static Object clickEventPayload(ClickEvent event) {
        if (CLICK_EVENT_PAYLOAD == null) {
            return null;
        }
        try {
            return CLICK_EVENT_PAYLOAD.invoke(event);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    /**
     * Invoke a no-args method on an object, looking it up on the interfaces it implements first - the payload
     * implementations in Adventure 5 are package private, so their methods can only be called via their interfaces.
     *
     * @param target The object to invoke the method on
     * @param name   The name of the method
     * @return The returned value, or <code>null</code> if the method doesn't exist or couldn't be invoked
     */
    private static Object invoke(Object target, String name) {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            for (Class<?> parent : type.getInterfaces()) {
                try {
                    return parent.getMethod(name).invoke(target);
                } catch (ReflectiveOperationException | RuntimeException ignored) {
                }
            }
        }
        try {
            return target.getClass().getMethod(name).invoke(target);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static final java.lang.reflect.Method CLICK_EVENT_VALUE = findMethod(ClickEvent.class, "value");
    private static final java.lang.reflect.Method CLICK_EVENT_PAYLOAD = findMethod(ClickEvent.class, "payload");

    private static java.lang.reflect.Method findMethod(Class<?> clazz, String name) {
        try {
            return clazz.getMethod(name);
        } catch (NoSuchMethodException | SecurityException ignored) {
            return null;
        }
    }

    /**
     * Whether this Adventure version supports a certain class
     *
     * @param className The fully qualified name of the class to check for
     * @return Whether the class could be found
     */
    static boolean hasClass(String className) {
        try {
            Class.forName(className, false, Util.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
