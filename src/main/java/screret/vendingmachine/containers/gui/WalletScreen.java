package screret.vendingmachine.containers.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.containers.WalletItemMenu;

public class WalletScreen extends AbstractContainerScreen<WalletItemMenu> {

    private static final ResourceLocation GUI = new ResourceLocation(VendingMachine.MODID, "textures/gui/wallet.png");


    public WalletScreen(WalletItemMenu menu, Inventory inventory, Component displayName) {
        super(menu, inventory, displayName);
        this.imageWidth = 176;
        this.imageHeight = 144;
        this.inventoryLabelX = 5;
        this.inventoryLabelY = 52;
    }

    @Override
    public void render(PoseStack poseStack,  int mouseX, int mouseY, float partialTicks){
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI);
        this.blit(poseStack, leftPos, topPos, 0, 0, this.getXSize(), this.getYSize());
    }
}
