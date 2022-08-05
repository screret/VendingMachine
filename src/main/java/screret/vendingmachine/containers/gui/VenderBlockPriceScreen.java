package screret.vendingmachine.containers.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.containers.VenderBlockContainer;
import screret.vendingmachine.containers.VenderPriceEditorContainer;
import screret.vendingmachine.events.packets.ChangePricePacket;
import screret.vendingmachine.events.packets.OpenVenderGUIPacket;
import screret.vendingmachine.tileEntities.VendingMachineTile;

import javax.annotation.Nullable;

public class VenderBlockPriceScreen extends AbstractContainerScreen<VenderPriceEditorContainer> {
    private int currentPrice = 0;

    private final ResourceLocation buttonGui = new ResourceLocation(VendingMachine.MODID, "textures/gui/vending_machine_prices_gui.png");
    private final ResourceLocation gui = new ResourceLocation(VendingMachine.MODID, "textures/gui/vending_machine_gui.png");

    public VenderBlockPriceScreen(VenderPriceEditorContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelX = 5;
        this.inventoryLabelY = 128;
    }

    private Button addPriceMenuButton1;
    private Button addPriceMenuButton2;
    private EditBox itemPriceInput;
    private ItemStack selectedItem;

    private VenderTabButton mainPageButton;
    private VenderTabButton thisPageButton;

    private boolean renderPriceMenu = false;

    @Override
    public void init(){
        super.init();

        leftPos = (this.width - this.getXSize()) / 2;
        topPos = (this.height - this.getYSize()) / 2;

        mainPageButton = new VenderTabButton(leftPos + this.imageWidth, topPos + 2, 32, 28, new TranslatableComponent("gui.vendingmachine.tab_price"), onTabButtonPress(true), true, false);
        thisPageButton = new VenderTabButton(leftPos + this.imageWidth, topPos + 30, 32, 28, new TranslatableComponent("gui.vendingmachine.tab_price"), onTabButtonPress(false), false, true);

        this.addRenderableWidget(mainPageButton);
        this.addRenderableWidget(thisPageButton);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);

        if(this.menu.selectedSlot != null && menu.isAllowedToTakeItems){
            fillGradient(poseStack, leftPos + menu.selectedSlot.x, topPos + menu.selectedSlot.y, leftPos + menu.selectedSlot.x + 16, topPos + menu.selectedSlot.y + 16, 0x7500FF00, 0x75009900);
        }

