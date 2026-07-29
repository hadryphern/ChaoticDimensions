package net.blue.chaoticd.content.entity;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import net.blue.chaoticd.content.ModMenus;
import net.blue.chaoticd.content.menu.SirOrensTradeMenu;
import net.blue.chaoticd.content.trade.SirOrensTrade;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * A one-owner, stationary villager whose offers are completed by
 * {@link SirOrensTradeMenu}.  It deliberately does not use vanilla merchant
 * offers, because those offers cannot represent the six-item, 500-count
 * payments in Sir. Orens' catalogue.
 */
public final class SirOrensEntity extends Villager {
    private static final String OWNER_KEY = "SirOrensOwner";
    private static final String UNLOCKED_LEVEL_KEY = "SirOrensUnlockedLevel";
    private static final String TRADE_EXPERIENCE_KEY = "SirOrensTradeExperience";
    private static final String TRADE_USES_KEY = "SirOrensTradeUses";
    private static final String NEXT_RESTOCK_TIME_KEY = "SirOrensNextRestockTime";
    private static final double INTERACTION_RANGE_SQUARED = 64.0D;
    /** One Minecraft day, matching the cadence expected from villager restocks. */
    private static final long RESTOCK_INTERVAL_TICKS = 24_000L;

    @Nullable
    private UUID ownerUuid;
    private int unlockedLevel = 1;
    private int tradeExperience;
    private final Map<Integer, Integer> tradeUses = new HashMap<>();
    private long nextRestockGameTime = -1L;

    public SirOrensEntity(EntityType<? extends Villager> type, Level level) {
        super(type, level);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!isOwner(player)) {
            player.sendSystemMessage(Component.translatable("message.chaoticd.sir_orens_owner_only"));
            return InteractionResult.CONSUME;
        }

        if (!isWithinTradeRange(player)) {
            player.sendSystemMessage(Component.translatable("message.chaoticd.sir_orens_too_far"));
            return InteractionResult.CONSUME;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new TradeMenuProvider());
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            restockTradesIfDue();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        if (ownerUuid != null) {
            tag.putUUID(OWNER_KEY, ownerUuid);
        }

        tag.putInt(UNLOCKED_LEVEL_KEY, unlockedLevel);
        tag.putInt(TRADE_EXPERIENCE_KEY, tradeExperience);

        CompoundTag usesTag = new CompoundTag();
        for (SirOrensTrade trade : SirOrensTrade.ALL) {
            int used = tradeUses.getOrDefault(trade.id(), 0);

            if (used > 0) {
                usesTag.putInt(tradeUseKey(trade), used);
            }
        }
        tag.put(TRADE_USES_KEY, usesTag);

