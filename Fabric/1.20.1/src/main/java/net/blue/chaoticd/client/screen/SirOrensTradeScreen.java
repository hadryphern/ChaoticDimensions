package net.blue.chaoticd.client.screen;

import net.blue.chaoticd.content.menu.SirOrensTradeMenu;
import net.blue.chaoticd.content.trade.SirOrensTrade;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * A vanilla-villager-style interface for Sir. Orens.
 *
 * <p>The menu cannot use Minecraft's actual {@code MerchantMenu}: vanilla
 * offers only accept two item stacks of at most 64 each, while Orens has
 * trades with up to six materials and counts in the hundreds. This screen
 * therefore keeps the normal villager layout and interaction language, while
 * the server safely checks every displayed requirement in the player's real
 * inventory when the result slot is clicked.</p>
 */
public final class SirOrensTradeScreen extends AbstractContainerScreen<SirOrensTradeMenu> {
    private static final ResourceLocation VILLAGER_TEXTURE = new ResourceLocation(
        "minecraft",
        "textures/gui/container/villager2.png"
    );

    private static final int TEXT_COLOR = 0xFF404040;
    private static final int OFFER_LIST_X = 5;
    private static final int OFFER_LIST_Y = 18;
    private static final int OFFER_ROW_HEIGHT = 20;
    private static final int VISIBLE_OFFERS = 7;
    private static final int FIRST_INPUT_X = 136;
    private static final int INPUT_Y = 37;
    private static final int SECOND_INPUT_X = 162;
    private static final int OUTPUT_X = 220;

    private int selectedTrade;
    private int firstVisibleTrade;

