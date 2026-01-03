/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.renderer;

import voidstrike_dev.voidstrike_client.MeteorClient;
import voidstrike_dev.voidstrike_client.events.meteor.CustomFontChangedEvent;
import voidstrike_dev.voidstrike_client.gui.WidgetScreen;
import voidstrike_dev.voidstrike_client.renderer.text.CustomTextRenderer;
import voidstrike_dev.voidstrike_client.renderer.text.FontFace;
import voidstrike_dev.voidstrike_client.renderer.text.FontFamily;
import voidstrike_dev.voidstrike_client.renderer.text.FontInfo;
import voidstrike_dev.voidstrike_client.systems.config.Config;
import voidstrike_dev.voidstrike_client.utils.PreInit;
import voidstrike_dev.voidstrike_client.utils.render.FontUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static voidstrike_dev.voidstrike_client.MeteorClient.mc;

public class Fonts {
    public static final String[] BUILTIN_FONTS = { "JetBrains Mono", "Comfortaa", "Tw Cen MT", "Pixelation" };

    public static String DEFAULT_FONT_FAMILY;
    public static FontFace DEFAULT_FONT;

    public static final List<FontFamily> FONT_FAMILIES = new ArrayList<>();
    public static CustomTextRenderer RENDERER;

    private Fonts() {
    }

    @PreInit
    public static void refresh() {
        FONT_FAMILIES.clear();

        for (String builtinFont : BUILTIN_FONTS) {
            FontUtils.loadBuiltin(FONT_FAMILIES, builtinFont);
        }

        for (String fontPath : FontUtils.getSearchPaths()) {
            FontUtils.loadSystem(FONT_FAMILIES, new File(fontPath));
        }

        FONT_FAMILIES.sort(Comparator.comparing(FontFamily::getName));

        MeteorClient.LOG.info("Found {} font families.", FONT_FAMILIES.size());

        DEFAULT_FONT_FAMILY = FontUtils.getBuiltinFontInfo(BUILTIN_FONTS[1]).family();
        DEFAULT_FONT = getFamily(DEFAULT_FONT_FAMILY).get(FontInfo.Type.Regular);

        Config config = Config.get();
        load(config != null ? config.font.get() : DEFAULT_FONT);
    }

    public static void load(FontFace fontFace) {
        // Ensure we always have a renderer
        ensureRendererExists();
        
        if (fontFace == null) {
            fontFace = DEFAULT_FONT;
        }
        
        if (fontFace == null) {
            MeteorClient.LOG.error("Both fontFace and DEFAULT_FONT are null! Cannot load any font.");
            return;
        }
        
        if (RENDERER != null) {
            try {
                if (RENDERER.fontFace.equals(fontFace)) return;
                else RENDERER.destroy();
            } catch (Exception e) {
                MeteorClient.LOG.error("Error checking current font", e);
            }
        }

        try {
            RENDERER = new CustomTextRenderer(fontFace);
            MeteorClient.EVENT_BUS.post(CustomFontChangedEvent.get());
        }
        catch (Exception e) {
            MeteorClient.LOG.error("Failed to load font: {}, trying fallback", fontFace, e);
            
            // Try to load any available font as fallback
            FontFace fallback = getFirstAvailableFont();
            if (fallback != null && !fallback.equals(fontFace)) {
                try {
                    RENDERER = new CustomTextRenderer(fallback);
                    MeteorClient.EVENT_BUS.post(CustomFontChangedEvent.get());
                    MeteorClient.LOG.info("Successfully loaded fallback font: {}", fallback);
                } catch (Exception fallbackException) {
                    MeteorClient.LOG.error("Failed to load fallback font too", fallbackException);
                }
            }
        }

        if (mc.currentScreen instanceof WidgetScreen && Config.get().customFont.get()) {
            ((WidgetScreen) mc.currentScreen).invalidate();
        }
    }
    
    private static void ensureRendererExists() {
        if (RENDERER == null) {
            try {
                // Try to create a renderer with any available font
                FontFace fallback = getFirstAvailableFont();
                if (fallback != null) {
                    RENDERER = new CustomTextRenderer(fallback);
                    MeteorClient.LOG.info("Created emergency renderer with font: {}", fallback);
                } else {
                    MeteorClient.LOG.warn("No fonts available for emergency renderer");
                }
            } catch (Exception e) {
                MeteorClient.LOG.error("Failed to create emergency renderer", e);
            }
        }
    }

    public static FontFamily getFamily(String name) {
        for (FontFamily fontFamily : Fonts.FONT_FAMILIES) {
            if (fontFamily.getName().equalsIgnoreCase(name)) {
                return fontFamily;
            }
        }

        return null;
    }
    
    private static FontFace getFirstAvailableFont() {
        // Try to get the first available font from any family
        for (FontFamily family : FONT_FAMILIES) {
            if (family != null) {
                FontFace font = family.get(FontInfo.Type.Regular);
                if (font != null) return font;
                
                font = family.get(FontInfo.Type.Bold);
                if (font != null) return font;
                
                font = family.get(FontInfo.Type.Italic);
                if (font != null) return font;
                
                font = family.get(FontInfo.Type.BoldItalic);
                if (font != null) return font;
            }
        }
        return null;
    }
}
