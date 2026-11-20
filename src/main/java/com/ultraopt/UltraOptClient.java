package com.ultraopt;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UltraOptClient implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("ZenithVisuals");

    private static KeyMapping openKey;
    private static KeyMapping islandKey;

    @Override
    public void onInitializeClient() {
        Cfg.load();

        InputConstants.Type type;
        try {
            type = InputConstants.Type.valueOf("KEYSYM");
        } catch (IllegalArgumentException e) {
            type = InputConstants.Type.valueOf("KEYBOARD");
        }
        KeyMapping.Category cat = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("ultraopt", "main"));
        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.ultraopt.settings", type, 344, cat));  // Right Shift
        islandKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.ultraopt.island", type, 74, cat));   // J

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("ultraopt", "hud"), Hud::render);

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) Island.chat(message.getString());
        });
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, time) ->
                Island.chat(message.getString()));

        LOG.info("Zenith Visuals loaded");
    }

    private void onTick(Minecraft mc) {
        while (openKey.consumeClick()) {
            mc.gui.setScreen(new SettingsScreen(mc.gui.screen()));
        }
        while (islandKey.consumeClick()) {
            Island.toggle();
        }
        if (mc.player == null || mc.level == null) {
            CullLogic.clear();
        }
        Island.tick(mc);
    }
}
