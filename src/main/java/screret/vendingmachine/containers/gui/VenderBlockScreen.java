package screret.vendingmachine.containers.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.opengl.GL11;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.capabilities.configs.VendingMachineConfig;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.events.packets.OpenVenderGUIPacket;
import screret.vendingmachine.events.packets.PacketAllowItemTake;
import screret.vendingmachine.events.packets.PacketSendBuy;
import screret.vendingmachine.tileEntities.VendingMachineTile;

public class VenderBlockScreen extends ContainerScreen<VenderBlockContainer> {

    private final ResourceLocation widgets = new ResourceLocation(VendingMachine.MODID, "textures/gui/widgets.png");
    private final ResourceLocation gui = new ResourceLocation(VendingMachine.MODID, "textures/gui/vending_machine_gui.png");

    public VenderBlockScreen(VenderBlockContainer container, PlayerInventory inv, ITextComponent name) {
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

        this.addButton(new Button(leftPos + 110, topPos + 108, 53, 18, new TranslationTextComponent("gui.vendingmachine.buybutton"), onBuyButtonPress));
        //this.addButton(new Button(leftPos + 133, topPos + 64, 18, 9, new TranslationTextComponent("gui.vendingmachine.buytestbutton"), onTestButtonPress));

        if(menu.currentPlayer.equals(menu.getTile().owner)){
            this.addButton(new VenderTabButton(leftPos + this.imageWidth, topPos + 12, 32, 28, new TranslationTextComponent("gui.vendingmachine.tab_price"), onTabButtonPress(true), true, true));
            this.addButton(new VenderTabButton(leftPos + this.imageWidth, topPos + 40, 32, 28, new TranslationTextComponent("gui.vendingmachine.tab_price"), onTabButtonPress(false), false, false));
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);

        if(menu.selectedSlot != null && !menu.isAllowedToTakeItems){
            fillGradient(matrixStack, leftPos + menu.selectedSlot.x, topPos + menu.selectedSlot.y, leftPos + menu.selectedSlot.x + 16, topPos + menu.selectedSlot.y + 16, 0x7500FF00, 0x75009900);
        }
    }

    public void tick() {
        super.tick();
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1, 1, 1, 1);
        this.minecraft.getTextureManager().bind(gui);
        leftPos = (this.width - this.getXSize()) / 2;
        topPos = (this.height - this.getYSize()) / 2;
        this.blit(matrixStack, leftPos, topPos, 0, 0, this.getXSize(), this.getYSize());
    }

    private ItemStack lastItem;
    private TranslationTextComponent itemPrice = new TranslationTextComponent("msg.vendingmachine.price");
    private TranslationTextComponent lastItemPrice = new TranslationTextComponent("msg.vendingmachine.price");

    @Override
    protected void renderTooltip(MatrixStack matrixStack, ItemStack itemStack, int x, int y) {
        itemPrice = new TranslationTextComponent("msg.vendingmachine.price", this.menu.getTile().getPrices().get(itemStack.getItem()));
        FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
        net.minecraftforge.fml.client.gui.GuiUtils.preItemToolTip(itemStack);
        if(lastItem != null){
            this.getTooltipFromItem(lastItem).remove(lastItemPrice);
        }
        this.getTooltipFromItem(itemStack).add(itemPrice);
        this.renderWrappedToolTip(matrixStack, this.getTooltipFromItem(itemStack), x, y, (font == null ? this.font : font));
        net.minecraftforge.fml.client.gui.GuiUtils.postItemToolTip();

        lastItemPrice = itemPrice;
        lastItem = itemStack;
    }

    public Button.IPressable onTestButtonPress = new Button.IPressable() {
        @Override
        public void onPress(Button button) {
            VendingMachine.NETWORK_HANDLER.sendToServer(new PacketAllowItemTake(menu.getTile().getBlockPos(), menu.currentPlayer, !menu.buyTestMode_REMOVE_LATER));
            menu.buyTestMode_REMOVE_LATER = !menu.buyTestMode_REMOVE_LATER;
            menu.checkPlayerAllowedToChangeInv(menu.currentPlayer);
        }
    };

    public final Button.IPressable onBuyButtonPress = button -> {
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

    public Button.IPressable onTabButtonPress(boolean isMain){
        return new Button.IPressable() {
            @Override
            public void onPress(Button button) {
                VendingMachineTile tile = menu.getTile();
                VendingMachine.NETWORK_HANDLER.sendToServer(new OpenVenderGUIPacket(tile.getBlockPos(), !isMain));
            }
        };
    }
}
