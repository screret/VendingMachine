package screret.vendingmachine.containers.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import screret.vendingmachine.VendingMachine;
import screret.vendingmachine.configs.VendingMachineConfig;
import screret.vendingmachine.containers.VenderBlockMenu;
import screret.vendingmachine.containers.VenderPriceEditorMenu;
import screret.vendingmachine.events.packets.ChangePricePacket;
import screret.vendingmachine.events.packets.OpenVenderGUIPacket;
import screret.vendingmachine.blockEntities.VendingMachineBlockEntity;
import screret.vendingmachine.items.MoneyItem;

import javax.annotation.Nullable;

public class VenderBlockPriceScreen extends AbstractContainerScreen<VenderPriceEditorMenu> {
    private int currentPrice = 0;
    private ItemStack selectedItem;

    private final ResourceLocation buttonGui = new ResourceLocation(VendingMachine.MODID, "textures/gui/vending_machine_prices_gui.png");
    private final ResourceLocation gui = new ResourceLocation(VendingMachine.MODID, "textures/gui/vending_machine_gui.png");

    private Button addPriceMenuButtonContinue;
    private Button addPriceMenuButtonCancel;
    private EditBox itemPriceInput;

    private VenderTabButton mainPageButton;
    private VenderTabButton thisPageButton;

    private boolean renderPriceMenu = false;

    public VenderBlockPriceScreen(VenderPriceEditorMenu container, Inventory inv, Component name) {
        super(container, inv, name);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelX = 5;
        this.inventoryLabelY = 128;
    }

    @Override
    public void init(){
        super.init();

        leftPos = (this.width - this.getXSize()) / 2;
        topPos = (this.height - this.getYSize()) / 2;

        mainPageButton = new VenderTabButton(leftPos + this.imageWidth, topPos + 2, 32, 28, Component.translatable("gui.vendingmachine.tab_price"), onTabButtonPress(true), true, false);
        thisPageButton = new VenderTabButton(leftPos + this.imageWidth, topPos + 30, 32, 28, Component.translatable("gui.vendingmachine.tab_price"), onTabButtonPress(false), false, true);

        this.addRenderableWidget(mainPageButton);
        this.addRenderableWidget(thisPageButton);
    }
    private boolean oldFullscreenState = true;

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if(oldFullscreenState != this.minecraft.options.fullscreen().get()){
            oldFullscreenState = this.minecraft.options.fullscreen().get();
            leftPos = (this.width - this.imageWidth) / 2;
            topPos = (this.height - this.imageHeight) / 2;
            if(renderPriceMenu){
                createAddMenu(itemPriceInput.getValue());
            }
        }

        this.renderBackground(poseStack);
        this.itemRenderer.blitOffset = this.itemRenderer.blitOffset - 200f;
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.itemRenderer.blitOffset = this.itemRenderer.blitOffset + 200f;


        if(this.menu.selectedSlot != null && menu.isAllowedToTakeItems){
            fillGradient(poseStack, leftPos + menu.selectedSlot.x, topPos + menu.selectedSlot.y, leftPos + menu.selectedSlot.x + 16, topPos + menu.selectedSlot.y + 16, 0x7500FF00, 0x75009900);
        }

        this.renderButtons(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    private static final Component addPriceGuiName = Component.translatable("gui.vendingmachine.button_add_price");

    private void renderButtons(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if(renderPriceMenu){
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, this.buttonGui);

            //this.setBlitOffset(this.getBlitOffset() + 10);
            poseStack.translate(0.0D, 0.0D, 300.0D);
            this.blit(poseStack, leftPos + 22, topPos + 50, 112, 202, 92, 54);
            addPriceMenuButtonContinue.renderButton(poseStack, mouseX, mouseY, partialTicks);
            addPriceMenuButtonCancel.renderButton(poseStack, mouseX, mouseY, partialTicks);
            itemPriceInput.renderButton(poseStack, mouseX, mouseY, partialTicks);
            this.font.draw(poseStack, addPriceGuiName, leftPos + 32, topPos + 54, 0xFF404040);
            //this.setBlitOffset(this.getBlitOffset() - 10);
            poseStack.translate(0.0D, 0.0D, -300.0D);

            if(itemPriceInput.getValue().length() > 0 && !itemPriceInput.getValue().matches("\\u0000+|\\ufeff+|\\u200b+|\\u200c+|\\u200d+| +|^$"))
                addPriceMenuButtonContinue.active = true;
            else
                addPriceMenuButtonContinue.active = false;
        }
    }

    private static final Component toolTipLeftClickAddPrice = Component.translatable("tooltip.vendingmachine.left_click_price");
    private static final Component toolTipRightClickRemovePrice = Component.translatable("tooltip.vendingmachine.right_click_price");