        this.renderButtons(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    private void renderButtons(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if(renderPriceMenu){
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, this.buttonGui);
            this.blit(poseStack, leftPos + 22, topPos + 50, 112, 202, 92, 54);
            addPriceMenuButton1.renderButton(poseStack, mouseX, mouseY, partialTicks);
            addPriceMenuButton2.renderButton(poseStack, mouseX, mouseY, partialTicks);
            itemPriceInput.renderButton(poseStack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, ItemStack stack, int mouseX, int mouseY) {
        var tooltip = this.getTooltipFromItem(stack);
        tooltip.add(1, new TranslatableComponent("msg.vendingmachine.price", this.menu.getTile().getPrices().get(stack.getItem())));
        tooltip.add(2, new TranslatableComponent("msg.vendingmachine.rightclickprice"));
        this.renderTooltip(poseStack, this.getTooltipFromItem(stack), stack.getTooltipImage(), mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        mainPageButton.renderButton(poseStack, mouseX, mouseY, partialTicks);
        thisPageButton.renderButton(poseStack, mouseX, mouseY, partialTicks);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.gui);
        this.blit(poseStack, leftPos, topPos, 0, 0, this.getXSize(), this.getYSize());
        this.blit(poseStack, leftPos + 133, topPos + 35, 98, 4, 18, 18);
        this.blit(poseStack, leftPos + 133, topPos + 89, 98, 4, 18, 18);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        Slot slot = findSlot(mouseX, mouseY);
        if(slot != null && (itemPriceInput == null || !itemPriceInput.isActive()) && slot.index > VenderBlockContainer.INPUT_SLOTS_X_AMOUNT_PLUS_1 * VenderBlockContainer.INPUT_SLOTS_Y_AMOUNT_PLUS_1){
            selectedItem = slot.getItem();
            if(selectedItem != null && selectedItem != ItemStack.EMPTY){
                if(mouseButton == 0){
                    menu.selectedSlot = (SlotItemHandler) slot;
                    createAddMenu();
                }else if(mouseButton == 1){
                    VendingMachineTile tile = menu.getTile();
                    VendingMachine.NETWORK_HANDLER.sendToServer(new ChangePricePacket(tile.getBlockPos(), selectedItem, currentPrice, false));
                    tile.removePrice(selectedItem);
                }

            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void removeAddMenu(){
        renderPriceMenu = false;
        addPriceMenuButton1.visible = false;
        addPriceMenuButton1.active = false;
        addPriceMenuButton2.visible = false;
        addPriceMenuButton2.active = false;
        itemPriceInput.setValue("0");
        itemPriceInput.setEditable(false);
        itemPriceInput.visible = false;
        itemPriceInput.active = false;
    }

    public void createAddMenu(){
        renderPriceMenu = true;
        if(itemPriceInput == null){
            itemPriceInput = new EditBox(this.minecraft.font, leftPos + 32, topPos + 54, 64, 16, new TextComponent(""));
            itemPriceInput.setFilter(text -> text.matches("[0-9]+"));
            this.addWidget(itemPriceInput);
            itemPriceInput.setMaxLength(8);
        } else {
            itemPriceInput.visible = true;
        }
        itemPriceInput.active = true;
        itemPriceInput.setEditable(true);

        if(addPriceMenuButton1 == null){
            addPriceMenuButton1 = new Button(leftPos + 32, topPos + 76, 32, 16, new TranslatableComponent("gui.vendingmachine.addprice"), onAddedPress);
            this.addWidget(addPriceMenuButton1);
        } else {
            addPriceMenuButton1.visible = true;
            addPriceMenuButton1.x = leftPos + 32;
            addPriceMenuButton1.y = topPos + 86;
        }
        addPriceMenuButton1.active = true;

        if(addPriceMenuButton2 == null){
            addPriceMenuButton2 = new Button(leftPos + 72, topPos + 76, 32, 16, new TranslatableComponent("gui.vendingmachine.cancel"), hideAddMenu);
            this.addWidget(addPriceMenuButton2);
        }else {
            addPriceMenuButton2.visible = true;
            addPriceMenuButton2.x = leftPos + 72;
            addPriceMenuButton2.y = topPos + 86;
        }
        addPriceMenuButton2.active = true;
    }

    @Override
    public boolean keyPressed(int key, int p_keyPressed_2_, int p_keyPressed_3_) {
        if ((itemPriceInput != null && itemPriceInput.isFocused()) && key != 256) {
            return itemPriceInput.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_);
        }else{
            return super.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_);
        }
    }

    @Nullable
    private Slot findSlot(double p_97745_, double p_97746_) {
        for(int i = 0; i < this.menu.slots.size(); ++i) {
            Slot slot = this.menu.slots.get(i);
            if (this.isHovering(slot, p_97745_, p_97746_) && slot.isActive()) {
                return slot;
            }
        }

        return null;
    }
    private boolean isHovering(Slot p_97775_, double p_97776_, double p_97777_) {
        return this.isHovering(p_97775_.x, p_97775_.y, 16, 16, p_97776_, p_97777_);
    }

    public Button.OnPress onTabButtonPress(boolean isMain){
        return new Button.OnPress() {
            @Override
            public void onPress(Button button) {
                VendingMachineTile tile = menu.getTile();
                if (isMain) {
                    VendingMachine.NETWORK_HANDLER.sendToServer(new OpenVenderGUIPacket(tile.getBlockPos(), true));
                    //VendingMachine.NETWORK_HANDLER.sendToServer(new SOpenWindowPacket(menu.containerId, Registration.VENDER_CONT_PRICES.get(), new TranslationTextComponent("gui.vendingmachine.changeprice")));
                }
            }
        };
    }

    public Button.OnPress onAddedPress = new Button.OnPress() {
        @Override
        public void onPress(Button button) {
            if(menu.selectedSlot != null){
                currentPrice = Integer.parseInt(itemPriceInput.getValue());
                VendingMachineTile tile = menu.getTile();
                VendingMachine.NETWORK_HANDLER.sendToServer(new ChangePricePacket(tile.getBlockPos(), menu.selectedSlot.getItem(), currentPrice, true));
                removeAddMenu();
                tile.addPrice(selectedItem, currentPrice);
            }

        }
    };

    public Button.OnPress hideAddMenu = new Button.OnPress() {
        @Override
        public void onPress(Button button) {
            removeAddMenu();
        }
    };
}