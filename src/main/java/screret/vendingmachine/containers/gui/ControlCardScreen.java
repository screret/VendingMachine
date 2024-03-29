package screret.vendingmachine.containers.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.capabilities.Controller;
import screret.vendingmachine.containers.ControlCardMenu;
import screret.vendingmachine.events.packets.LoadChunkPacket;
import screret.vendingmachine.events.packets.OpenVenderGUIPacket;

public class ControlCardScreen extends AbstractContainerScreen<ControlCardMenu> {
    private final ResourceLocation textureLocation = new ResourceLocation(VendingMachine.MODID, "textures/gui/controller_gui.png");
    private final ControlCardMenu backupMenu;

    public ControlCardScreen(ControlCardMenu container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name);
        this.imageWidth = 128;
        this.imageHeight = 53;
        if(container == null || this.menu == null && playerInventory.player.containerMenu instanceof ControlCardMenu){
            backupMenu = (ControlCardMenu) playerInventory.player.containerMenu;
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
                this.addRenderableWidget(new Button(posX, posY, 18, 18, Component.empty(), onSelectedPress(slotNumber)));
            }
        }
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.textureLocation);
        this.blit(matrixStack, leftPos, topPos, 0, 0, this.getXSize(), this.getYSize());
    }

    public Button.OnPress onSelectedPress(int index)
    {
        return new Button.OnPress() {
            @Override
            public void onPress(Button button) {
                BlockPos machinePos = menu.getController().getMachine(index);
                Level level = menu.getCurrentPlayer().getLevel();
                if(!level.isLoaded(machinePos)){
                    VendingMachine.NETWORK_HANDLER.sendToServer(new LoadChunkPacket(machinePos));
                }
                VendingMachine.NETWORK_HANDLER.sendToServer(new OpenVenderGUIPacket(machinePos, true));
            }
        };
    }
}
