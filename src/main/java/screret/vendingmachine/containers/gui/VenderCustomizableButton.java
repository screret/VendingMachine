package screret.vendingmachine.containers.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class VenderCustomizableButton extends Button {

    public final ResourceLocation textureLoc;

    public final int u, v;

    public VenderCustomizableButton(ResourceLocation textureLoc, int x, int y, int xSize, int ySize, int u, int v, OnPress pressable) {
        this(textureLoc, x, y, xSize, ySize, u, v, Component.empty(), pressable, NO_TOOLTIP);
    }

    public VenderCustomizableButton(ResourceLocation textureLoc, int x, int y, int xSize, int ySize, int u, int v, Component textComponent, OnPress pressable) {
        this(textureLoc, x, y, xSize, ySize, u, v, textComponent, pressable, NO_TOOLTIP);
    }

    public VenderCustomizableButton(ResourceLocation textureLoc, int x, int y, int xSize, int ySize, int u, int v, Component textComponent, OnPress pressable, OnTooltip tooltip) {
        super(x, y, xSize, ySize, textComponent, pressable, tooltip);
        this.textureLoc = textureLoc;
        this.u = u;
        this.v = v;
    }

    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        Font fontrenderer = minecraft.font;
        minecraft.getTextureManager().bindForSetup(this.textureLoc);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(poseStack, this.x, this.y, this.u, this.v + i * this.height, this.width, this.height);
        this.renderBg(poseStack, minecraft, mouseX, mouseY);
        int j = getFGColor();
        drawCenteredString(poseStack, fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);

        if (this.isHovered) {
            this.renderToolTip(poseStack, mouseX, mouseY);
        }
    }
}
