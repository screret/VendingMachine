package screret.vendingmachine.containers.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.opengl.GL11;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.capabilities.configs.VendingMachineConfig;
import screret.vendingmachine.containers.VenderPriceEditorContainer;
import screret.vendingmachine.events.packets.ChangePricePacket;
import screret.vendingmachine.events.packets.DropMoneyOnClosePacket;
import screret.vendingmachine.events.packets.OpenVenderGUIPacket;
import screret.vendingmachine.tileEntities.VendingMachineTile;

public class VenderBlockPriceScreen extends ContainerScreen<VenderPriceEditorContainer> {
    private boolean scrolling;
    private float scrollOffs;
    private int startIndex;

    private boolean displayPrices;
    private int currentPrice = 4;
    private ItemStack currentItem;
    private ItemStack selectedItem;

    private boolean displayAddMenu;

    private ResourceLocation gui = new ResourceLocation(VendingMachine.MODID, "textures/gui/vending_machine_prices_gui.png");

    public VenderBlockPriceScreen(VenderPriceEditorContainer container, PlayerInventory inv, ITextComponent name) {
        super(container, inv, name);
        this.imageWidth = 176;
        this.imageHeight = 202;
        this.inventoryLabelX = 5;
        this.inventoryLabelY = 128;

        container.registerUpdateListener(this::containerChanged);
    }

    final static int COOK_BAR_XPOS = 49;
    final static  int COOK_BAR_YPOS = 60;

    private Button addPriceMenuButton1;
    private Button addPriceMenuButton2;
    private TextFieldWidget itemNameInput;
    private Slider itemPriceInput;

    private Button addPriceButton;
    private Button delPriceButton;

    private VenderTabButton mainPageButton;
    private VenderTabButton thisPageButton;

