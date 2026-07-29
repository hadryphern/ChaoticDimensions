package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.menu.SirOrensTradeMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/** Screen/menu registrations that are shared by the server and client. */
public final class ModMenus {
    public static final ExtendedScreenHandlerType<SirOrensTradeMenu> SIR_ORENS_TRADES = Registry.register(
        BuiltInRegistries.MENU,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "sir_orens_trades"),
        new ExtendedScreenHandlerType<>(SirOrensTradeMenu::new)
    );

    private ModMenus() {
    }

    public static void initialize() {
        // Forces class loading during common mod initialization.
    }
}
