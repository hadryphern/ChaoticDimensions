package net.blue.chaoticd.content.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A basic leather backpack that opens a normal three-row chest inventory. */
public final class LeatherBackpackItem extends Item {
    public LeatherBackpackItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
        Level level,
        Player player,
        InteractionHand hand
    ) {
        ItemStack backpack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            BackpackContainer container = new BackpackContainer(backpack);
            player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignoredPlayer) ->
                    ChestMenu.threeRows(containerId, inventory, container),
                Component.translatable("container.chaoticd.leather_backpack")
            ));
        }

        return InteractionResultHolder.sidedSuccess(backpack, level.isClientSide);
    }
}