    public SirOrensTradeScreen(SirOrensTradeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 276;
        imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTradeTooltips(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(VILLAGER_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 512, 256);
        renderExperienceBar(graphics);
        renderOfferList(graphics);
        renderSelectedOffer(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Component level = Component.translatable("container.chaoticd.sir_orens.level", menu.unlockedLevel());
        graphics.drawString(font, title.copy().append(" - ").append(level), 136, 6, TEXT_COLOR, false);

        graphics.drawString(font, playerInventoryTitle, 107, 74, TEXT_COLOR, false);
    }

    /** Draws the same compact progression language used by the vanilla merchant UI. */
    private void renderExperienceBar(GuiGraphics graphics) {
        int x = leftPos + 136;
        int y = topPos + 21;
        int width = 102;
        int progressWidth;

        if (menu.unlockedLevel() >= SirOrensTrade.MAX_LEVEL) {
            progressWidth = width - 2;
        } else {
            int required = menu.experienceForNextLevel();
            progressWidth = required <= 0
                ? 0
                : (width - 2) * menu.experienceIntoCurrentLevel() / required;
        }

        graphics.fill(x, y, x + width, y + 5, 0xFF303030);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 4, 0xFF5D5D5D);

        if (progressWidth > 0) {
            graphics.fill(x + 1, y + 1, x + 1 + progressWidth, y + 4, 0xFF4B8F38);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isInsideOfferList(mouseX, mouseY)) {
            int row = (int) ((mouseY - (topPos + OFFER_LIST_Y)) / OFFER_ROW_HEIGHT);
            int tradeIndex = firstVisibleTrade + row;

            if (tradeIndex >= 0 && tradeIndex < SirOrensTrade.ALL.size()) {
                selectedTrade = tradeIndex;
                return true;
            }
        }

        if (button == 0 && isInsideResultSlot(mouseX, mouseY)) {
            if (isAvailable(currentTrade())) {
                requestTrade();
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if (isInsideOfferList(mouseX, mouseY) && maxFirstVisibleTrade() > 0 && scrollAmount != 0.0D) {
            int direction = scrollAmount > 0.0D ? -1 : 1;
            firstVisibleTrade = Math.max(0, Math.min(maxFirstVisibleTrade(), firstVisibleTrade + direction));
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollAmount);
    }

    private void renderOfferList(GuiGraphics graphics) {
        for (int row = 0; row < VISIBLE_OFFERS; row++) {
            int tradeIndex = firstVisibleTrade + row;

            if (tradeIndex >= SirOrensTrade.ALL.size()) {
                return;
            }

            SirOrensTrade trade = SirOrensTrade.ALL.get(tradeIndex);
            int x = leftPos + OFFER_LIST_X;
            int y = topPos + OFFER_LIST_Y + row * OFFER_ROW_HEIGHT;

            if (tradeIndex == selectedTrade) {
                graphics.fill(x, y, x + 89, y + 20, 0x66757575);
                graphics.renderOutline(x, y, 89, 20, 0xFF5E5E5E);
            }

            ItemStack firstCost = trade.costs().get(0).displayStack();
            graphics.renderItem(firstCost, x + 2, y + 2);
            renderCount(graphics, firstCost, x + 2, y + 2, trade.costs().get(0).count());

            if (trade.costs().size() == 2) {
                ItemStack secondCost = trade.costs().get(1).displayStack();
                graphics.renderItem(secondCost, x + 25, y + 2);
                renderCount(graphics, secondCost, x + 25, y + 2, trade.costs().get(1).count());
            } else if (trade.costs().size() > 2) {
                graphics.drawString(font, "+" + (trade.costs().size() - 1), x + 25, y + 6, TEXT_COLOR, false);
            }

            graphics.drawString(font, "→", x + 49, y + 5, TEXT_COLOR, false);

            ItemStack output = trade.outputStack();
            graphics.renderItem(output, x + 69, y + 2);
            renderCount(graphics, output, x + 69, y + 2, trade.outputCount());

            if (!isAvailable(trade)) {
                graphics.fill(x, y, x + 89, y + 20, 0x669B0000);
            }
        }

        if (maxFirstVisibleTrade() > 0) {
            int thumbHeight = Math.max(12, 134 * VISIBLE_OFFERS / SirOrensTrade.ALL.size());
            int thumbY = topPos + OFFER_LIST_Y
                + (134 - thumbHeight) * firstVisibleTrade / maxFirstVisibleTrade();
            graphics.fill(leftPos + 94, topPos + OFFER_LIST_Y, leftPos + 98, topPos + 152, 0xFF6A6A6A);
            graphics.fill(leftPos + 94, thumbY, leftPos + 98, thumbY + thumbHeight, 0xFFB8B8B8);
        }
    }

    private void renderSelectedOffer(GuiGraphics graphics) {
        SirOrensTrade trade = currentTrade();

        renderCostSlot(graphics, trade.costs().get(0), leftPos + FIRST_INPUT_X, topPos + INPUT_Y, false);

        if (trade.costs().size() >= 2) {
            renderCostSlot(graphics, trade.costs().get(1), leftPos + SECOND_INPUT_X, topPos + INPUT_Y, false);
        }

        ItemStack output = trade.outputStack();
        graphics.renderItem(output, leftPos + OUTPUT_X, topPos + INPUT_Y);
        renderCount(graphics, output, leftPos + OUTPUT_X, topPos + INPUT_Y, trade.outputCount());

        for (int index = 2; index < trade.costs().size(); index++) {
            int column = index - 2;
            int x = leftPos + FIRST_INPUT_X + column * 20;
            int y = topPos + 56;
            renderCostSlot(graphics, trade.costs().get(index), x, y, true);
        }

        if (!isAvailable(trade)) {
            graphics.fill(leftPos + OUTPUT_X, topPos + INPUT_Y, leftPos + OUTPUT_X + 16, topPos + INPUT_Y + 16, 0x99A00000);
        }
    }

    private void renderCostSlot(
        GuiGraphics graphics,
        SirOrensTrade.Cost cost,
        int x,
        int y,
        boolean drawBacking
    ) {
        if (drawBacking) {
            graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF8B8B8B);
            graphics.fill(x, y, x + 16, y + 16, 0xFF3F3F3F);
        }

        ItemStack stack = cost.displayStack();
        graphics.renderItem(stack, x, y);
        renderCount(graphics, stack, x, y, cost.count());
    }

    private void renderCount(GuiGraphics graphics, ItemStack stack, int x, int y, int count) {
        if (count > 1) {
            graphics.renderItemDecorations(font, stack, x, y, Integer.toString(count));
        }
    }

    private void renderTradeTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        SirOrensTrade trade = currentTrade();

        if (isWithin(mouseX, mouseY, leftPos + FIRST_INPUT_X, topPos + INPUT_Y, 16, 16)) {
            graphics.renderTooltip(font, trade.costs().get(0).displayStack(), mouseX, mouseY);
            return;
        }

        if (trade.costs().size() >= 2
            && isWithin(mouseX, mouseY, leftPos + SECOND_INPUT_X, topPos + INPUT_Y, 16, 16)) {
            graphics.renderTooltip(font, trade.costs().get(1).displayStack(), mouseX, mouseY);
            return;
        }

        for (int index = 2; index < trade.costs().size(); index++) {
            int x = leftPos + FIRST_INPUT_X + (index - 2) * 20;

            if (isWithin(mouseX, mouseY, x, topPos + 56, 16, 16)) {
                graphics.renderTooltip(font, trade.costs().get(index).displayStack(), mouseX, mouseY);
                return;
            }
        }

        if (isWithin(mouseX, mouseY, leftPos + OUTPUT_X, topPos + INPUT_Y, 16, 16)) {
            graphics.renderTooltip(font, trade.outputStack(), mouseX, mouseY);
            return;
        }

        if (isInsideOfferList(mouseX, mouseY)) {
            int row = (mouseY - (topPos + OFFER_LIST_Y)) / OFFER_ROW_HEIGHT;
            int tradeIndex = firstVisibleTrade + row;

            if (tradeIndex >= 0 && tradeIndex < SirOrensTrade.ALL.size()) {
                SirOrensTrade hoveredTrade = SirOrensTrade.ALL.get(tradeIndex);
                graphics.renderTooltip(font, hoveredTrade.outputStack(), mouseX, mouseY);
            }
        }
    }

    private boolean isInsideOfferList(double mouseX, double mouseY) {
        return mouseX >= leftPos + OFFER_LIST_X
            && mouseX < leftPos + 94
            && mouseY >= topPos + OFFER_LIST_Y
            && mouseY < topPos + OFFER_LIST_Y + VISIBLE_OFFERS * OFFER_ROW_HEIGHT;
    }

    private boolean isInsideResultSlot(double mouseX, double mouseY) {
        return isWithin(mouseX, mouseY, leftPos + OUTPUT_X, topPos + INPUT_Y, 16, 16);
    }

    private static boolean isWithin(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private int maxFirstVisibleTrade() {
        return Math.max(0, SirOrensTrade.ALL.size() - VISIBLE_OFFERS);
    }

    private boolean isUnlocked(SirOrensTrade trade) {
        return trade.level() <= menu.unlockedLevel();
    }

    /** Reuses the vanilla unavailable tint for a depleted offer without changing the layout. */
    private boolean isAvailable(SirOrensTrade trade) {
        return isUnlocked(trade) && menu.isTradeInStock(trade);
    }

    private void requestTrade() {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, currentTrade().id());
        }
    }

    private SirOrensTrade currentTrade() {
        return SirOrensTrade.ALL.get(selectedTrade);
    }
}