        if (nextRestockGameTime >= 0L) {
            tag.putLong(NEXT_RESTOCK_TIME_KEY, nextRestockGameTime);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ownerUuid = tag.hasUUID(OWNER_KEY) ? tag.getUUID(OWNER_KEY) : null;

        int savedLevel = Math.max(1, Math.min(SirOrensTrade.MAX_LEVEL, tag.getInt(UNLOCKED_LEVEL_KEY)));

        /*
         * Old Sir. Orens saves only stored the level.  Seed the new XP value
         * at that level's threshold so an existing owner never loses a tier
         * when updating the mod.
         */
        int maximumExperience = SirOrensTrade.experienceRequiredForLevel(SirOrensTrade.MAX_LEVEL);
        int savedExperience = tag.contains(TRADE_EXPERIENCE_KEY)
            ? Math.min(maximumExperience, Math.max(0, tag.getInt(TRADE_EXPERIENCE_KEY)))
            : SirOrensTrade.experienceRequiredForLevel(savedLevel);

        tradeExperience = Math.min(
            maximumExperience,
            Math.max(savedExperience, SirOrensTrade.experienceRequiredForLevel(savedLevel))
        );
        unlockedLevel = Math.max(savedLevel, SirOrensTrade.levelForExperience(tradeExperience));

        tradeUses.clear();
        if (tag.contains(TRADE_USES_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag usesTag = tag.getCompound(TRADE_USES_KEY);

            for (SirOrensTrade trade : SirOrensTrade.ALL) {
                int used = Math.max(0, Math.min(trade.maxUses(), usesTag.getInt(tradeUseKey(trade))));

                if (used > 0) {
                    tradeUses.put(trade.id(), used);
                }
            }
        }

        nextRestockGameTime = tag.contains(NEXT_RESTOCK_TIME_KEY, Tag.TAG_LONG)
            ? Math.max(-1L, tag.getLong(NEXT_RESTOCK_TIME_KEY))
            : -1L;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public boolean isOwner(Player player) {
        return isOwnedBy(player.getUUID());
    }

    public boolean isOwnedBy(UUID ownerUuid) {
        return this.ownerUuid != null && this.ownerUuid.equals(ownerUuid);
    }

    public boolean isWithinTradeRange(Player player) {
        return distanceToSqr(player) <= INTERACTION_RANGE_SQUARED;
    }

    public boolean canTradeWith(Player player) {
        return isOwner(player) && isWithinTradeRange(player) && !isRemoved();
    }

    public int getUnlockedLevel() {
        return unlockedLevel;
    }

    public int getTradeExperience() {
        return tradeExperience;
    }

    /** Returns whether this offer is available before any player payment is consumed. */
    public boolean isTradeInStock(SirOrensTrade trade) {
        restockTradesIfDue();
        return tradeUses.getOrDefault(trade.id(), 0) < trade.maxUses();
    }

    /** Returns the remaining uses of an offer after applying a due restock. */
    public int getRemainingTradeUses(SirOrensTrade trade) {
        restockTradesIfDue();
        return Math.max(0, trade.maxUses() - tradeUses.getOrDefault(trade.id(), 0));
    }

    /** Snapshot ordered exactly like {@link SirOrensTrade#ALL} for menu synchronization. */
    public int[] getRemainingTradeUses() {
        restockTradesIfDue();
        int[] remainingUses = new int[SirOrensTrade.ALL.size()];

        for (int index = 0; index < SirOrensTrade.ALL.size(); index++) {
            remainingUses[index] = getRemainingTradeUses(SirOrensTrade.ALL.get(index));
        }

        return remainingUses;
    }

    /**
     * Marks a successfully completed offer as used. The payment must already
     * have been accepted by the server before this is called.
     */
    public boolean consumeTradeUse(SirOrensTrade trade) {
        restockTradesIfDue();

        int used = tradeUses.getOrDefault(trade.id(), 0);
        if (used >= trade.maxUses()) {
            return false;
        }

        int updatedUses = used + 1;
        tradeUses.put(trade.id(), updatedUses);

        // Vanilla villagers reset their offers on a work/restock cadence even
        // when an offer was only used once. Start the same shared timer on
        // the first use, rather than making a partly used offer wait until it
        // is exhausted before it can be replenished.
        if (nextRestockGameTime < 0L) {
            nextRestockGameTime = level().getGameTime() + RESTOCK_INTERVAL_TICKS;
        }

        return true;
    }

    /**
     * Awards XP only when the completed offer belongs to the current tier.
     * Previous tiers remain usable, but cannot be farmed to bypass the next
     * stage of Sir. Orens' progression.
     *
     * @return {@code true} when the successful trade unlocked a new level
     */
    public boolean recordSuccessfulTrade(SirOrensTrade completedTrade) {
        if (unlockedLevel >= SirOrensTrade.MAX_LEVEL || completedTrade.level() != unlockedLevel) {
            return false;
        }

        int maximumExperience = SirOrensTrade.experienceRequiredForLevel(SirOrensTrade.MAX_LEVEL);
        tradeExperience = Math.min(maximumExperience, tradeExperience + completedTrade.experienceReward());
        int resolvedLevel = SirOrensTrade.levelForExperience(tradeExperience);

        if (resolvedLevel > unlockedLevel) {
            unlockedLevel = resolvedLevel;
            return true;
        }

        return false;
    }

    private void restockTradesIfDue() {
        if (level().isClientSide || nextRestockGameTime < 0L || level().getGameTime() < nextRestockGameTime) {
            return;
        }

        tradeUses.clear();
        nextRestockGameTime = -1L;
    }

    private static String tradeUseKey(SirOrensTrade trade) {
        return Integer.toString(trade.id());
    }

    /** Applies the persistent, home-bound rules configured by the spawn system. */
    public void configureForHome(UUID ownerUuid) {
        setOwnerUuid(ownerUuid);
        setCustomName(Component.literal("Sir. Orens"));
        setCustomNameVisible(true);
        setPersistenceRequired();
        setInvulnerable(true);
        setNoAi(true);
        setCanPickUpLoot(false);
    }

    private final class TradeMenuProvider implements ExtendedScreenHandlerFactory {
        @Override
        public Component getDisplayName() {
            return Component.translatable("container.chaoticd.sir_orens");
        }

        @Override
        public AbstractContainerMenu createMenu(
            int containerId,
            net.minecraft.world.entity.player.Inventory inventory,
            Player player
        ) {
            return new SirOrensTradeMenu(containerId, inventory, SirOrensEntity.this);
        }

        @Override
        public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buffer) {
            buffer.writeUUID(getUUID());
            buffer.writeVarInt(getUnlockedLevel());
            buffer.writeVarInt(getTradeExperience());
            for (int remainingUses : getRemainingTradeUses()) {
                buffer.writeVarInt(remainingUses);
            }
        }
    }
}
