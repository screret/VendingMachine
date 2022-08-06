package screret.vendingmachine.containers.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.configs.VendingMachineConfig;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.events.packets.OpenVenderGUIPacket;
import screret.vendingmachine.events.packets.PacketSendBuy;
import screret.vendingmachine.blockEntities.VendingMachineBlockEntity;

public class VenderBlockScreen extends AbstractContainerScreen<VenderBlockContainer> {
    private ResourceLocation widgets = new ResourceLocation(VendingMachine.MODID, "textures/gui/widgets.png");
    private ResourceLocation gui = new ResourceLocation(VendingMachine.MODID, "textures/gui/vending_machine_gui.png");

    public VenderBlockScreen(VenderBlockContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelX = 5;
        this.inventoryLabelY = 128;
    }

    final static int COOK_BAR_XPOS = 49;
    final static  int COOK_BAR_YPOS = 60;

    @Override
    public void init(){
        super.init();

        leftPos = (this.width - this.getXSize()) / 2;
        topPos = (this.height - this.getYSize()) / 2;

        this.addRenderableWidget(new Button(leftPos + 110, topPos + 108, 53, 18, new TranslatableComponent("gui.vendingmachine.buybutton"), onBuyButtonPress));
        //this.addRenderableWidget(new Button(leftPos + 133, topPos + 64, 18, 9, new TranslatableComponent("gui.vendingmachine.buytestbutton"), onTestButtonPress));

        if(menu.currentPlayer.equals(menu.getTile().owner)){
            this.addRenderableWidget(new VenderTabButton(leftPos + this.imageWidth, topPos + 2, 32, 28, new TranslatableComponent("gui.vendingmachine.mainbutton"), onTabButtonPress(true), true, true));
            this.addRenderableWidget(new VenderTabButton(leftPos + this.imageWidth, topPos + 30, 32, 28, new TranslatableComponent("gui.vendingmachine.tab_price"), onTabButtonPress(false), false, false));
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);

        if(menu.selectedSlot != null && !menu.isAllowedToTakeItems){
            fillGradient(poseStack, leftPos + menu.selectedSlot.x, topPos + menu.selectedSlot.y, leftPos + menu.selectedSlot.x + 16, topPos + menu.selectedSlot.y + 16, 0x7500FF00, 0x75009900);
        }

        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.gui);
        this.blit(poseStack, leftPos, topPos, 0, 0, this.getXSize(), this.getYSize());
    }

    @Override
    protected void renderTooltip(PoseStack matrixStack, ItemStack itemStack, int x, int y) {
        Object price = this.menu.getTile().getPrices().get(itemStack.getItem());
        var tooltip = this.getTooltipFromItem(itemStack);
        if(price != null && this.hoveredSlot.index < VenderBlockContainer.LAST_CONTAINER_SLOT_INDEX) tooltip.add(1, new TranslatableComponent("msg.vendingmachine.price", price, VendingMachineConfig.GENERAL.isStackPrices.get() ? itemStack.getMaxStackSize() : 1));

        this.renderTooltip(matrixStack, tooltip, itemStack.getTooltipImage(), x, y, this.font);
    }

    public final Button.OnPress onBuyButtonPress = button -> {
        //menu.getTile().buy(menu.selectedSlot.getSlotIndex());
        if(menu.selectedSlot != null) {
            if(hasShiftDown() && !VendingMachineConfig.GENERAL.isStackPrices.get()){
                VendingMachine.NETWORK_HANDLER.sendToServer(new PacketSendBuy(menu.getTile().getBlockPos(), menu.selectedSlot.getSlotIndex(), 64));
            }else if(!VendingMachineConfig.GENERAL.isStackPrices.get()){
                VendingMachine.NETWORK_HANDLER.sendToServer(new PacketSendBuy(menu.getTile().getBlockPos(), menu.selectedSlot.getSlotIndex(), 1));
            }else {
                VendingMachine.NETWORK_HANDLER.sendToServer(new PacketSendBuy(menu.getTile().getBlockPos(), menu.selectedSlot.getSlotIndex(), 64));
            }
        }
    }; //VendingMachine.SIMPLE_CHANNEL.sendToServer(new PacketSendBuy(menu.selectedSlot)); };

    public Button.OnPress onTabButtonPress(boolean isMain){
        return button -> {
            VendingMachineBlockEntity tile = menu.getTile();
            if (!isMain) {
                VendingMachine.NETWORK_HANDLER.sendToServer(new OpenVenderGUIPacket(tile.getBlockPos(), false));
            }
        };
    }
}
