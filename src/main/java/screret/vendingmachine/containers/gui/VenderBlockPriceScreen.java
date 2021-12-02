package screret.vendingmachine.containers.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;
import org.lwjgl.opengl.GL11;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.configs.VendingMachineConfig;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.containers.VenderPriceEditorContainer;
import screret.vendingmachine.events.packets.AddPricePacket;
import screret.vendingmachine.events.packets.OpenGUIPacket;
import screret.vendingmachine.tileEntities.VendingMachineTile;

public class VenderBlockPriceScreen extends ContainerScreen<VenderPriceEditorContainer> {
    int relX, relY;

    private boolean scrolling;
    private float scrollOffs;
    private int startIndex;

    private boolean displayPrices;
    private int currentPrice = 4;

    private boolean displayAddMenu;

    private ResourceLocation gui = new ResourceLocation(VendingMachine.MODID, "textures/gui/vending_machine_prices_gui.png");

    public VenderBlockPriceScreen(VenderPriceEditorContainer container, PlayerInventory inv, ITextComponent name) {
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

    private Button addPriceMenuButton1;
    private Button addPriceMenuButton2;
    private TextFieldWidget itemNameInput;
    private Slider itemPriceInput;

    @Override
    public void init(){
        super.init();

        relX = (this.width - this.getXSize()) / 2;
        relY = (this.height - this.getYSize()) / 2;

        this.addButton(new Button(relX + 148, relY + 32, 16, 16, new TranslationTextComponent("gui.vendingmachine.addprice"), onAddPress()));

        this.addButton(new VenderTabButton(relX + this.imageWidth, relY + 2, 32, 28, new TranslationTextComponent("gui.vendingmachine.tab_price"), onTabButtonPress(true), true, false));
        this.addButton(new VenderTabButton(relX + this.imageWidth, relY + 30, 32, 28, new TranslationTextComponent("gui.vendingmachine.tab_price"), onTabButtonPress(false), false, true));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
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
        this.renderButtons(matrixStack, mouseX, mouseY, relX + 11, relY + 21, this.startIndex + 24);
        if(displayAddMenu){
            this.renderAddMenu(matrixStack, mouseX, mouseY);
        }
    }

    private void renderButtons(MatrixStack matrixStack, int mouseX, int mouseY, int start, int end, int lastPos) {
        for(int i = this.startIndex; i < lastPos && i < VendingMachineConfig.DECRYPTED_PRICES.size(); ++i) {
            int j = i - this.startIndex;
            int l = j / 4;
            int i1 = end + l * 18 + 2;
            int j1 = 222;
            if (i == this.menu.selectedItemIndex) {
                j1 += 18;
            } /*else if (mouseX >= start && mouseY >= i1 && mouseX < start + 16 && mouseY < i1 + 18) {
                j1 += 36;
            }*/

            this.blit(matrixStack, start, i1 - 1, 0, j1, 111, 18);
        }
    }

    public void renderAddMenu(MatrixStack matrixStack, int mouseX, int mouseY){
        this.minecraft.getTextureManager().bind(gui);
        this.blit(matrixStack, relX + 28, relY + 50, 176, 62, 80, 52);
    }

    public void removeAddMenu(){
        displayAddMenu = false;
        addPriceMenuButton1.visible = false;
        addPriceMenuButton2.visible = false;
        itemNameInput.visible = false;
        itemNameInput.setValue("");
        itemPriceInput.visible = false;
    }

    public void addAddMenu(){
        if(itemNameInput == null){
            itemNameInput = new TextFieldWidget(minecraft.font, relX + 32, relY + 54, 64, 16, new TranslationTextComponent("gui.vendingmachine.inputtemplate"));
            this.addButton(itemNameInput);
            this.setInitialFocus(itemNameInput);
        } else {
            itemNameInput.setVisible(true);
            itemNameInput.visible = true;
        }
        itemNameInput.setFocus(true);
        itemNameInput.active = true;
        itemNameInput.setEditable(true);

        if(itemPriceInput == null){
            itemPriceInput = new Slider(relX + 32, relY + 70, 64, 16, new TranslationTextComponent("gui.vendingmachine.priceslider"), new StringTextComponent("1"), 1, 64, 4, false, true, emptyPressable);
            this.addButton(itemPriceInput);
        } else {
            itemPriceInput.visible = true;
        }

        if(addPriceMenuButton1 == null){
            addPriceMenuButton1 = new Button(relX + 38, relY + 86, 32, 16, new TranslationTextComponent("gui.vendingmachine.addprice"), onAddedPress(Registry.ITEM.get(new ResourceLocation(itemNameInput.getValue())), Math.round(Math.round(itemPriceInput.sliderValue))));
            this.addButton(addPriceMenuButton1);
        } else {
            addPriceMenuButton1.visible = true;
        }

        if(addPriceMenuButton2 == null){
            addPriceMenuButton2 = new Button(relX + 76, relY + 86, 32, 16, new TranslationTextComponent("gui.vendingmachine.cancel"), hideAddMenu);
            this.addButton(addPriceMenuButton2);
        }else {
            addPriceMenuButton2.visible = true;
        }

        VenderBlockContainer.LOGGER.info(menu.getTile().priceHashMap);
    }

    public boolean mouseDragged(double p_231045_1_, double p_231045_3_, int p_231045_5_, double p_231045_6_, double p_231045_8_) {
        if (this.scrolling && this.isScrollBarActive()) {
            int i = this.topPos + 14;
            int j = i + 54;
            this.scrollOffs = ((float)p_231045_3_ - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.scrollOffs = MathHelper.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int)((double)(this.scrollOffs * (float)this.getOffscreenRows()) + 0.5D) * 4;
            return true;
        } else {
            return super.mouseDragged(p_231045_1_, p_231045_3_, p_231045_5_, p_231045_6_, p_231045_8_);
        }
    }

    @Override
    public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
        this.scrolling = false;
        if (this.displayPrices) {
            int i = this.leftPos + 52;
            int j = this.topPos + 14;
            int k = this.startIndex + 12;

            for(int l = this.startIndex; l < k; ++l) {
                int i1 = l - this.startIndex;
                double d0 = p_231044_1_ - (double)(i + i1 % 4 * 16);
                double d1 = p_231044_3_ - (double)(j + i1 / 4 * 18);
                if (d0 >= 0.0D && d1 >= 0.0D && d0 < 16.0D && d1 < 18.0D && this.menu.clickMenuButton(this.minecraft.player, l)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, l);
                    return true;
                }
            }

            i = this.leftPos + 119;
            j = this.topPos + 9;
            if (p_231044_1_ >= (double)i && p_231044_1_ < (double)(i + 12) && p_231044_3_ >= (double)j && p_231044_3_ < (double)(j + 54)) {
                this.scrolling = true;
            }
        }

