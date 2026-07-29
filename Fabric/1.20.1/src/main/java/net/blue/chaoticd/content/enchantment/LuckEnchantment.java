package net.blue.chaoticd.content.enchantment;

import net.blue.chaoticd.content.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * Encantamento oculto exclusivo dos equipamentos de Esmeralda.
 *
 * <p>O encantamento Sorte não aparece na mesa de encantamentos,
 * não é vendido por villagers e não pode surgir aleatoriamente
 * em loot ou livros encantados.</p>
 *
 * <p>Ele é aplicado automaticamente pelo sistema de inicialização
 * dos equipamentos de Esmeralda.</p>
 */
public final class LuckEnchantment extends Enchantment {
    public LuckEnchantment() {
        super(
            Rarity.COMMON,
            EnchantmentCategory.BREAKABLE,
            EquipmentSlot.values()
        );
    }

    /**
     * Define o custo mínimo interno do encantamento.
     *
     * <p>Este valor praticamente não será utilizado porque Sorte
     * não pode aparecer naturalmente na mesa de encantamentos.</p>
     */
    @Override
    public int getMinCost(int level) {
        return 1;
    }

    /**
     * Define o custo máximo interno do encantamento.
     */
    @Override
    public int getMaxCost(int level) {
        return 100;
    }

    /**
     * Sorte possui cinco níveis.
     */
    @Override
    public int getMaxLevel() {
        return 5;
    }

    /**
     * Permite que Sorte exista apenas nos equipamentos
     * de Esmeralda registrados pelo mod.
     */
    @Override
    public boolean canEnchant(ItemStack stack) {
        return ModItems.isEmeraldGear(stack);
    }

    /**
     * Marca Sorte como um encantamento especial.
     */
    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    /**
     * Impede villagers bibliotecários de venderem Sorte.
     */
    @Override
    public boolean isTradeable() {
        return false;
    }

    /**
     * Impede Sorte de aparecer na mesa de encantamentos,
     * em loot aleatório e em outras seleções vanilla.
     */
    @Override
    public boolean isDiscoverable() {
        return false;
    }
}
