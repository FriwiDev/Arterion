package me.friwi.arterion.client.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import me.friwi.arterion.client.ArterionModConfig;
import me.friwi.arterion.client.gui.util.TooltipPainter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.util.ResourceLocation;

public class ChestMenuGui extends GuiScreen {
	private String title;
	private ContainerChest container;

	private int rows;
	private int totalWidth;
	private int totalHeight;
	private int elementWidth;
	private int elementHeight;
	private int spacer = 5;

	public ChestMenuGui(ContainerChest container) {
		this.title = container.getLowerChestInventory().getName().replace("\2478", "\2477");
		this.container = container;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when
	 * the GUI is displayed and when the window resizes, the buttonList is cleared
	 * beforehand.
	 */
	public void initGui() {
		this.mc.thePlayer.openContainer = this.container;

		IInventory chest = container.getLowerChestInventory();

		this.rows = chest.getSizeInventory() / 9;

		if (this.width < 50 || this.height < 50)
			return; // Do not crash client by division by 0

		// Calc outer dimensions and box sizes
		this.totalWidth = (int) ((this.width / 4 * 3.8) - 10 * this.spacer);
		this.elementWidth = this.totalWidth / 9;
		this.elementHeight = (int) (this.elementWidth * 1.5f);
		this.totalHeight = this.elementWidth * this.rows + this.spacer * (this.rows + 1);
		if (this.totalHeight > this.height / 4 * 3 - this.spacer * (this.rows + 1)) {
			this.totalHeight = this.height / 4 * 3 - this.spacer * (this.rows + 1);
			this.elementHeight = this.totalHeight / this.rows;
			this.elementWidth = (int) (this.elementHeight / 1.5f);
			this.totalWidth = this.elementWidth * 9;
		}
		this.totalWidth += this.spacer * 10;
		this.totalHeight += this.spacer * (this.rows + 1);
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void onGuiClosed() {
		if(Minecraft.getMinecraft().thePlayer!=null) {
			if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(new C0DPacketCloseWindow(container.windowId));
		}
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		int backupScale = Minecraft.getMinecraft().gameSettings.guiScale;
		Minecraft.getMinecraft().gameSettings.guiScale = getDesiredScale();

		ScaledResolution scaled = new ScaledResolution(mc);
		mouseX = mouseX * scaled.getScaledWidth() / width;
		mouseY = mouseY * scaled.getScaledHeight() / height;
		this.width = scaled.getScaledWidth();
		this.height = scaled.getScaledHeight();

		// Calc outer dimensions and box sizes
		this.totalWidth = (int) ((this.width / 4 * 3.8) - 10 * this.spacer);
		this.elementWidth = this.totalWidth / 9;
		this.elementHeight = (int) (this.elementWidth * 1.5f);
		this.totalHeight = this.elementWidth * this.rows + this.spacer * (this.rows + 1);
		if (this.totalHeight > this.height / 4 * 3 - this.spacer * (this.rows + 1)) {
			this.totalHeight = this.height / 4 * 3 - this.spacer * (this.rows + 1);
			this.elementHeight = this.totalHeight / this.rows;
			this.elementWidth = (int) (this.elementHeight / 1.5f);
			this.totalWidth = this.elementWidth * 9;
		}
		this.totalWidth += this.spacer * 10;
		this.totalHeight += this.spacer * (this.rows + 1);

		int yoff = this.height / 8 + 25;
		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < rows; y++) {
				int xoffs = this.width / 2 - this.totalWidth / 2 + this.spacer + x * (this.spacer + this.elementWidth);
				int yoffs = yoff + this.spacer + y * (this.spacer + this.elementHeight);
				boolean m = isMouseOver(mouseX, mouseY, xoffs, yoffs, this.elementWidth, this.elementWidth);
				if (m) {
					ItemStack stack = container.getSlot(y * 9 + x).getStack();
					if (stack != null) {
						Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(
								new C0EPacketClickWindow(this.container.windowId, y * 9 + x, 0, 0, stack, (short) 0));
						this.playPressSound(mc.getSoundHandler());
					}
				}
			}
		}

		Minecraft.getMinecraft().gameSettings.guiScale = backupScale;

		scaled = new ScaledResolution(mc);
		this.width = scaled.getScaledWidth();
		this.height = scaled.getScaledHeight();
	}

	private int getDesiredScale() {
		int scale = ArterionModConfig.getGuiScale();
		if (scale == 1 || scale == 2) {
			return scale;
		} else {
			return 2;
		}
	}

