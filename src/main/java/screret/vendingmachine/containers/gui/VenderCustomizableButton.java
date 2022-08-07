package screret.vendingmachine.containers.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.StringReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;

public class VenderCustomizableButton extends Button {

    public final ResourceLocation textureLoc;

    public final int u, v;

    public VenderCustomizableButton(ResourceLocation textureLoc, int x, int y, int xSize, int ySize, int u, int v, IPressable pressable) {
        this(textureLoc, x, y, xSize, ySize, u, v, StringTextComponent.EMPTY, pressable, NO_TOOLTIP);
    }

    public VenderCustomizableButton(ResourceLocation textureLoc, int x, int y, int xSize, int ySize, int u, int v, ITextComponent textComponent, IPressable pressable) {
        this(textureLoc, x, y, xSize, ySize, u, v, textComponent, pressable, (ITooltip)null);
    }

    public VenderCustomizableButton(ResourceLocation textureLoc, int x, int y, int xSize, int ySize, int u, int v, ITextComponent textComponent, IPressable pressable, ITooltip tooltip) {
        super(x, y, xSize, ySize, textComponent, pressable, tooltip);
        this.textureLoc = textureLoc;
        this.u = u;
        this.v = v;
    }

    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer fontrenderer = minecraft.font;
        minecraft.getTextureManager().bind(this.textureLoc);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(matrixStack, this.x, this.y, this.u, this.v + i * this.height, this.width, this.height);
        this.renderBg(matrixStack, minecraft, mouseX, mouseY);
        int j = getFGColor();
        drawCenteredString(matrixStack, fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);

        if (this.isHovered()) {
            this.renderToolTip(matrixStack, mouseX, mouseY);
        }
    }
}
