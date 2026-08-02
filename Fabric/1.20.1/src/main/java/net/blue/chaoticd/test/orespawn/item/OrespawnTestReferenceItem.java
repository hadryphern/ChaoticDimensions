package net.blue.chaoticd.test.orespawn.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** An original inert marker used to prove the isolated test item pipeline. */
public final class OrespawnTestReferenceItem extends Item {
    public OrespawnTestReferenceItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        Level level,
        List<Component> tooltip,
        TooltipFlag flag
    ) {
        tooltip.add(Component.translatable("tooltip.chaoticd.orespawn_test.local_only")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.chaoticd.orespawn_test.no_legacy_asset")
            .withStyle(ChatFormatting.DARK_GRAY));
    }
}