	/**
	 * Draws the screen and all the components in it. Args : mouseX, mouseY,
	 * renderPartialTicks
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		int backupScale = Minecraft.getMinecraft().gameSettings.guiScale;
		Minecraft.getMinecraft().gameSettings.guiScale = getDesiredScale();

		ScaledResolution scaled = new ScaledResolution(mc);
		mouseX = mouseX * scaled.getScaledWidth() / width;
		mouseY = mouseY * scaled.getScaledHeight() / height;
		this.width = scaled.getScaledWidth();
		this.height = scaled.getScaledHeight();

		this.mc.entityRenderer.setupOverlayRendering();

		// Calc outer dimensions and box sizes
		this.totalWidth = (int) ((this.width / 4 * 3.8) - 10 * this.spacer);
		this.elementWidth = this.totalWidth / 9;
		this.elementHeight = (int) (this.elementWidth * 1.5f);
		this.totalHeight = this.elementWidth * this.rows + this.spacer * (this.rows + 1);
		if (this.totalHeight > this.height / 4 * 3 - this.spacer * (this.rows + 1)) {
			this.totalHeight = this.height / 4 * 3 - this.spacer * (this.rows + 1);
			this.elementHeight = this.totalHeight / this.rows;
			this.elementWidth = (int) (this.elementHeight / 1.5f);
			this.totalWidth = this.elementWidth * 9;
		}
		this.totalWidth += this.spacer * 10;
		this.totalHeight += this.spacer * (this.rows + 1);

		this.drawDefaultBackground();
		int yoff = this.height / 8 - 16;
		this.drawCenteredString(this.fontRendererObj, title, this.width / 2, yoff, 16777215);

		yoff += 41;
		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < rows; y++) {
				int xoffs = this.width / 2 - this.totalWidth / 2 + this.spacer + x * (this.spacer + this.elementWidth);
				int yoffs = yoff + this.spacer + y * (this.spacer + this.elementHeight);
				boolean m = isMouseOver(mouseX, mouseY, xoffs, yoffs, this.elementWidth, this.elementWidth);
				ItemStack stack = container.getSlot(y * 9 + x).getStack();
				if (stack != null) {
					this.drawBox(stack, xoffs, yoffs, this.elementWidth, this.elementHeight, m);
				}
			}
		}
		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < rows; y++) {
				int xoffs = this.width / 2 - this.totalWidth / 2 + this.spacer + x * (this.spacer + this.elementWidth);
				int yoffs = yoff + this.spacer + y * (this.spacer + this.elementHeight);
				boolean m = isMouseOver(mouseX, mouseY, xoffs, yoffs, this.elementWidth, this.elementWidth);
				ItemStack stack = container.getSlot(y * 9 + x).getStack();
				if (stack != null) {
					this.drawBoxTooltip(stack, xoffs, yoffs, this.elementWidth, this.elementHeight, m);
				}
			}
		}

		super.drawScreen(mouseX, mouseY, partialTicks);

		Minecraft.getMinecraft().gameSettings.guiScale = backupScale;

		scaled = new ScaledResolution(mc);
		this.width = scaled.getScaledWidth();
		this.height = scaled.getScaledHeight();

		this.mc.entityRenderer.setupOverlayRendering();
	}

	private void drawBox(ItemStack stack, int xoffs, int yoffs, int width, int height, boolean mouseOver) {
		if (mouseOver) {
			this.drawRect(xoffs, yoffs, xoffs + width, yoffs + width, 0xFFAAAAAA);
		}
		GlStateManager.pushMatrix();
		GlStateManager.translate(xoffs, yoffs, 0);
		GlStateManager.scale((width + 0f) / 16f, (width + 0f) / 16f, 1f);
		RenderHelper.disableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();
		mc.getRenderItem().renderItemIntoGUI(stack, 0, 0);
		GlStateManager.popMatrix();
	}

	private void drawBoxTooltip(ItemStack stack, int xoffs, int yoffs, int width, int height, boolean mouseOver) {
		TooltipPainter.drawXCenteredHoveringText(stack.getTooltip(Minecraft.getMinecraft().thePlayer, false),
				xoffs + this.elementWidth / 2 + 3, yoffs + this.elementWidth, mc.fontRendererObj);
	}

	private boolean isMouseOver(int mouseX, int mouseY, int xoffs, int yoffs, int width, int height) {
		return mouseX >= xoffs && mouseX < xoffs + width && mouseY >= yoffs && mouseY < yoffs + height;
	}

	public void playPressSound(SoundHandler soundHandlerIn) {
		soundHandlerIn.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
	}
}