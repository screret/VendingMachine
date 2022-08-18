package screret.vendingmachine.containers.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.configs.VendingMachineConfig;
import screret.vendingmachine.containers.VenderBlockMenu;
import screret.vendingmachine.events.packets.PacketCashOutC2S;
import screret.vendingmachine.events.packets.OpenGuiPacketC2S;
import screret.vendingmachine.events.packets.PacketBuyC2S;
import screret.vendingmachine.blockEntities.VendingMachineBlockEntity;
import screret.vendingmachine.util.Util;

public class VenderBlockScreen extends AbstractContainerScreen<VenderBlockMenu> {
    private ResourceLocation widgets = new ResourceLocation(VendingMachine.MODID, "textures/gui/widgets.png");
    private static final ResourceLocation GUI = new ResourceLocation(VendingMachine.MODID, "textures/gui/vending_machine_gui.png");

    public VenderBlockScreen(VenderBlockMenu container, Inventory inv, Component name) {
        super(container, inv, name);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelX = 5;
        this.inventoryLabelY = 128;
    }

    @Override
    public void init(){
        super.init();

        leftPos = (this.width - this.imageWidth) / 2;
        topPos = (this.height - this.imageHeight) / 2;

        this.insertedMoneyTextX = leftPos + 136;
        this.insertedMoneyTextY = topPos + 16;

        this.addRenderableWidget(new Button(leftPos + 109, topPos + 108, 53, 18, new TranslatableComponent("gui.vendingmachine.button_buy"), onBuyButtonPress));
        //this.addRenderableWidget(new Button(leftPos + 133, topPos + 64, 18, 9, new TranslatableComponent("gui.vendingmachine.buytestbutton"), onTestButtonPress));

        if(menu.checkPlayerAllowedToChangeInv(menu.currentPlayer)){
            this.addRenderableWidget(new VenderTabButton(leftPos + this.imageWidth, topPos + 2, 32, 28, new TranslatableComponent("gui.vendingmachine.button_tab_main"), onTabButtonPress(true), true, true));
            this.addRenderableWidget(new VenderTabButton(leftPos + this.imageWidth, topPos + 30, 32, 28, new TranslatableComponent("gui.vendingmachine.button_tab_price"), onTabButtonPress(false), false, false));
            this.addRenderableWidget(new Button(leftPos + 127, topPos + 60, 16, 8, TextComponent.EMPTY, onCashOutButtonPress, onCashOutButtonTooltip));
        }
    }

    private int insertedMoneyTextX, insertedMoneyTextY;
    private boolean oldFullscreenState = true;

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if(oldFullscreenState != this.minecraft.options.fullscreen){
            oldFullscreenState = this.minecraft.options.fullscreen;
            leftPos = (this.width - this.imageWidth) / 2;
            topPos = (this.height - this.imageHeight) / 2;
            this.insertedMoneyTextX = leftPos + 136;
            this.insertedMoneyTextY = topPos + 16;
        }

        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);

        if(menu.selectedSlot != null && !menu.isAllowedToTakeItems){
            fillGradient(poseStack, leftPos + menu.selectedSlot.x, topPos + menu.selectedSlot.y, leftPos + menu.selectedSlot.x + 16, topPos + menu.selectedSlot.y + 16, 0x7500FF00, 0x75009900);
        }

        FormattedCharSequence sequence = new TranslatableComponent("gui.vendingmachine.inserted_money", Util.DECIMAL_FORMAT.format(menu.getTile().currentPlayerInsertedMoney)).getVisualOrderText();
        this.font.draw(poseStack, sequence, (float)(insertedMoneyTextX - this.font.width(sequence) / 2), (float)insertedMoneyTextY, 0x404040);

        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI);
        this.blit(poseStack, leftPos, topPos, 0, 0, this.getXSize(), this.getYSize());
    }

    @Override
    protected void renderTooltip(PoseStack matrixStack, ItemStack itemStack, int x, int y) {
        Object price = this.menu.getTile().getPrices().get(itemStack.getItem());
        var tooltip = this.getTooltipFromItem(itemStack);
        if(this.hoveredSlot.index < VenderBlockMenu.LAST_CONTAINER_SLOT_INDEX) tooltip.add(1, new TranslatableComponent("tooltip.vendingmachine.price", Util.DECIMAL_FORMAT.format(price == null ? 0 : price), VendingMachineConfig.GENERAL.isStackPrices.get() ? itemStack.getMaxStackSize() : 1));

        this.renderTooltip(matrixStack, tooltip, itemStack.getTooltipImage(), x, y, this.font);
    }

    public final Button.OnPress onBuyButtonPress = button -> {
        //menu.getTile().buy(menu.selectedSlot.getSlotIndex());
        if(menu.selectedSlot != null) {
            if(hasShiftDown() && !VendingMachineConfig.GENERAL.isStackPrices.get()){
                VendingMachine.NETWORK_HANDLER.sendToServer(new PacketBuyC2S(menu.getTile().getBlockPos(), menu.selectedSlot.getSlotIndex(), 64));
            }else if(!VendingMachineConfig.GENERAL.isStackPrices.get()){
                VendingMachine.NETWORK_HANDLER.sendToServer(new PacketBuyC2S(menu.getTile().getBlockPos(), menu.selectedSlot.getSlotIndex(), 1));
            }else {
                VendingMachine.NETWORK_HANDLER.sendToServer(new PacketBuyC2S(menu.getTile().getBlockPos(), menu.selectedSlot.getSlotIndex(), 64));
            }
        }
    }; //VendingMachine.SIMPLE_CHANNEL.sendToServer(new PacketBuyC2S(menu.selectedSlot)); };

    private Button.OnPress onTabButtonPress(boolean isMain){
        return button -> {
            VendingMachineBlockEntity tile = menu.getTile();
            if (!isMain) {
                VendingMachine.NETWORK_HANDLER.sendToServer(new OpenGuiPacketC2S(tile.getBlockPos(), false));
            }
        };
    }

    private Button.OnPress onCashOutButtonPress = button -> {
            VendingMachineBlockEntity tile = menu.getTile();
            VendingMachine.NETWORK_HANDLER.sendToServer(new PacketCashOutC2S(tile.getBlockPos()));
        };

    private static final Component buttonTooltip = new TranslatableComponent("tooltip.vendingmachine.cash_out");

    private Button.OnTooltip onCashOutButtonTooltip = (button, poseStack, x, y) -> {
        VenderBlockScreen.super.renderTooltip(poseStack, buttonTooltip, x, y);
    };

}