    @Override
    protected void renderTooltip(PoseStack poseStack, ItemStack itemStack, int mouseX, int mouseY) {
        if(!renderPriceMenu){
            Object _price = this.menu.getTile().getPrices().get(itemStack.getItem());
            float price = _price == null ? 0 : (float) _price;
            var tooltip = this.getTooltipFromItem(itemStack);
            tooltip.add(1, Component.translatable("tooltip.vendingmachine.price", MoneyItem.DECIMAL_FORMAT.format(price), VendingMachineConfig.GENERAL.isStackPrices.get() ? itemStack.getMaxStackSize() : 1));
            tooltip.add(2, toolTipLeftClickAddPrice);
            tooltip.add(3, toolTipRightClickRemovePrice);
            this.renderTooltip(poseStack, tooltip, itemStack.getTooltipImage(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        mainPageButton.renderButton(poseStack, mouseX, mouseY, partialTicks);
        thisPageButton.renderButton(poseStack, mouseX, mouseY, partialTicks);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.gui);
        this.blit(poseStack, leftPos, topPos, 0, 0, this.getXSize(), this.getYSize());
        this.blit(poseStack, leftPos + 126, topPos + 35, 98, 4, 18, 18);
        this.blit(poseStack, leftPos + 122, topPos + 76, 98, 4, 26, 26);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        SlotItemHandler slot = findSlot(mouseX, mouseY);
        if(slot != null && (itemPriceInput == null || !itemPriceInput.isActive()) && slot.getSlotIndex() < VenderBlockMenu.LAST_CONTAINER_SLOT_INDEX){
            selectedItem = slot.getItem();
            if(selectedItem != null && selectedItem != ItemStack.EMPTY){
                if(mouseButton == 0){
                    menu.selectedSlot = slot;
                    var val = this.menu.getTile().getPrices().get(selectedItem.getItem());
                    String selectedItemValueString = MoneyItem.DECIMAL_FORMAT.format(val == null ? 0 : val);
                    createAddMenu(selectedItemValueString);
                }else if(mouseButton == 1){
                    VendingMachineBlockEntity tile = menu.getTile();
                    VendingMachine.NETWORK_HANDLER.sendToServer(new ChangePricePacket(tile.getBlockPos(), selectedItem, currentPrice, false));
                    tile.removePrice(selectedItem);
                    menu.selectedSlot = null;
                }

            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void removeAddMenu(){
        renderPriceMenu = false;
        addPriceMenuButtonContinue.visible = false;
        addPriceMenuButtonContinue.active = false;
        addPriceMenuButtonCancel.visible = false;
        addPriceMenuButtonCancel.active = false;
        itemPriceInput.setValue("");
        itemPriceInput.setEditable(false);
        itemPriceInput.visible = false;
        itemPriceInput.active = false;
    }

    public void createAddMenu(String inputVal){
        renderPriceMenu = true;

        if(itemPriceInput == null){
            itemPriceInput = new EditBox(this.minecraft.font, leftPos + 32, topPos + 64, 64, 16, Component.empty());
            itemPriceInput.setFilter(text -> text.matches("(^$|[0-9]+)"));
            this.addWidget(itemPriceInput);
            itemPriceInput.setValue(inputVal);
            itemPriceInput.setMaxLength(8);
        } else {
            itemPriceInput.visible = true;
            itemPriceInput.setValue(inputVal);
            itemPriceInput.x = leftPos + 32;
            itemPriceInput.y = topPos + 64;
        }
        itemPriceInput.active = true;
        itemPriceInput.setEditable(true);

        if(addPriceMenuButtonContinue == null){
            addPriceMenuButtonContinue = new Button(leftPos + 32, topPos + 84, 32, 16, Component.translatable("gui.vendingmachine.button_continue"), onAddedPress);
            this.addWidget(addPriceMenuButtonContinue);
        } else {
            addPriceMenuButtonContinue.visible = true;
            addPriceMenuButtonContinue.x = leftPos + 32;
            addPriceMenuButtonContinue.y = topPos + 84;
        }
        addPriceMenuButtonContinue.active = true;

        if(addPriceMenuButtonCancel == null){
            addPriceMenuButtonCancel = new Button(leftPos + 72, topPos + 84, 32, 16, Component.translatable("gui.vendingmachine.button_cancel"), hideAddMenu);
            this.addWidget(addPriceMenuButtonCancel);
        }else {
            addPriceMenuButtonCancel.visible = true;
            addPriceMenuButtonCancel.x = leftPos + 72;
            addPriceMenuButtonCancel.y = topPos + 84;
        }
        addPriceMenuButtonCancel.active = true;
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
    private SlotItemHandler findSlot(double p_97745_, double p_97746_) {
        for(int i = 0; i < this.menu.slots.size(); ++i) {
            SlotItemHandler slot = (SlotItemHandler) this.menu.slots.get(i);
            if (this.isHovering(slot, p_97745_, p_97746_) && slot.isActive()) {
                return slot;
            }
        }

        return null;
    }
    private boolean isHovering(Slot p_97775_, double p_97776_, double p_97777_) {
        return this.isHovering(p_97775_.x, p_97775_.y, 16, 16, p_97776_, p_97777_);
    }

    @Override
    public void onClose() {
        super.onClose();
        this.menu.getTile().setChanged();
    }

    public Button.OnPress onTabButtonPress(boolean isMain){
        return new Button.OnPress() {
            @Override
            public void onPress(Button button) {
                VendingMachineBlockEntity tile = menu.getTile();
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
                currentPrice = itemPriceInput.getValue().equals("") ? 0 : Integer.parseInt(itemPriceInput.getValue());
                VendingMachineBlockEntity tile = menu.getTile();
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