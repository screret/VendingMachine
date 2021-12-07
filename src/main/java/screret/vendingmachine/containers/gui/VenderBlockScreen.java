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
import screret.vendingmachine.events.packets.OpenGUIPacket;
import screret.vendingmachine.events.packets.PacketAllowItemTake;
import screret.vendingmachine.events.packets.PacketSendBuy;
import screret.vendingmachine.tileEntities.VendingMachineTile;

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
        this.addRenderableWidget(new Button(leftPos + 133, topPos + 64, 18, 9, new TranslatableComponent("gui.vendingmachine.buytestbutton"), onTestButtonPress));

        if(menu.currentPlayer.equals(menu.getTile().owner)){
            this.addRenderableWidget(new VenderTabButton(leftPos + this.imageWidth, topPos + 12, 32, 28, new TranslatableComponent("gui.vendingmachine.tab_price"), onTabButtonPress(true), true, true));
            this.addRenderableWidget(new VenderTabButton(leftPos + this.imageWidth, topPos + 40, 32, 28, new TranslatableComponent("gui.vendingmachine.tab_price"), onTabButtonPress(false), false, false));
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);

        if(menu.selectedSlot != null && !menu.isAllowedToTakeItems){
            fillGradient(poseStack, leftPos + menu.selectedSlot.x, topPos + menu.selectedSlot.y, leftPos + menu.selectedSlot.x + 16, topPos + menu.selectedSlot.y + 16, 0x7500FF00, 0x75009900);
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.gui);
        leftPos = (this.width - this.getXSize()) / 2;
        topPos = (this.height - this.getYSize()) / 2;
        this.blit(poseStack, leftPos, topPos, 0, 0, this.getXSize(), this.getYSize());
    }

    private ItemStack lastItem;
    private TranslatableComponent itemPrice = new TranslatableComponent("msg.vendingmachine.price");
    private TranslatableComponent lastItemPrice = new TranslatableComponent("msg.vendingmachine.price");

    @Override
    protected void renderTooltip(PoseStack matrixStack, ItemStack itemStack, int x, int y) {
        itemPrice = new TranslatableComponent("msg.vendingmachine.price", this.menu.getTile().getPrices().get(itemStack.getItem()));
        if(lastItem != null){
            this.getTooltipFromItem(lastItem).remove(lastItemPrice);
        }
        this.getTooltipFromItem(itemStack).add(itemPrice);
        this.renderTooltip(matrixStack, this.getTooltipFromItem(itemStack), itemStack.getTooltipImage(), x, y, this.font);

        lastItemPrice = itemPrice;
        lastItem = itemStack;
    }

    public Button.OnPress onTestButtonPress = new Button.OnPress() {
        @Override
        public void onPress(Button button) {
            VendingMachine.NETWORK_HANDLER.sendToServer(new PacketAllowItemTake(menu.getTile().getBlockPos(), menu.currentPlayer, !menu.buyTestMode_REMOVE_LATER));
            menu.buyTestMode_REMOVE_LATER = !menu.buyTestMode_REMOVE_LATER;
            menu.checkPlayerAllowedToChangeInv(menu.currentPlayer);
        }
    };

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
        return new Button.OnPress() {
            @Override
            public void onPress(Button button) {
                VendingMachineTile tile = menu.getTile();
                if (!isMain) {
                    VendingMachine.NETWORK_HANDLER.sendToServer(new OpenGUIPacket(tile.getBlockPos(), false));
                    //VendingMachine.NETWORK_HANDLER.sendToServer(new SOpenWindowPacket(menu.containerId, Registration.VENDER_CONT_PRICES.get(), new TranslationTextComponent("gui.vendingmachine.changeprice")));
                }
            }
        };
    }
}
