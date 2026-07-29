package net.blue.chaoticd.mixin;

import java.util.ArrayList;
import java.util.List;
import net.blue.chaoticd.content.ModItems;
import net.blue.chaoticd.gameplay.DeathTotemInventorySlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps Death Totems inside the old player inventory while every other item is
 * dropped normally. ShadowDimensionSystems copies the retained stack on respawn.
 */
@Mixin(Inventory.class)
public abstract class InventoryDropMixin {
    @Unique
    private List<DeathTotemInventorySlot> chaoticd$savedDeathTotems = List.of();

    @Inject(method = "dropAll", at = @At("HEAD"))
    private void chaoticd$hideDeathTotemsBeforeDrop(CallbackInfo callback) {
        Inventory inventory = (Inventory)(Object)this;
        List<DeathTotemInventorySlot> saved = new ArrayList<>();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);

            if (stack.is(ModItems.DEATH_TOTEM)) {
                saved.add(new DeathTotemInventorySlot(slot, stack.copy()));
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }

        chaoticd$savedDeathTotems = saved;
    }

    @Inject(method = "dropAll", at = @At("TAIL"))
    private void chaoticd$restoreDeathTotemsAfterDrop(CallbackInfo callback) {
        Inventory inventory = (Inventory)(Object)this;

        for (DeathTotemInventorySlot saved : chaoticd$savedDeathTotems) {
            inventory.setItem(saved.slot(), saved.stack());
        }

        chaoticd$savedDeathTotems = List.of();
    }
}