    @Override
    public void init(){
        super.init();

        this.displayPrices = menu.hasPricesSet();

        leftPos = (this.width - this.getXSize()) / 2;
        topPos = (this.height - this.getYSize()) / 2;

        addPriceButton = new VenderCustomizableButton(gui, leftPos + 148, topPos + 32, 16, 16, 192, 15, onAddPress);
        delPriceButton = new VenderCustomizableButton(gui, leftPos + 148, topPos + 76, 16, 16, 176, 15, onRemovedPress);

        this.addButton(addPriceButton);
        this.addButton(delPriceButton);

        mainPageButton = new VenderTabButton(leftPos + this.imageWidth, topPos + 2, 32, 28, new TranslationTextComponent("gui.vendingmachine.tab_price"), onTabButtonPress(true), true, false);
        thisPageButton = new VenderTabButton(leftPos + this.imageWidth, topPos + 30, 32, 28, new TranslationTextComponent("gui.vendingmachine.tab_price"), onTabButtonPress(false), false, true);

        this.addButton(mainPageButton);
        this.addButton(thisPageButton);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        leftPos = (this.width - this.getXSize()) / 2;
        topPos = (this.height - this.getYSize()) / 2;
        this.renderBackground(matrixStack);
        this.renderOthers(matrixStack, mouseX, mouseY, partialTicks);
        this.renderButtons(matrixStack, mouseX, mouseY, leftPos + 12, topPos + 21, this.startIndex + 9, partialTicks);

        this.minecraft.getTextureManager().bind(gui);
        int k = (int)(147.0F * this.scrollOffs);
        this.blit(matrixStack, leftPos + 127, topPos + 21 + k, 176 + (this.isScrollBarActive() ? 0 : 12), 0, 12, 15);

        this.addPriceButton.render(matrixStack, mouseX, mouseY, partialTicks);
        this.delPriceButton.render(matrixStack, mouseX, mouseY, partialTicks);
        if(displayAddMenu){
            this.renderAddMenu(matrixStack, mouseX, mouseY, partialTicks);
        }
        this.renderTooltip(matrixStack, mouseX, mouseY);

        this.delPriceButton.active = selectedItem != null && !selectedItem.isEmpty();
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1, 1, 1, 1);
        mainPageButton.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        thisPageButton.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        this.minecraft.getTextureManager().bind(gui);
        this.blit(matrixStack, leftPos, topPos, 0, 0, this.getXSize(), this.getYSize());
    }

    private void renderButtons(MatrixStack matrixStack, int mouseX, int mouseY, int startX, int startY, int lastPos, float partialTicks) {
        Object[] stacks = menu.getTile().getPrices().keySet().toArray();
        Object[] prices = menu.getTile().getPrices().values().toArray();
        for(int i = this.startIndex; i < lastPos && i < menu.getTile().getPrices().size(); ++i) {
            int j = i - this.startIndex;
            int i1 = startY + j * 18;
            int j1 = this.imageHeight;
            int j2 = 0;
            if (i == this.menu.selectedItemIndex) {
                j1 += 17;
                this.selectedItem = new ItemStack((Item)stacks[i]);
            } else if (mouseX >= startX && mouseY >= i1 && mouseX < startY + 16 && mouseY < i1 + 18) {
                j1 += 34;
            }

            this.minecraft.getTextureManager().bind(gui);
            this.blit(matrixStack, startX, i1, j2, j1, 112, 18);
            ITextComponent itemName = ((Item)stacks[i]).getDescription();
            drawCenteredString(matrixStack, minecraft.font, itemName, startX + 56, i1 + 5, getFGColor());
            drawCenteredString(matrixStack, minecraft.font, prices[i].toString(), startX + 104, i1 + 5, getFGColor());
            this.itemRenderer.renderAndDecorateItem(new ItemStack((Item)stacks[i]), startX + 5, i1 + 1);
        }
    }

    public void renderAddMenu(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        this.minecraft.getTextureManager().bind(gui);
        this.blit(matrixStack, leftPos + 22, topPos + 50, 112, 202, 92, 54);

        itemNameInput.render(matrixStack, mouseX, mouseY, partialTicks);
        itemPriceInput.render(matrixStack, mouseX, mouseY, partialTicks);
        addPriceMenuButton1.render(matrixStack, mouseX, mouseY, partialTicks);
        addPriceMenuButton2.render(matrixStack, mouseX, mouseY, partialTicks);

        currentPrice = itemPriceInput.getValueInt();
        if(ResourceLocation.isValidResourceLocation(itemNameInput.getValue())){
            ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemNameInput.getValue())));
            itemRenderer.renderAndDecorateItem(stack, leftPos + 32, topPos + 54);
            currentItem = stack;
        }

        addPriceMenuButton1.active = !currentItem.isEmpty();
    }

    public void removeAddMenu(){
        displayAddMenu = false;
        addPriceMenuButton1.visible = false;
        addPriceMenuButton1.active = false;
        addPriceMenuButton2.visible = false;
        addPriceMenuButton2.active = false;
        itemNameInput.visible = false;
        itemNameInput.active = false;
        itemNameInput.setValue("");
        itemNameInput.setEditable(false);
        itemPriceInput.visible = false;
        itemPriceInput.active = false;
    }

    public void createAddMenu(){
        if(itemNameInput == null){
            itemNameInput = new TextFieldWidget(this.minecraft.font, leftPos + 32, topPos + 54, 64, 16, new StringTextComponent("minecraft:dirt"));
            this.addButton(itemNameInput);
        } else {
            itemPriceInput.x = leftPos + 32;
            itemPriceInput.y = topPos + 54;
            itemNameInput.visible = true;
        }
        itemNameInput.active = true;
        itemNameInput.setEditable(true);

        if(itemPriceInput == null){
            itemPriceInput = new Slider(leftPos + 32, topPos + 70, 64, 16, new TranslationTextComponent("gui.vendingmachine.priceslider"), new StringTextComponent("1"), 1, 64, 4, false, true, emptyPressable);
            this.addButton(itemPriceInput);
        } else {
            itemPriceInput.visible = true;
            itemPriceInput.x = leftPos + 32;
            itemPriceInput.y = topPos + 70;
        }
        itemPriceInput.active = true;

        if(addPriceMenuButton1 == null){
            addPriceMenuButton1 = new Button(leftPos + 32, topPos + 86, 32, 16, new TranslationTextComponent("gui.vendingmachine.addprice"), onAddedPress);
            this.addButton(addPriceMenuButton1);
        } else {
            addPriceMenuButton1.visible = true;
            addPriceMenuButton1.x = leftPos + 32;
            addPriceMenuButton1.y = topPos + 86;
        }

        if(addPriceMenuButton2 == null){
            addPriceMenuButton2 = new Button(leftPos + 72, topPos + 86, 32, 16, new TranslationTextComponent("gui.vendingmachine.cancel"), hideAddMenu);
            this.addButton(addPriceMenuButton2);
        }else {
            addPriceMenuButton2.visible = true;
            addPriceMenuButton2.x = leftPos + 72;
            addPriceMenuButton2.y = topPos + 86;
        }
        addPriceMenuButton2.active = true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int dragType, double p_231045_6_, double p_231045_8_) {
        if (this.scrolling && this.isScrollBarActive()) {
            int i = this.topPos + 21;
            int j = i + 162;
            this.scrollOffs = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.scrollOffs = MathHelper.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int)((double)(this.scrollOffs * (float)this.getOffscreenRows()) + 0.5D) * 4;
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, dragType, p_231045_6_, p_231045_8_);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int dragType) {
        this.scrolling = false;
        if (this.displayPrices) {
            int i = this.leftPos + 12;
            int j = this.topPos + 21;
            int k = this.startIndex + 9;

            for(int l = this.startIndex; l < k; ++l) {
                int i1 = l - this.startIndex;
                double d0 = mouseX - (double)(i);
                double d1 = mouseY - (double)(j + i1 * 18);
                if (d0 >= 0.0D && d1 >= 0.0D && d0 < 112.0D && d1 < 18.0D && this.menu.clickMenuButton(this.minecraft.player, l) && !displayAddMenu) {
                        Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, l);
                        return true;
                }
            }

            i = this.leftPos + 119;
            j = this.topPos + 21;
            if (mouseX >= (double)i && mouseX < (double)(i + 12) && mouseY >= (double)j && mouseY < (double)(j + 162)) {
                this.scrolling = true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, dragType);
    }

    @Override
    public boolean mouseScrolled(double p_231043_1_, double p_231043_3_, double p_231043_5_) {
        if (this.isScrollBarActive()) {
            int i = this.getOffscreenRows();
            this.scrollOffs = (float)((double)this.scrollOffs - p_231043_5_ / (double)i);
            this.scrollOffs = MathHelper.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int)((double)(this.scrollOffs * (float)i) + 0.5D) * 4;
        }

        return true;
    }

    private boolean isScrollBarActive() {
        return this.displayPrices && VendingMachineConfig.GENERAL.allowPriceEditing.get() ? menu.getTile().getPrices().size() > 9 : VendingMachineConfig.DECRYPTED_PRICES.size() > 9;
    }

    protected int getOffscreenRows() {
        return VendingMachineConfig.GENERAL.allowPriceEditing.get() ? menu.getTile().getPrices().size() - 9 : VendingMachineConfig.DECRYPTED_PRICES.size() - 9;
    }

    public int getFGColor() {
        return 16777215; // White
    }

    private void containerChanged() {
        this.displayPrices = this.menu.hasPricesSet();
        if (!this.displayPrices) {
            this.scrollOffs = 0.0F;
            this.startIndex = 0;
        }
    }

    @Override
    public boolean keyPressed(int key, int p_keyPressed_2_, int p_keyPressed_3_) {
        if ((itemNameInput != null && itemNameInput.isFocused()) && key != 256) {
            return itemNameInput.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_);
        }else{
            return super.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        VendingMachine.NETWORK_HANDLER.sendToServer(new DropMoneyOnClosePacket(this.menu.getTile().getBlockPos()));
    }

    public void renderOthers(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        int i = this.leftPos;
        int j = this.topPos;
        this.renderBg(matrixStack, partialTicks, mouseX, mouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiContainerEvent.DrawBackground(this, matrixStack, mouseX, mouseY));
        RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)i, (float)j, 0.0F);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableRescaleNormal();
        RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.renderLabels(matrixStack, mouseX, mouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiContainerEvent.DrawForeground(this, matrixStack, mouseX, mouseY));

        RenderSystem.popMatrix();
        RenderSystem.enableDepthTest();
    }

    protected void renderLabels(MatrixStack p_230451_1_, int p_230451_2_, int p_230451_3_) {
        this.font.draw(p_230451_1_, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        //this.font.draw(p_230451_1_, this.inventory.getDisplayName(), (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
    }

    public Button.IPressable onTabButtonPress(boolean isMain){
        return new Button.IPressable() {
            @Override
            public void onPress(Button button) {
                VendingMachineTile tile = menu.getTile();
                VendingMachine.NETWORK_HANDLER.sendToServer(new OpenVenderGUIPacket(tile.getBlockPos(), !isMain));
            }
        };
    }

    public Button.IPressable onAddPress = new Button.IPressable() {
        @Override
        public void onPress(Button button) {
            displayAddMenu = true;
            createAddMenu();
        }
    };

    public Button.IPressable onAddedPress = new Button.IPressable() {
        @Override
        public void onPress(Button button) {
            VendingMachineTile tile = menu.getTile();
            VendingMachine.NETWORK_HANDLER.sendToServer(new ChangePricePacket(tile.getBlockPos(), currentItem, currentPrice, true, Minecraft.getInstance().player.getUUID()));
            removeAddMenu();
            tile.addPrice(currentItem, currentPrice);
            menu.updateGUI();
        }
    };

    public Button.IPressable onRemovedPress = new Button.IPressable() {
        @Override
        public void onPress(Button button) {
            if(selectedItem != null && !selectedItem.isEmpty()){
                VendingMachineTile tile = menu.getTile();
                VendingMachine.NETWORK_HANDLER.sendToServer(new ChangePricePacket(tile.getBlockPos(), selectedItem, currentPrice, false, Minecraft.getInstance().player.getUUID()));
                tile.removePrice(selectedItem);
                menu.updateGUI();
            }
        }
    };

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