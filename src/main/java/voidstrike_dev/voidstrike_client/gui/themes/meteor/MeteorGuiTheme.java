/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.gui.themes.meteor;

import voidstrike_dev.voidstrike_client.utils.render.color.SettingColor;

public class MeteorGuiTheme extends MeteorTheme {

    // Colors

    {
        accentColor = color("accent", "Main color of the GUI.", new SettingColor(145, 61, 226));
        checkboxColor = color("checkbox", "Color of checkbox.", new SettingColor(145, 61, 226));
        plusColor = color("plus", "Color of plus button.", new SettingColor(50, 255, 50));
        minusColor = color("minus", "Color of minus button.", new SettingColor(255, 50, 50));
        favoriteColor = color("favorite", "Color of checked favorite button.", new SettingColor(250, 215, 0));
    }

    // Text

    {
        textColor = color(sgTextColors, "text", "Color of text.", new SettingColor(255, 255, 255));
        textSecondaryColor = color(sgTextColors, "text-secondary-text", "Color of secondary text.", new SettingColor(150, 150, 150));
        textHighlightColor = color(sgTextColors, "text-highlight", "Color of text highlighting.", new SettingColor(45, 125, 245, 100));
        titleTextColor = color(sgTextColors, "title-text", "Color of title text.", new SettingColor(255, 255, 255));
        loggedInColor = color(sgTextColors, "logged-in-text", "Color of logged in account name.", new SettingColor(45, 225, 45));
        placeholderColor = color(sgTextColors, "placeholder", "Color of placeholder text.", new SettingColor(255, 255, 255, 20));
    }

    // Background

    {
        backgroundColor = new ThreeStateColorSetting(
                sgBackgroundColors,
                "background",
                new SettingColor(20, 20, 20, 200),
                new SettingColor(30, 30, 30, 200),
                new SettingColor(40, 40, 40, 200)
        );

        moduleBackground = color(sgBackgroundColors, "module-background", "Color of module background when active.", new SettingColor(50, 50, 50));
    }

    // Outline

    {
        outlineColor = new ThreeStateColorSetting(
                sgOutline,
                "outline",
                new SettingColor(0, 0, 0),
                new SettingColor(10, 10, 10),
                new SettingColor(20, 20, 20)
        );
    }

    // Separator

    {
        separatorText = color(sgSeparator, "separator-text", "Color of separator text", new SettingColor(255, 255, 255));
        separatorCenter = color(sgSeparator, "separator-center", "Center color of separators.", new SettingColor(255, 255, 255));
        separatorEdges = color(sgSeparator, "separator-edges", "Color of separator edges.", new SettingColor(225, 225, 225, 150));
    }

    // Scrollbar

    {
        scrollbarColor = new ThreeStateColorSetting(
                sgScrollbar,
                "Scrollbar",
                new SettingColor(30, 30, 30, 200),
                new SettingColor(40, 40, 40, 200),
                new SettingColor(50, 50, 50, 200)
        );
    }

    // Slider

    {
        sliderHandle = new ThreeStateColorSetting(
                sgSlider,
                "slider-handle",
                new SettingColor(130, 0, 255),
                new SettingColor(140, 30, 255),
                new SettingColor(150, 60, 255)
        );

        sliderLeft = color(sgSlider, "slider-left", "Color of slider left part.", new SettingColor(100,35,170));
        sliderRight = color(sgSlider, "slider-right", "Color of slider right part.", new SettingColor(50, 50, 50));
    }

    // Starscript

    {
        starscriptText = color(sgStarscript, "starscript-text", "Color of text in Starscript code.", new SettingColor(169, 183, 198));
        starscriptBraces = color(sgStarscript, "starscript-braces", "Color of braces in Starscript code.", new SettingColor(150, 150, 150));
        starscriptParenthesis = color(sgStarscript, "starscript-parenthesis", "Color of parenthesis in Starscript code.", new SettingColor(169, 183, 198));
        starscriptDots = color(sgStarscript, "starscript-dots", "Color of dots in starscript code.", new SettingColor(169, 183, 198));
        starscriptCommas = color(sgStarscript, "starscript-commas", "Color of commas in starscript code.", new SettingColor(169, 183, 198));
        starscriptOperators = color(sgStarscript, "starscript-operators", "Color of operators in Starscript code.", new SettingColor(169, 183, 198));
        starscriptStrings = color(sgStarscript, "starscript-strings", "Color of strings in Starscript code.", new SettingColor(106, 135, 89));
        starscriptNumbers = color(sgStarscript, "starscript-numbers", "Color of numbers in Starscript code.", new SettingColor(104, 141, 187));
        starscriptKeywords = color(sgStarscript, "starscript-keywords", "Color of keywords in Starscript code.", new SettingColor(204, 120, 50));
        starscriptAccessedObjects = color(sgStarscript, "starscript-accessed-objects", "Color of accessed objects (before a dot) in Starscript code.", new SettingColor(152, 118, 170));
    }

    public MeteorGuiTheme() {
        super("Contrast");
    }

}
