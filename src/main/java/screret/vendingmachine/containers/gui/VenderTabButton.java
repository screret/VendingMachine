package screret.vendingmachine.containers.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.init.Registration;

public class VenderTabButton extends Button {

    private final int xSize;
    private final int ySize;

    boolean buttonIsMain;
    boolean selected;

    public static final ResourceLocation GUI_LOCATION = new ResourceLocation(VendingMachine.MODID,"textures/gui/vending_machine_gui.png");

    public VenderTabButton(int x, int y, int xSize, int ySize, BaseComponent text, Button.OnPress onPress, boolean isMain, boolean selected) {
        this(x, y, xSize, ySize, text, onPress, NO_TOOLTIP, isMain, selected);
    }

    public VenderTabButton(int x, int y, int xSize, int ySize, BaseComponent text, Button.OnPress onPress, OnTooltip tooltip, boolean isMain, boolean selected) {
        super(x, y, xSize, ySize, text, onPress, tooltip);
        this.buttonIsMain = isMain;
        this.xSize = xSize;
        this.ySize = ySize;
        this.selected = selected;
    }

    public ItemRenderer itemRenderer;
    ItemStack stack = new ItemStack(Registration.VENDER_ITEM_BLUE.get());

    @Override
    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        itemRenderer = minecraft.getItemRenderer();

        int itemX = x + 4;
        int itemY = y + 5;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GUI_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHoveredOrFocused());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        if (!selected && !buttonIsMain) {
            this.blit(matrixStack, this.x, this.y, 208, 28, this.xSize, this.ySize);
            this.renderBg(matrixStack, minecraft, mouseX, mouseY);
            int j = getFGColor();
            //drawCenteredString(matrixStack, fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
        } else if(!buttonIsMain) {
            this.blit(matrixStack, this.x, this.y, 176, 28, this.xSize, this.ySize);
            this.renderBg(matrixStack, minecraft, mouseX, mouseY);
            int j = getFGColor();
            //drawCenteredString(matrixStack, fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
        } else if (selected) {
            this.blit(matrixStack, this.x, this.y, 176, 0, this.xSize, this.ySize);
            this.renderBg(matrixStack, minecraft, mouseX, mouseY);
            int j = getFGColor();
            itemRenderer.renderAndDecorateItem(stack, this.x + 4, this.y + 5);
            this.itemRenderer.renderGuiItemDecorations(minecraft.font, stack, itemX, itemY);
            //drawCenteredString(matrixStack, fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
        } else {
            this.blit(matrixStack, this.x, this.y, 208, 0, this.xSize, this.ySize);
            this.renderBg(matrixStack, minecraft, mouseX, mouseY);
            int j = getFGColor();
            itemRenderer.renderAndDecorateItem(new ItemStack(Registration.VENDER_ITEM_BLUE.get()), this.x + 4, this.y + 5);
            this.itemRenderer.renderGuiItemDecorations(minecraft.font, stack, itemX, itemY);
            //drawCenteredString(matrixStack, fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
        }
    }
}
