package net.blue.chaoticd.content.menu;

import java.util.UUID;
import net.blue.chaoticd.content.ModMenus;
import net.blue.chaoticd.content.entity.SirOrensEntity;
import net.blue.chaoticd.content.trade.SirOrensTrade;
import net.blue.chaoticd.content.trade.SirOrensTradeService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A button-only menu that safely completes Sir. Orens' large multi-item
 * trades. The player's inventory remains the payment source, so 350+ item
 * requirements are supported without fake stacks or client trust.
 */
public final class SirOrensTradeMenu extends AbstractContainerMenu {
    private final UUID sirOrensUuid;
    private final DataSlot unlockedLevel;
    private final DataSlot tradeExperience;
    private final DataSlot[] remainingTradeUses;

    /** Client construction path used by the extended screen-handler packet. */
    public SirOrensTradeMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(
            containerId,
            inventory,
            buffer.readUUID(),
            buffer.readVarInt(),
            buffer.readVarInt(),
            readInitialRemainingUses(buffer),
            null
        );
    }

    /** Server construction path used when the player interacts with Sir. Orens. */
    public SirOrensTradeMenu(int containerId, Inventory inventory, SirOrensEntity sirOrens) {
        this(
            containerId,
            inventory,
            sirOrens.getUUID(),
            sirOrens.getUnlockedLevel(),
            sirOrens.getTradeExperience(),
            sirOrens.getRemainingTradeUses(),
            sirOrens
        );
    }

    private SirOrensTradeMenu(
        int containerId,
        Inventory inventory,
        UUID sirOrensUuid,
        int initialUnlockedLevel,
        int initialTradeExperience,
        int[] initialRemainingUses,
        SirOrensEntity serverSirOrens
    ) {
        super(ModMenus.SIR_ORENS_TRADES, containerId);
        this.sirOrensUuid = sirOrensUuid;
        this.unlockedLevel = serverSirOrens == null
            ? clientIntSlot(initialUnlockedLevel)
            : new DataSlot() {
                @Override
                public int get() {
                    return serverSirOrens.getUnlockedLevel();
                }

                @Override
                public void set(int value) {
                    // The entity is server authoritative; client values are ignored.
                }
        };
        addDataSlot(unlockedLevel);
        this.tradeExperience = serverSirOrens == null
            ? clientIntSlot(initialTradeExperience)
            : new DataSlot() {
                @Override
                public int get() {
                    return serverSirOrens.getTradeExperience();
                }

                @Override
                public void set(int value) {
                    // The entity is server authoritative; client values are ignored.
                }
        };
        addDataSlot(tradeExperience);
        this.remainingTradeUses = new DataSlot[SirOrensTrade.ALL.size()];

        for (int index = 0; index < SirOrensTrade.ALL.size(); index++) {
            SirOrensTrade trade = SirOrensTrade.ALL.get(index);
            int initialRemaining = index < initialRemainingUses.length
                ? Math.max(0, Math.min(trade.maxUses(), initialRemainingUses[index]))
                : trade.maxUses();

            remainingTradeUses[index] = serverSirOrens == null
                ? clientIntSlot(initialRemaining)
                : new DataSlot() {
                    @Override
                    public int get() {
                        return serverSirOrens.getRemainingTradeUses(trade);
                    }

                    @Override
                    public void set(int value) {
                        // The entity is server authoritative; client values are ignored.
                    }
            };
            addDataSlot(remainingTradeUses[index]);
        }
        addPlayerInventorySlots(inventory);
    }

    private static int[] readInitialRemainingUses(FriendlyByteBuf buffer) {
        int[] remainingUses = new int[SirOrensTrade.ALL.size()];

        for (int index = 0; index < remainingUses.length; index++) {
            remainingUses[index] = buffer.readVarInt();
        }

        return remainingUses;
    }

    private static DataSlot clientIntSlot(int initialValue) {
        DataSlot slot = DataSlot.standalone();
        slot.set(initialValue);
        return slot;
    }

    public UUID sirOrensUuid() {
        return sirOrensUuid;
    }

    public int unlockedLevel() {
        return unlockedLevel.get();
    }

    public int tradeExperience() {
        return tradeExperience.get();
    }

    /** XP earned inside the current tier's progress bar. */
    public int experienceIntoCurrentLevel() {
        return Math.max(
            0,
            tradeExperience() - SirOrensTrade.experienceRequiredForLevel(unlockedLevel())
        );
    }

    /** XP needed inside the current tier to make the next level available. */
    public int experienceForNextLevel() {
        if (unlockedLevel() >= SirOrensTrade.MAX_LEVEL) {
            return 0;
        }

        return SirOrensTrade.experienceRequiredForLevel(unlockedLevel() + 1)
            - SirOrensTrade.experienceRequiredForLevel(unlockedLevel());
    }

    /** Current server-synchronized stock remaining for the specified offer. */
    public int remainingUses(SirOrensTrade trade) {
        int index = SirOrensTrade.ALL.indexOf(trade);
        return index < 0 ? 0 : Math.max(0, remainingTradeUses[index].get());
    }

    public boolean isTradeInStock(SirOrensTrade trade) {
        return remainingUses(trade) > 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int tradeId) {
        if (player.level().isClientSide) {
            return true;
        }

        SirOrensTrade trade = SirOrensTrade.byId(tradeId).orElse(null);
        SirOrensEntity sirOrens = findSirOrens(player);

        if (trade == null || sirOrens == null || !sirOrens.canTradeWith(player)) {
            return false;
        }

        if (trade.level() > sirOrens.getUnlockedLevel()) {
            player.sendSystemMessage(Component.translatable("message.chaoticd.sir_orens_trade_locked"));
            return true;
        }

        if (!sirOrens.isTradeInStock(trade)) {
            broadcastChanges();
            return true;
        }

        if (!SirOrensTradeService.tryComplete(player, trade)) {
            player.sendSystemMessage(Component.translatable("message.chaoticd.sir_orens_missing_items"));
            return true;
        }

        // The stock check above and this consumption happen on the same
        // server thread. The second check is defensive and prevents an offer
        // from ever exceeding its configured use limit.
        if (!sirOrens.consumeTradeUse(trade)) {
            broadcastChanges();
            return true;
        }

        sirOrens.recordSuccessfulTrade(trade);
        player.sendSystemMessage(Component.translatable("message.chaoticd.sir_orens_trade_complete"));
        broadcastChanges();
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.level().isClientSide) {
            return true;
        }

        SirOrensEntity sirOrens = findSirOrens(player);
        return sirOrens != null && sirOrens.canTradeWith(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        if (slot < 0 || slot >= slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot source = slots.get(slot);

        if (!source.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = source.getItem();
        ItemStack original = stack.copy();
        int hotbarStart = 27;

        if (slot < hotbarStart) {
            if (!moveItemStackTo(stack, hotbarStart, 36, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, hotbarStart, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            source.set(ItemStack.EMPTY);
        } else {
            source.setChanged();
        }

        return original;
    }

    private void addPlayerInventorySlots(Inventory inventory) {
        /*
         * Keep the real, clickable inventory aligned with Minecraft's
         * villager trade layout. The custom trade service still scans these
         * same 36 slots for payments larger than a vanilla stack.
         */
        int inventoryX = 107;
        int inventoryY = 84;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                    inventory,
                    column + row * 9 + 9,
                    inventoryX + column * 18,
                    inventoryY + row * 18
                ));
            }
        }

        int hotbarY = inventoryY + 58;

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, inventoryX + column * 18, hotbarY));
        }
    }

    private SirOrensEntity findSirOrens(Player player) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return null;
        }

        Entity entity = serverPlayer.serverLevel().getEntity(sirOrensUuid);
        return entity instanceof SirOrensEntity sirOrens ? sirOrens : null;
    }
}
