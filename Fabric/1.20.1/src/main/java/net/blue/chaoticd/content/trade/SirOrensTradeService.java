package net.blue.chaoticd.content.trade;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Performs complete-or-no-op inventory payment for Sir. Orens' large offers. */
public final class SirOrensTradeService {
    private SirOrensTradeService() {
    }

    /**
     * Checks every requirement before changing any stack.  This keeps a
     * partial payment from ever consuming resources when an expensive offer
     * cannot be completed.
     */
    public static boolean tryComplete(Player player, SirOrensTrade trade) {
        Inventory inventory = player.getInventory();
        Map<Item, Integer> required = requiredCounts(trade);

        if (!hasEveryCost(inventory, required)) {
            return false;
        }

        consumeEveryCost(inventory, required);
        ItemStack output = trade.outputStack();

        inventory.add(output);

        if (!output.isEmpty()) {
            player.drop(output, false);
        }

        inventory.setChanged();
        return true;
    }

    private static Map<Item, Integer> requiredCounts(SirOrensTrade trade) {
        Map<Item, Integer> required = new HashMap<>();

        for (SirOrensTrade.Cost cost : trade.costs()) {
            required.merge(cost.item(), cost.count(), Math::addExact);
        }

        return required;
    }

    private static boolean hasEveryCost(Inventory inventory, Map<Item, Integer> required) {
        Map<Item, Integer> found = new HashMap<>();

        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            ItemStack stack = inventory.getItem(slot);

            if (!stack.isEmpty() && required.containsKey(stack.getItem())) {
                found.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }

        for (Map.Entry<Item, Integer> entry : required.entrySet()) {
            if (found.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    private static void consumeEveryCost(Inventory inventory, Map<Item, Integer> required) {
        for (Map.Entry<Item, Integer> entry : required.entrySet()) {
            int remaining = entry.getValue();

            for (int slot = 0; slot < Inventory.INVENTORY_SIZE && remaining > 0; slot++) {
                ItemStack stack = inventory.getItem(slot);

                if (stack.isEmpty() || stack.getItem() != entry.getKey()) {
                    continue;
                }

                int removed = Math.min(remaining, stack.getCount());
                stack.shrink(removed);
                remaining -= removed;

                if (stack.isEmpty()) {
                    inventory.setItem(slot, ItemStack.EMPTY);
                }
            }
        }
    }
}