        return super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
    }

    private boolean isScrollBarActive() {
        return this.displayPrices && VendingMachineConfig.DECRYPTED_PRICES.size() > 12;
    }

    protected int getOffscreenRows() {
        return (VendingMachineConfig.DECRYPTED_PRICES.size() + 4 - 1) / 4 - 3;
    }

    private void containerChanged() {
        this.displayPrices = this.menu.hasPricesSet();
        if (!this.displayPrices) {
            this.scrollOffs = 0.0F;
            this.startIndex = 0;
        }

    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (itemNameInput.isFocused()) {
            return itemNameInput.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }else{
            return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
    }

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

    public Button.IPressable onAddPress(){
        return new Button.IPressable() {
            @Override
            public void onPress(Button button) {
                displayAddMenu = true;
                addAddMenu();
            }
        };
    }

    public Button.IPressable onAddedPress(Item item, int price){
        return new Button.IPressable() {
            @Override
            public void onPress(Button button) {
                VendingMachineTile tile = menu.getTile();
                VendingMachine.NETWORK_HANDLER.sendToServer(new AddPricePacket(tile.getBlockPos(), item, price));
                removeAddMenu();
                tile.priceHashMap.put(item, price);
            }
        };
    }

    public Button.IPressable emptyPressable = new Button.IPressable() {
        @Override
        public void onPress(Button button) {

        }
    };

    public Button.IPressable hideAddMenu = new Button.IPressable() {
        @Override
        public void onPress(Button button) {
            removeAddMenu();
        }
    };
}