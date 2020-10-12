package net.earthcomputer.multiconnect.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class DropDownWidget<T> extends AbstractPressableButtonWidget {

    private static final int DROP_DOWN_ELEMENT_HEIGHT = 20;
    private static final Text EXPAND_DOWN_TEXT = new LiteralText("v");
    private static final Text EXPAND_RIGHT_TEXT = new LiteralText(">");

    private final Function<T, Text> labelExtractor;
    private TooltipRenderer<T> tooltipRenderer = (stack, element, x, y) -> {};
    private final List<Category> categories = new ArrayList<>();
    private T value;
    private Consumer<T> valueListener = val -> {};

    private int hoveredCategory = -1;
    private int hoveredSubcategory = -1;
    private boolean expanded = false;

    public DropDownWidget(int x, int y, int width, int height, T initialValue, Function<T, Text> labelExtractor) {
        super(x, y, width, height, labelExtractor.apply(initialValue));
        this.labelExtractor = labelExtractor;
        this.value = initialValue;
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
        if (hoveredSubcategory != -1) {
            setValue(categories.get(hoveredCategory).children.get(hoveredSubcategory));
            playDownSound(MinecraftClient.getInstance().getSoundManager());
            expanded = false;
            return;
        }

        if (hoveredCategory == -1 || !isMouseInMainMenuPart(mouseX, mouseY)) {
            expanded = false;
            if (isHovered()) {
                playDownSound(MinecraftClient.getInstance().getSoundManager());
            }
            return;
        }

        Category category = categories.get(hoveredCategory);
        if (!category.hasChildren()) {
            setValue(category.value);
            playDownSound(MinecraftClient.getInstance().getSoundManager());
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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        if (visible && expanded) {
            updateHover(mouseX, mouseY);

            // main list
            for (int categoryIndex = 0; categoryIndex < categories.size(); categoryIndex++) {
                Category category = categories.get(categoryIndex);

                int categoryY = this.y + height + DROP_DOWN_ELEMENT_HEIGHT * categoryIndex;
                renderButtonBackground(matrices, x, categoryY, categoryIndex == hoveredCategory);

                TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                int textY = categoryY + (height - 8) / 2;
                drawCenteredText(matrices, textRenderer, labelExtractor.apply(category.value), x + width / 2, textY, 0xffffff);
                if (category.hasChildren()) {
                    drawTextWithShadow(matrices, textRenderer, EXPAND_RIGHT_TEXT, x + width - 5 - textRenderer.getWidth(EXPAND_RIGHT_TEXT), textY, 0xc0c0c0);
                }
            }

            // subcategory list
            if (hoveredCategory != -1) {
                Category category = categories.get(hoveredCategory);
                if (category.hasChildren()) {
                    int subcategoriesX = shouldExpandSubcategoriesLeft() ? x - width : x + width;
                    int subcategoriesY = y + height + DROP_DOWN_ELEMENT_HEIGHT * hoveredCategory;
                    if (shouldExpendSubcategoriesUp()) {
                        subcategoriesY -= DROP_DOWN_ELEMENT_HEIGHT * (category.children.size() - 1);
                    }

                    for (int subcategoryIndex = 0; subcategoryIndex < category.children.size(); subcategoryIndex++) {
                        int subcategoryY = subcategoriesY + DROP_DOWN_ELEMENT_HEIGHT * subcategoryIndex;
                        renderButtonBackground(matrices, subcategoriesX, subcategoryY, subcategoryIndex == hoveredSubcategory);

                        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                        drawCenteredText(matrices, textRenderer, labelExtractor.apply(category.children.get(subcategoryIndex)), subcategoriesX + width / 2, subcategoryY + (height - 8) / 2, 0xffffff);
                    }
                }

                if (hoveredSubcategory < 0 || hoveredSubcategory >= category.children.size()) {
                    tooltipRenderer.render(matrices, category.value, mouseX, mouseY);
                } else {
                    tooltipRenderer.render(matrices, category.children.get(hoveredSubcategory), mouseX, mouseY);
                }
            }
        }
    }

    private void renderButtonBackground(MatrixStack matrices, int x, int y, boolean hovered) {
        MinecraftClient.getInstance().getTextureManager().bindTexture(WIDGETS_LOCATION);
        int yImage = getYImage(hovered);
        drawTexture(matrices, x, y, 0, 46 + yImage * 20, width / 2, height);
        drawTexture(matrices, x + width / 2, y, 200 - width / 2, 46 + yImage * 20, width / 2, height);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        drawTextWithShadow(matrices, textRenderer, EXPAND_DOWN_TEXT, x + width - 5 - textRenderer.getWidth(EXPAND_DOWN_TEXT), y + (height - 8) / 2, 0xc0c0c0);
        if (isHovered())
            if (hoveredCategory == -1 || !expanded) {
                tooltipRenderer.render(matrices, value, mouseX, mouseY);
            }
    }

    private void updateHover(int mouseX, int mouseY) {
        // update hovered subcategory based on current hovered category
        if (hoveredCategory != -1 && hoveredCategory < categories.size()) {
            List<T> children = categories.get(hoveredCategory).children;

            int subcategoriesX = shouldExpandSubcategoriesLeft() ? x - width : x + width;
            int subcategoriesY = y + height + DROP_DOWN_ELEMENT_HEIGHT * hoveredCategory;
            if (shouldExpendSubcategoriesUp()) {
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
            hoveredCategory = (mouseY - y - height) / DROP_DOWN_ELEMENT_HEIGHT;
        } else {
            // if the mouse is not currently over a category, keep it hovered if the category actually has children, otherwise un-hover it
            if (hoveredCategory != -1 && hoveredCategory < categories.size() && !categories.get(hoveredCategory).hasChildren()) {
                hoveredCategory = -1;
            }
        }
    }

    private boolean isMouseInMainMenuPart(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y + height && mouseY < y + height + DROP_DOWN_ELEMENT_HEIGHT * categories.size();
    }

    private boolean shouldExpandSubcategoriesLeft() {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        assert currentScreen != null;
        return x + width + width > currentScreen.width;
    }

    private boolean shouldExpendSubcategoriesUp() {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        assert currentScreen != null;
        return y + height + DROP_DOWN_ELEMENT_HEIGHT * hoveredCategory + DROP_DOWN_ELEMENT_HEIGHT * categories.get(hoveredCategory).children.size() > currentScreen.height;
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
        void render(MatrixStack matrices, T element, int x, int y);
    }
}
