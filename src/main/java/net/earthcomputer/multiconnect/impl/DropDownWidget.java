package net.earthcomputer.multiconnect.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class DropDownWidget<T> extends AbstractButton {

    private static final int DROP_DOWN_ELEMENT_HEIGHT = 20;
    private static final int SCROLL_BAR_WIDTH = 6;
    private static final Component EXPAND_DOWN_TEXT = Component.literal("v");
    private static final Component EXPAND_RIGHT_TEXT = Component.literal(">");

    private final Function<T, Component> labelExtractor;
    private Function<T, Component> categoryLabelExtractor;
    private TooltipRenderer<T> tooltipRenderer = (stack, element, x, y, isCategory) -> {};
    private final List<Category> categories = new ArrayList<>();
    private T value;
    private Consumer<T> valueListener = val -> {};

    private int hoveredCategory = -1;
    private int hoveredSubcategory = -1;
    private boolean expanded = false;
    private int scrollPos = 0;
    private float fractionalScroll = 0;
    private float scrollBarGrabPos = -1;

    public DropDownWidget(int x, int y, int width, int height, T initialValue, Function<T, Component> labelExtractor) {
        super(x, y, width, height, labelExtractor.apply(initialValue));
        this.labelExtractor = labelExtractor;
        this.categoryLabelExtractor = labelExtractor;
        this.value = initialValue;
    }

    public Function<T, Component> getLabelExtractor() {
        return labelExtractor;
    }

    public Function<T, Component> getCategoryLabelExtractor() {
        return categoryLabelExtractor;
    }

    public DropDownWidget<T> setCategoryLabelExtractor(Function<T, Component> categoryLabelExtractor) {
        this.categoryLabelExtractor = categoryLabelExtractor;
        return this;
    }

    public DropDownWidget<T> setTooltipRenderer(TooltipRenderer<T> tooltipRenderer) {
        this.tooltipRenderer = tooltipRenderer;
        return this;
    }

    public Category add(T value) {
        Category category = new Category(value);
        categories.add(category);
        return category;
    }

    public void remove(Category category) {
        categories.remove(category);
    }

    public void setValue(T value) {
        this.value = value;
        setMessage(labelExtractor.apply(value));
        valueListener.accept(value);
    }

    public T getValue() {
        return value;
    }

    public void setValueListener(Consumer<T> valueListener) {
        this.valueListener = valueListener;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (active && visible && expanded && isValidClickButton(button)) {
            onExpandedMouseClicked((int) mouseX, (int) mouseY);
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    private void onExpandedMouseClicked(int mouseX, int mouseY) {
        int trackHeight = numShownCategories() * DROP_DOWN_ELEMENT_HEIGHT;
        if (needsScrollBar()
                && mouseX >= getX() + width - SCROLL_BAR_WIDTH
                && mouseX < getX() + width
                && mouseY >= getY() + height
                && mouseY < getY() + height + trackHeight
        ) {
            // clicked on scroll bar
            int scrollBarY = scrollBarY();
            int scrollBarHeight = scrollBarHeight();
            if (mouseY >= getY() + height + scrollBarY && mouseY < getY() + height + scrollBarY + scrollBarHeight) {
                // clicked on scroll bar itself
                int scrollBarTop = getY() + height + scrollBarY;
                scrollBarGrabPos = (mouseY - scrollBarTop) / (float) scrollBarHeight;
            } else {
                // clicked on scroll bar track
                int scrollBarTop = mouseY - scrollBarHeight / 2 - getY() - height;
                int maxScrollBarTop = trackHeight - scrollBarHeight;
                if (maxScrollBarTop > 0) {
                    float percentage = Mth.clamp((float) scrollBarTop / maxScrollBarTop, 0, 1);
                    fractionalScroll = percentage * (categories.size() - numShownCategories());
                    scrollPos = Mth.floor(fractionalScroll);
                    fractionalScroll -= scrollPos;
                }
                scrollBarGrabPos = 0.5f;
            }
            hoveredCategory = -1;
            hoveredSubcategory = -1;
            return;
        }

        if (hoveredSubcategory != -1) {
            setValue(categories.get(hoveredCategory).children.get(hoveredSubcategory));
            playDownSound(Minecraft.getInstance().getSoundManager());
            expanded = false;
            return;
        }

        if (hoveredCategory == -1 || !isMouseInMainMenuPart(mouseX, mouseY)) {
            expanded = false;
            if (isHoveredOrFocused()) {
                playDownSound(Minecraft.getInstance().getSoundManager());
            }
            return;
        }

        Category category = categories.get(hoveredCategory);
        if (!category.hasChildren()) {
            setValue(category.value);
            playDownSound(Minecraft.getInstance().getSoundManager());
            expanded = false;
        }
    }

    @Override
    public void onPress() {
        if (!expanded) {
            expanded = true;
            hoveredCategory = -1;
            hoveredSubcategory = -1;
        }
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (scrollBarGrabPos == -1) {
            return;
        }
        int trackHeight = numShownCategories() * DROP_DOWN_ELEMENT_HEIGHT;
        int scrollBarTop = (int) (mouseY - scrollBarGrabPos * scrollBarHeight()) - getY() - height;
        int maxScrollBarTop = trackHeight - scrollBarHeight();
        if (maxScrollBarTop > 0) {
            float percentage = Mth.clamp((float) scrollBarTop / maxScrollBarTop, 0, 1);
            fractionalScroll = percentage * (categories.size() - numShownCategories());
            scrollPos = Mth.floor(fractionalScroll);
            fractionalScroll -= scrollPos;
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isValidClickButton(button)) {
            scrollBarGrabPos = -1;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!active || !visible || !expanded || !needsScrollBar() || !isMouseInMainMenuPart((int) mouseX, (int) mouseY)) {
            return false;
        }
        fractionalScroll -= amount;
        int amountToScroll = Mth.floor(fractionalScroll);
        scrollPos = Mth.clamp(scrollPos + amountToScroll, 0, categories.size() - numShownCategories());
        fractionalScroll -= amountToScroll;
        return true;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        updateHover((int) mouseX, (int) mouseY);
        return active && visible && (super.isMouseOver(mouseX, mouseY) || hoveredCategory != -1 || scrollBarGrabPos != -1);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        if (visible && expanded) {
            if (scrollPos > categories.size() - numShownCategories()) {
                scrollPos = categories.size() - numShownCategories();
            }
            updateHover(mouseX, mouseY);

            // scroll bar
            if (needsScrollBar()) {
                int scrollBarX = getX() + dropDownElementWidth();
                int scrollBarTop = getY() + height;
                int scrollBarY = scrollBarTop + scrollBarY();
                int scrollBarHeight = scrollBarHeight();
                int scrollBarBottom = scrollBarTop + numShownCategories() * DROP_DOWN_ELEMENT_HEIGHT;

                RenderSystem.disableTexture();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buffer = tesselator.getBuilder();
                buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

                // track of the bar
                buffer.vertex(scrollBarX, scrollBarBottom, 0).color(0, 0, 0, 255).endVertex();
                buffer.vertex(scrollBarX + SCROLL_BAR_WIDTH, scrollBarBottom, 0).color(0, 0, 0, 255).endVertex();
                buffer.vertex(scrollBarX + SCROLL_BAR_WIDTH, scrollBarTop, 0).color(0, 0, 0, 255).endVertex();
                buffer.vertex(scrollBarX, scrollBarTop, 0).color(0, 0, 0, 255).endVertex();
                // scroll bar shadow
                buffer.vertex(scrollBarX, scrollBarY + scrollBarHeight, 0).color(128, 128, 128, 255).endVertex();
                buffer.vertex(scrollBarX + SCROLL_BAR_WIDTH, scrollBarY + scrollBarHeight, 0).color(128, 128, 128, 255).endVertex();
                buffer.vertex(scrollBarX + SCROLL_BAR_WIDTH, scrollBarY, 0).color(128, 128, 128, 255).endVertex();
                buffer.vertex(scrollBarX, scrollBarY, 0).color(128, 128, 128, 255).endVertex();
                // scroll bar
                buffer.vertex(scrollBarX, scrollBarY + scrollBarHeight - 1, 0).color(192, 192, 192, 255).endVertex();
                buffer.vertex(scrollBarX + SCROLL_BAR_WIDTH - 1, scrollBarY + scrollBarHeight - 1, 0).color(192, 192, 192, 255).endVertex();
                buffer.vertex(scrollBarX + SCROLL_BAR_WIDTH - 1, scrollBarY, 0).color(192, 192, 192, 255).endVertex();
                buffer.vertex(scrollBarX, scrollBarY, 0).color(192, 192, 192, 255).endVertex();

                tesselator.end();
                RenderSystem.enableTexture();
            }

            // main list
            for (int categoryIndex = scrollPos, ctgyIdxEnd = scrollPos + numShownCategories(); categoryIndex < ctgyIdxEnd; categoryIndex++) {
                Category category = categories.get(categoryIndex);

                int categoryY = getY() + height + DROP_DOWN_ELEMENT_HEIGHT * (categoryIndex - scrollPos);
                renderButtonBackground(matrices, getX(), categoryY, dropDownElementWidth(), categoryIndex == hoveredCategory);

                Font textRenderer = Minecraft.getInstance().font;
                int textY = categoryY + (height - 8) / 2;
                drawCenteredString(matrices, textRenderer, categoryLabelExtractor.apply(category.value), getX() + dropDownElementWidth() / 2, textY, 0xffffff);
                if (category.hasChildren()) {
                    drawString(matrices, textRenderer, EXPAND_RIGHT_TEXT, getX() + dropDownElementWidth() - 5 - textRenderer.width(EXPAND_RIGHT_TEXT), textY, 0xc0c0c0);
                }
            }

            // subcategory list
            if (hoveredCategory != -1) {
                Category category = categories.get(hoveredCategory);
                if (category.hasChildren()) {
                    int subcategoriesX = shouldExpandSubcategoriesLeft() ? getX() - width : getX() + width;
                    int subcategoriesY = getY() + height + DROP_DOWN_ELEMENT_HEIGHT * (hoveredCategory - scrollPos);
                    if (shouldExpandSubcategoriesUp()) {
                        subcategoriesY -= DROP_DOWN_ELEMENT_HEIGHT * (category.children.size() - 1);
                    }

                    for (int subcategoryIndex = 0; subcategoryIndex < category.children.size(); subcategoryIndex++) {
                        int subcategoryY = subcategoriesY + DROP_DOWN_ELEMENT_HEIGHT * subcategoryIndex;
                        renderButtonBackground(matrices, subcategoriesX, subcategoryY, width, subcategoryIndex == hoveredSubcategory);

                        Font textRenderer = Minecraft.getInstance().font;
                        drawCenteredString(matrices, textRenderer, labelExtractor.apply(category.children.get(subcategoryIndex)), subcategoriesX + width / 2, subcategoryY + (height - 8) / 2, 0xffffff);
                    }
                }

                if (hoveredSubcategory < 0 || hoveredSubcategory >= category.children.size()) {
                    tooltipRenderer.render(matrices, category.value, mouseX, mouseY, true);
                } else {
                    tooltipRenderer.render(matrices, category.children.get(hoveredSubcategory), mouseX, mouseY, false);
                }
            }
        }
    }

    private void renderButtonBackground(PoseStack matrices, int x, int y, int width, boolean hovered) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        int yImage = getYImage(hovered);
        blit(matrices, x, y, 0, 46 + yImage * 20, width / 2, height);
        blit(matrices, x + width / 2, y, 200 - width / 2, 46 + yImage * 20, width / 2, height);
    }

    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);
        Font textRenderer = Minecraft.getInstance().font;
        drawString(matrices, textRenderer, EXPAND_DOWN_TEXT, getX() + width - 5 - textRenderer.width(EXPAND_DOWN_TEXT), getY() + (height - 8) / 2, 0xc0c0c0);
        if (isHoveredOrFocused()) {
            if (hoveredCategory == -1 || !expanded) {
                tooltipRenderer.render(matrices, value, mouseX, mouseY, false);
            }
        }
    }

    private void updateHover(int mouseX, int mouseY) {
        // update hovered subcategory based on current hovered category
        if (hoveredCategory != -1 && hoveredCategory < categories.size()) {
            List<T> children = categories.get(hoveredCategory).children;

            int subcategoriesX = shouldExpandSubcategoriesLeft() ? getX() - width : getX() + width;
            int subcategoriesY = getY() + height + DROP_DOWN_ELEMENT_HEIGHT * (hoveredCategory - scrollPos);
            if (shouldExpandSubcategoriesUp()) {
                subcategoriesY -= DROP_DOWN_ELEMENT_HEIGHT * (children.size() - 1);
            }

            if (mouseX >= subcategoriesX && mouseX < subcategoriesX + width && mouseY >= subcategoriesY) {
                hoveredSubcategory = (mouseY - subcategoriesY) / DROP_DOWN_ELEMENT_HEIGHT;
                if (hoveredSubcategory < children.size()) {
                    // we found a hovered subcategory, that's all we need to do
                    return;
                }
            }
        }

        // we didn't find a hovered subcategory
        hoveredSubcategory = -1;

        // update hovered category
        if (isMouseInMainMenuPart(mouseX, mouseY)) {
            hoveredCategory = (mouseY - getY() - height) / DROP_DOWN_ELEMENT_HEIGHT + scrollPos;
        } else {
            // if the mouse is not currently over a category, keep it hovered if the category actually has children, otherwise un-hover it
            if (hoveredCategory != -1 && hoveredCategory < categories.size() && !categories.get(hoveredCategory).hasChildren()) {
                hoveredCategory = -1;
            }
        }
    }

    private int dropDownElementWidth() {
        return needsScrollBar() ? width - SCROLL_BAR_WIDTH : width;
    }

    private boolean isMouseInMainMenuPart(int mouseX, int mouseY) {
        return mouseX >= getX() && mouseX < getX() + dropDownElementWidth() && mouseY >= getY() + height && mouseY < getY() + height + DROP_DOWN_ELEMENT_HEIGHT * numShownCategories();
    }

    private boolean shouldExpandSubcategoriesLeft() {
        Screen currentScreen = Minecraft.getInstance().screen;
        assert currentScreen != null;
        return getX() + width + width > currentScreen.width;
    }

    private boolean shouldExpandSubcategoriesUp() {
        Screen currentScreen = Minecraft.getInstance().screen;
        assert currentScreen != null;
        return getY() + height + DROP_DOWN_ELEMENT_HEIGHT * hoveredCategory + DROP_DOWN_ELEMENT_HEIGHT * categories.get(hoveredCategory).children.size() > currentScreen.height;
    }

    private boolean needsScrollBar() {
        Screen currentScreen = Minecraft.getInstance().screen;
        assert currentScreen != null;
        return getY() + height + DROP_DOWN_ELEMENT_HEIGHT * categories.size() > currentScreen.height;
    }

    private int numShownCategories() {
        if (needsScrollBar()) {
            Screen currentScreen = Minecraft.getInstance().screen;
            assert currentScreen != null;
            return Math.max(1, (currentScreen.height - getY() - height) / DROP_DOWN_ELEMENT_HEIGHT);
        }
        return categories.size();
    }

    private int scrollBarY() {
        int maxScrollBarY = numShownCategories() * DROP_DOWN_ELEMENT_HEIGHT - scrollBarHeight();
        int maxScrollPos = categories.size() - numShownCategories();
        return (int) (maxScrollBarY * Mth.clamp((scrollPos + fractionalScroll) / maxScrollPos, 0, 1));
    }

    private int scrollBarHeight() {
        int numShownCategories = numShownCategories();
        return numShownCategories * numShownCategories * DROP_DOWN_ELEMENT_HEIGHT / Math.max(1, categories.size());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput messageBuilder) {
        // TODO: better narration and accessibility
        this.defaultButtonNarrationText(messageBuilder);
    }

    public class Category {
        private T value;
        private final List<T> children = new ArrayList<>();

        private Category(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public void add(T value) {
            children.add(value);
        }

        public void remove(T value) {
            children.remove(value);
        }

        public boolean hasChildren() {
            return !children.isEmpty();
        }
    }

    @FunctionalInterface
    public interface TooltipRenderer<T> {
        void render(PoseStack matrices, T element, int x, int y, boolean isCategory);
    }
}
