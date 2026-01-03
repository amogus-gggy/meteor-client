/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client;

import voidstrike_dev.voidstrike_client.addons.AddonManager;
import voidstrike_dev.voidstrike_client.addons.MeteorAddon;
import voidstrike_dev.voidstrike_client.events.game.OpenScreenEvent;
import voidstrike_dev.voidstrike_client.events.meteor.KeyEvent;
import voidstrike_dev.voidstrike_client.events.meteor.MouseClickEvent;
import voidstrike_dev.voidstrike_client.events.world.TickEvent;
import voidstrike_dev.voidstrike_client.gui.GuiThemes;
import voidstrike_dev.voidstrike_client.gui.WidgetScreen;
import voidstrike_dev.voidstrike_client.gui.tabs.Tab;
import voidstrike_dev.voidstrike_client.gui.tabs.Tabs;
import voidstrike_dev.voidstrike_client.renderer.Fonts;
import voidstrike_dev.voidstrike_client.systems.Systems;
import voidstrike_dev.voidstrike_client.systems.config.Config;
import voidstrike_dev.voidstrike_client.systems.hud.screens.HudEditorScreen;
import voidstrike_dev.voidstrike_client.systems.modules.Categories;
import voidstrike_dev.voidstrike_client.systems.modules.Modules;
import voidstrike_dev.voidstrike_client.systems.modules.misc.DiscordPresence;
import voidstrike_dev.voidstrike_client.utils.PostInit;
import voidstrike_dev.voidstrike_client.utils.PreInit;
import voidstrike_dev.voidstrike_client.utils.ReflectInit;
import voidstrike_dev.voidstrike_client.utils.Utils;
import voidstrike_dev.voidstrike_client.utils.misc.Version;
import voidstrike_dev.voidstrike_client.utils.misc.input.KeyAction;
import voidstrike_dev.voidstrike_client.utils.misc.input.KeyBinds;
import voidstrike_dev.voidstrike_client.utils.network.OnlinePlayers;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.List;

public class MeteorClient implements ClientModInitializer {
    public static final String MOD_ID = "voidstrike-client";
    public static final ModMetadata MOD_META;
    public static final String NAME;
    public static final Version VERSION;
    public static final String BUILD_NUMBER;

    public static MeteorClient INSTANCE;
    public static MeteorAddon ADDON;

    public static MinecraftClient mc;
    public static final IEventBus EVENT_BUS = new EventBus();
    public static final File FOLDER = FabricLoader.getInstance().getGameDir().resolve(MOD_ID).toFile();
    public static final Logger LOG;

    static {
        MOD_META = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata();

        NAME = MOD_META.getName();
        LOG = LoggerFactory.getLogger(NAME);

        String versionString = MOD_META.getVersion().getFriendlyString();
        if (versionString.contains("-")) versionString = versionString.split("-")[0];

        // When building and running through IntelliJ and not Gradle it doesn't replace the version so just use a dummy
        if (versionString.equals("${version}")) versionString = "0.0.0";

        VERSION = new Version(versionString);
        BUILD_NUMBER = MOD_META.getCustomValue(MeteorClient.MOD_ID + ":build_number").getAsString();
    }

    @Override
    public void onInitializeClient() {
        if (INSTANCE == null) {
            INSTANCE = this;
            return;
        }

        // Global minecraft client accessor
        mc = MinecraftClient.getInstance();

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            LOG.info("Force loading mixins");
            MixinEnvironment.getCurrentEnvironment().audit();
        }

        LOG.info("Initializing {}", NAME);

        // Pre-load
        if (!FOLDER.exists()) {
            FOLDER.getParentFile().mkdirs();
            FOLDER.mkdir();
            Systems.addPreLoadTask(() -> Modules.get().get(DiscordPresence.class).enable());
        }

        // Register addons
        AddonManager.init();

        // Register event handlers
        EVENT_BUS.registerLambdaFactory("voidstrike_dev.voidstrike_client", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        AddonManager.ADDONS.forEach(addon -> {
            try {
                EVENT_BUS.registerLambdaFactory(addon.getPackage(), (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
            } catch (AbstractMethodError e) {
                throw new RuntimeException("Addon \"%s\" is too old and cannot be ran.".formatted(addon.name), e);
            }
        });

        // Register init classes
        ReflectInit.registerPackages();

        // Pre init
        ReflectInit.init(PreInit.class);

        // Register module categories
        Categories.init();

        // Load systems
        Systems.init();

        // Subscribe after systems are loaded
        EVENT_BUS.subscribe(this);

        // Initialise addons
        AddonManager.ADDONS.forEach(MeteorAddon::onInitialize);

        // Sort modules after addons have added their own
        Modules.get().sortModules();

        // Load configs
        Systems.load();

        // Post init
        ReflectInit.init(PostInit.class);

        // Save on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            OnlinePlayers.leave();
            Systems.save();
            GuiThemes.save();
        }));
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.currentScreen == null && mc.getOverlay() == null && KeyBinds.OPEN_COMMANDS.wasPressed()) {
            mc.setScreen(new ChatScreen(Config.get().prefix.get(), true));
        }
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Press && KeyBinds.OPEN_GUI.matchesKey(event.input)) {
            toggleGui();
        }
    }

    @EventHandler
    private void onMouseClick(MouseClickEvent event) {
        if (event.action == KeyAction.Press && KeyBinds.OPEN_GUI.matchesMouse(event.click)) {
            toggleGui();
        }
    }

    private void toggleGui() {
        if (Utils.canCloseGui()) mc.currentScreen.close();
        else if (Utils.canOpenGui()) {
            List<? extends Tab> tabs = Tabs.get();
            if (!tabs.isEmpty()) {
                tabs.getFirst().openScreen(GuiThemes.get());
            } else {
                LOG.warn("No tabs available to open GUI");
            }
        }
    }

    // Hide HUD

    private boolean wasWidgetScreen, wasHudHiddenRoot;

    @EventHandler(priority = EventPriority.LOWEST)
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof WidgetScreen) {
            if (!wasWidgetScreen) wasHudHiddenRoot = mc.options.hudHidden;
            if (GuiThemes.get().hideHUD() || wasHudHiddenRoot) {
                // Always show the MC HUD in the HUD editor screen since people like
                // to align some items with the hotbar or chat
                mc.options.hudHidden = !(event.screen instanceof HudEditorScreen);
            }
        } else {
            if (wasWidgetScreen) mc.options.hudHidden = wasHudHiddenRoot;
            wasHudHiddenRoot = mc.options.hudHidden;
        }

        wasWidgetScreen = event.screen instanceof WidgetScreen;
    }

    public static Identifier identifier(String path) {
        return Identifier.of(MeteorClient.MOD_ID, path);
    }
}
