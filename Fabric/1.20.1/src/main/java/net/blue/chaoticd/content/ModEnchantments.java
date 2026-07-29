package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.enchantment.BigBerthaEnchantment;
import net.blue.chaoticd.content.enchantment.DheathicEnchantment;
import net.blue.chaoticd.content.enchantment.DisparadaEnchantment;
import net.blue.chaoticd.content.enchantment.LuckEnchantment;
import net.blue.chaoticd.content.enchantment.RoyalEnchantment;
import net.blue.chaoticd.content.enchantment.SapphiricEnchantment;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;

/** Registers Chaotic Dimensions enchantments and obtainable enchantment books. */
public final class ModEnchantments {
    public static final Enchantment SAPPHIRIC = register(
        "sapphiric",
        new SapphiricEnchantment()
    );

    public static final Enchantment DHEATHIC = register(
        "dheathic",
        new DheathicEnchantment()
    );

    public static final Enchantment BIG_BERTHA = register(
        "big_bertha",
        new BigBerthaEnchantment()
    );

    public static final Enchantment ROYAL = register(
        "royal",
        new RoyalEnchantment()
    );

    public static final Enchantment DISPARADA = register(
        "disparada",
        new DisparadaEnchantment()
    );

    public static final Enchantment LUCK = register(
        "luck",
        new LuckEnchantment()
    );

    private ModEnchantments() {
    }

    private static Enchantment register(
        String id,
        Enchantment enchantment
    ) {
        return Registry.register(
            BuiltInRegistries.ENCHANTMENT,
            new ResourceLocation(ChaoticDimensions.MOD_ID, id),
            enchantment
        );
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(
            CreativeModeTabs.TOOLS_AND_UTILITIES
        ).register(entries -> {
            /* Vanilla has already inserted its normal levels. Only append the
             * extended levels so search and the tab do not contain duplicates. */
            addBooks(entries, Enchantments.SHARPNESS, 6, 15);
            addBooks(entries, Enchantments.UNBREAKING, 4, 10);
            addBooks(entries, Enchantments.ALL_DAMAGE_PROTECTION, 5, 15);
            addBooks(entries, Enchantments.FIRE_PROTECTION, 5, 15);
            addBooks(entries, Enchantments.BLAST_PROTECTION, 5, 15);
            addBooks(entries, Enchantments.PROJECTILE_PROTECTION, 5, 15);
            addBooks(entries, Enchantments.FALL_PROTECTION, 5, 15);
            addBooks(entries, Enchantments.THORNS, 4, 15);
            addBooks(entries, Enchantments.BLOCK_EFFICIENCY, 6, 10);
            addBooks(entries, Enchantments.KNOCKBACK, 3, 20);
            addBooks(entries, Enchantments.SMITE, 6, 10);
            addBooks(entries, Enchantments.BANE_OF_ARTHROPODS, 6, 10);
            addBooks(entries, Enchantments.SWEEPING_EDGE, 4, 10);
            addBooks(entries, Enchantments.FIRE_ASPECT, 3, 10);
            addBooks(entries, Enchantments.MOB_LOOTING, 4, 10);
            addBooks(entries, Enchantments.BLOCK_FORTUNE, 4, 10);
            /*
             * Every Chaotic Dimensions enchantment, including Disparada, is
             * intentionally kept in the mod's own creative tab. This vanilla
             * tab contains only the normal vanilla books and their extended
             * levels, together in one ordered sequence.
             *
             * Luck is intentionally omitted from either book list. It cannot
             * be obtained as a book, through villagers or through the
             * enchantment table.
             */
        });
    }

    private static void addBooks(
        FabricItemGroupEntries entries,
        Enchantment enchantment,
        int firstLevel,
        int maxLevel
    ) {
        for (int level = firstLevel; level <= maxLevel; level++) {
            entries.accept(
                EnchantedBookItem.createForEnchantment(
                    new EnchantmentInstance(enchantment, level)
                )
            );
        }
    }
}
