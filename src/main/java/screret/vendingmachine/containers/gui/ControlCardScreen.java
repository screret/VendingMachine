package screret.vendingmachine.containers.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.capabilities.Controller;
import screret.vendingmachine.containers.ContainerControlCard;
import screret.vendingmachine.events.packets.OpenVenderGUIPacket;

public class ControlCardScreen extends ContainerScreen<ContainerControlCard> {
    private final ResourceLocation textureLocation = new ResourceLocation(VendingMachine.MODID, "textures/gui/controller_gui.png");
    private static Logger logger = LogManager.getLogger();
    private final ContainerControlCard backupMenu;

    public ControlCardScreen(ContainerControlCard container, PlayerInventory playerInventory, ITextComponent name) {
        super(container, playerInventory, name);
        this.imageWidth = 128;
        this.imageHeight = 53;
        if(container == null || this.menu == null && playerInventory.player.containerMenu instanceof ContainerControlCard){
            backupMenu = (ContainerControlCard) playerInventory.player.containerMenu;
        } else {
            backupMenu = null;
        }
    }

    @Override
    public void init() {
        super.init();
        final int INPUT_SLOTS_XPOS = 12;
        final int INPUT_SLOTS_YPOS = 12;
        final int SLOT_X_SPACING = 18;
        final int SLOT_Y_SPACING = 18;

        Controller controller = menu.getController();

        for(int x = 0; x < controller.getMachines(); ++x){
            for(int y = 0; y < 3; ++y){
                int posX = leftPos + INPUT_SLOTS_XPOS + SLOT_X_SPACING * x;
                int posY = leftPos + INPUT_SLOTS_YPOS + SLOT_Y_SPACING * y;
                int slotNumber = y * 2 + x;
                this.addButton(new Button(posX, posY, 18, 18, StringTextComponent.EMPTY, onSelectedPress(slotNumber)));
            }
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1, 1, 1, 1);
        this.minecraft.getTextureManager().bind(textureLocation);
        this.blit(matrixStack, leftPos, topPos, 0, 0, this.getXSize(), this.getYSize());
    }

    public Button.IPressable onSelectedPress(int index)
    {
        return new Button.IPressable() {
            @Override
            public void onPress(Button button) {
                BlockPos machinePos = menu.getController().getMachine(index);
                VendingMachine.NETWORK_HANDLER.sendToServer(new OpenVenderGUIPacket(machinePos, true));
            }
        };
    }
}
