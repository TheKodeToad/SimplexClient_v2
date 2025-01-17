/*
 *
 *       Copyright (C) 2018-present Hyperium <https://hyperium.cc/>
 *
 *       This program is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU Lesser General Public License as published
 *       by the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       This program is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU Lesser General Public License for more details.
 *
 *       You should have received a copy of the GNU Lesser General Public License
 *       along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tk.simplexclient.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.StringUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@SuppressWarnings("all")
public class FontRenderer
{
    private static final Pattern COLOR_CODE_PATTERN;
    public final int FONT_HEIGHT = 9;
    private final int[] colorCodes;
    private final Map<String, Float> cachedStringWidth;
    private float antiAliasingFactor;
    private UnicodeFont unicodeFont;
    private int prevScaleFactor;
    private final String name;
    private final float size;

    public FontRenderer(final String fontName, final float fontSize) {
        this.colorCodes = new int[] { 0, 170, 43520, 43690, 11141120, 11141290, 16755200, 11184810, 5592405, 5592575, 5635925, 5636095, 16733525, 16733695, 16777045, 16777215 };
        this.cachedStringWidth = new HashMap<String, Float>();
        this.prevScaleFactor = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        this.name = fontName;
        this.size = fontSize;
        final ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        try {
            this.prevScaleFactor = resolution.getScaleFactor();
            (this.unicodeFont = new UnicodeFont(this.getFontByName(fontName).deriveFont(fontSize * this.prevScaleFactor / 2.0f))).addAsciiGlyphs();
            this.unicodeFont.getEffects().add(new ColorEffect(Color.WHITE));
            this.unicodeFont.loadGlyphs();
        }
        catch (FontFormatException | IOException | SlickException e) {
            e.printStackTrace();
        }
        this.antiAliasingFactor = resolution.getScaleFactor();
    }

    public FontRenderer(final Font font) {
        this(font.getFontName(), font.getSize());
    }

    public FontRenderer(final String fontName, final int fontType, final int size) {
        this(new Font(fontName, fontType, size));
    }

    private Font getFontByName(final String name) throws IOException, FontFormatException {
        if (name.equalsIgnoreCase("smooth")) {
            return this.getFontFromInput("/assets/minecraft/simplex/fonts/smooth.ttf");
        }
        return this.getFontFromInput("/assets/minecraft/simplex/fonts/smooth.ttf");
    }

    private Font getFontFromInput(final String path) throws IOException, FontFormatException {
        return Font.createFont(0, FontRenderer.class.getResourceAsStream(path));
    }

    public void drawStringScaled(final String text, final int givenX, final int givenY, final int color, final double givenScale) {
        GL11.glPushMatrix();
        GL11.glTranslated((double)givenX, (double)givenY, 0.0);
        GL11.glScaled(givenScale, givenScale, givenScale);
        this.drawString(text, 0, 0, color);
        GL11.glPopMatrix();
    }

    public void drawStringScaled(final String text, final float givenX, final float givenY, final int color, final double givenScale) {
        GL11.glPushMatrix();
        GL11.glTranslated((double)givenX, (double)givenY, 0.0);
        GL11.glScaled(givenScale, givenScale, givenScale);
        this.drawString(text, 0, 0, color);
        GL11.glPopMatrix();
    }

    public int drawString(final String text, final int x, final int y, final int color) {
        return this.drawString(text, x, (float)y, color);
    }

    public int drawString(final String text, float x, float y, final int color) {
        if (text == null) {
            return 0;
        }
        final ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        try {
            if (resolution.getScaleFactor() != this.prevScaleFactor) {
                this.prevScaleFactor = resolution.getScaleFactor();
                (this.unicodeFont = new UnicodeFont(this.getFontByName(this.name).deriveFont(this.size * this.prevScaleFactor / 2.0f))).addAsciiGlyphs();
                this.unicodeFont.getEffects().add(new ColorEffect(Color.WHITE));
                this.unicodeFont.loadGlyphs();
            }
        }
        catch (FontFormatException | IOException | SlickException e) {
            e.printStackTrace();
        }
        this.antiAliasingFactor = resolution.getScaleFactor();
        GL11.glPushMatrix();
        GlStateManager.scale(1.0f / this.antiAliasingFactor, 1.0f / this.antiAliasingFactor, 1.0f / this.antiAliasingFactor);
        x *= this.antiAliasingFactor;
        y *= this.antiAliasingFactor;
        final float originalX = x;
        final float red = (color >> 16 & 0xFF) / 255.0f;
        final float green = (color >> 8 & 0xFF) / 255.0f;
        final float blue = (color & 0xFF) / 255.0f;
        final float alpha = (color >> 24 & 0xFF) / 255.0f;
        GlStateManager.color(red, green, blue, alpha);
        int currentColor = color;
        final char[] characters = text.toCharArray();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.blendFunc(770, 771);
        final String[] parts = FontRenderer.COLOR_CODE_PATTERN.split(text);
        int index = 0;
        for (final String s : parts) {
            for (final String s2 : s.split("\n")) {
                for (final String s3 : s2.split("\r")) {
                    this.unicodeFont.drawString(x, y, s3, new org.newdawn.slick.Color(currentColor));
                    x += this.unicodeFont.getWidth(s3);
                    index += s3.length();
                    if (index < characters.length && characters[index] == '\r') {
                        x = originalX;
                        ++index;
                    }
                }
                if (index < characters.length && characters[index] == '\n') {
                    x = originalX;
                    y += this.getHeight(s2) * 2.0f;
                    ++index;
                }
            }
            if (index < characters.length) {
                final char colorCode = characters[index];
                if (colorCode == '&') {
                    final char colorChar = characters[index + 1];
                    final int codeIndex = "0123456789abcdef".indexOf(colorChar);
                    if (codeIndex < 0) {
                        if (colorChar == 'r') {
                            currentColor = color;
                        }
                    }
                    else {
                        currentColor = this.colorCodes[codeIndex];
                    }
                    index += 2;
                }
            }
        }
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.bindTexture(0);
        GlStateManager.popMatrix();
        return (int)x;
    }

    public int drawStringWithShadow(final String text, final float x, final float y, final int color) {
        this.drawString(StringUtils.stripControlCodes(text), x + 1f, y + 1f, 0);
        return this.drawString(text, x, y, color);
    }

    public void drawCenteredString(final String text, final float x, final float y, final int color) {
        this.drawString(text, x - ((int)this.getWidth(text) >> 1), y, color);
    }

    public void drawCenteredTextScaled(final String text, final int givenX, final int givenY, final int color, final double givenScale) {
        GL11.glPushMatrix();
        GL11.glTranslated((double)givenX, (double)givenY, 0.0);
        GL11.glScaled(givenScale, givenScale, givenScale);
        this.drawCenteredString(text, 0.0f, 0.0f, color);
        GL11.glPopMatrix();
    }

    public void drawCenteredStringWithShadow(final String text, final float x, final float y, final int color) {
        this.drawCenteredString(StringUtils.stripControlCodes(text), x + 0.5f, y + 0.5f, color);
        this.drawCenteredString(text, x, y, color);
    }

    public float getWidth(final String text) {
        if (this.cachedStringWidth.size() > 1000) {
            this.cachedStringWidth.clear();
        }
        return this.cachedStringWidth.computeIfAbsent(text, e -> this.unicodeFont.getWidth(CharColor.stripColor(text)) / this.antiAliasingFactor);
    }

    public float getCharWidth(final char c) {
        return this.unicodeFont.getWidth(String.valueOf(c));
    }

    public float getHeight(final String s) {
        return this.unicodeFont.getHeight(s) / 2.0f;
    }

    public UnicodeFont getFont() {
        return this.unicodeFont;
    }

    public void drawSplitString(final ArrayList<String> lines, final int x, final int y, final int color) {
        this.drawString(String.join("\n\r", lines), x, y, color);
    }

    public List<String> splitString(final String text, final int wrapWidth) {
        final List<String> lines = new ArrayList<String>();
        final String[] splitText = text.split(" ");
        StringBuilder currentString = new StringBuilder();
        for (final String word : splitText) {
            final String potential = (Object)currentString + " " + word;
            if (this.getWidth(potential) >= wrapWidth) {
                lines.add(currentString.toString());
                currentString = new StringBuilder();
            }
            currentString.append(word).append(" ");
        }
        lines.add(currentString.toString());
        return lines;
    }

    public String trimStringToWidth(final String text, final int width) {
        return this.trimStringToWidth(text, width, false);
    }

    public String trimStringToWidth(final String text, final int width, final boolean reverse) {
        final StringBuilder stringbuilder = new StringBuilder();
        int i = 0;
        final int j = reverse ? (text.length() - 1) : 0;
        final int k = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag2 = false;
        for (int l = j; l >= 0 && l < text.length() && i < width; l += k) {
            final char c0 = text.charAt(l);
            final float i2 = this.getCharWidth(c0);
            if (flag) {
                flag = false;
                if (c0 != 'l' && c0 != 'L') {
                    if (c0 == 'r' || c0 == 'R') {
                        flag2 = false;
                    }
                }
                else {
                    flag2 = true;
                }
            }
            else if (i2 < 0.0f) {
                flag = true;
            }
            else {
                i += (int)i2;
                if (flag2) {
                    ++i;
                }
            }
            if (i > width) {
                break;
            }
            if (reverse) {
                stringbuilder.insert(0, c0);
            }
            else {
                stringbuilder.append(c0);
            }
        }
        return stringbuilder.toString();
    }

    public int getStringWidth(String text)
    {
        if (text == null)
        {
            return 0;
        }
        else
        {
            float f = 0.0F;
            boolean flag = false;

            for (int i = 0; i < text.length(); ++i)
            {
                char c0 = text.charAt(i);
                float f1 = this.getCharWidth(c0);

                if (f1 < 0.0F && i < text.length() - 1)
                {
                    ++i;
                    c0 = text.charAt(i);

                    if (c0 != 108 && c0 != 76)
                    {
                        if (c0 == 114 || c0 == 82)
                        {
                            flag = false;
                        }
                    }
                    else
                    {
                        flag = true;
                    }

                    f1 = 0.0F;
                }

                f += f1;
            }

            return (int)f;
        }
    }

    static {
        COLOR_CODE_PATTERN = Pattern.compile("§[0123456789abcdefklmnor]");
    }
}
