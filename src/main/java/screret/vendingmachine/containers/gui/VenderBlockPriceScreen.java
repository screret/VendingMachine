package screret.vendingmachine.containers.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;
import org.lwjgl.opengl.GL11;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.events.packets.OpenGUIPacket;
import screret.vendingmachine.events.packets.PacketAllowItemTake;
import screret.vendingmachine.events.packets.PacketSendBuy;
import screret.vendingmachine.init.Registration;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import java.io.StringReader;

public class VenderBlockPriceScreen extends ContainerScreen<VenderBlockContainer> {
    int relX, relY;

    public int tab;

    private ResourceLocation widgets = new ResourceLocation(VendingMachine.MODID, "textures/gui/widgets.png");
    private ResourceLocation gui = new ResourceLocation(VendingMachine.MODID, "textures/gui/vending_machine_gui.png");

    public VenderBlockPriceScreen(VenderBlockContainer container, PlayerInventory inv, ITextComponent name) {
        super(container, inv, name);
        this.imageWidth = 174;
        this.imageHeight = 222;
        this.inventoryLabelX = 5;
        this.inventoryLabelY = 128;
        relX = (this.width - this.getXSize()) / 2;
        relY = (this.height - this.getYSize()) / 2;
    }

    final static int COOK_BAR_XPOS = 49;
    final static  int COOK_BAR_YPOS = 60;

    @Override
    public void init(){
        super.init();

        relX = (this.width - this.getXSize()) / 2;
        relY = (this.height - this.getYSize()) / 2;

        this.addButton(new VenderTabButton(relX + this.imageWidth, relY, 32, 28, new TranslationTextComponent("gui.vendingmachine.tab_price"), onTabButtonPress(true), true, false));
        this.addButton(new VenderTabButton(relX + this.imageWidth, relY + 28, 32, 28, new TranslationTextComponent("gui.vendingmachine.tab_price"), onTabButtonPress(false), false, true));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);

        if(menu.selectedSlot != null && !menu.isAllowedToTakeItems){
            fillGradient(matrixStack, relX + menu.selectedSlot.x, relY + menu.selectedSlot.y, relX + menu.selectedSlot.x + 16, relY + menu.selectedSlot.y + 16, 0x7500FF00, 0x75009900);
        }
    }

    public void tick() {
        super.tick();
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1, 1, 1, 1);
        this.minecraft.getTextureManager().bind(gui);
        relX = (this.width - this.getXSize()) / 2;
        relY = (this.height - this.getYSize()) / 2;
        this.blit(matrixStack, relX, relY, 0, 0, this.getXSize(), this.getYSize());
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
            VendingMachine.NETWORK_HANDLER.sendToServer(new PacketSendBuy(menu.getTile().getBlockPos(), menu.selectedSlot.getSlotIndex()));
        }
    }; //VendingMachine.SIMPLE_CHANNEL.sendToServer(new PacketSendBuy(menu.selectedSlot)); };

    public Button.IPressable onTabButtonPress(boolean isMain){
        return new Button.IPressable() {
            @Override
            public void onPress(Button button) {
                VendingMachineTile tile = menu.getTile();
                if (isMain) {
                    VendingMachine.NETWORK_HANDLER.sendToServer(new OpenGUIPacket(tile.getBlockPos(), true));
                    //VendingMachine.NETWORK_HANDLER.sendToServer(new SOpenWindowPacket(menu.containerId, Registration.VENDER_CONT_PRICES.get(), new TranslationTextComponent("gui.vendingmachine.changeprice")));
                }
            }
        };
    }
}
