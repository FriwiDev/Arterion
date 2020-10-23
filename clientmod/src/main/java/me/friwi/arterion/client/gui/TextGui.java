package me.friwi.arterion.client.gui;

import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;

import me.friwi.arterion.client.ArterionModConfig;
import me.friwi.arterion.client.gui.util.ParagraphUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;

public class TextGui extends GuiScreen {
	/** Text field containing the answer. */
	private GuiTextField inputTextField;
	/** "Done" button for the GUI. */
	private GuiButton doneBtn;
	private GuiButton cancelBtn;
	private String title;
	private String[] message;

	public TextGui(String title, String message) {
		this.title = title;
		List<String> m = ParagraphUtil.getDescriptionWithLimit(message, 60);
		this.message = new String[m.size()];
		m.toArray(this.message);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {
		this.inputTextField.updateCursorCounter();
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when
	 * the GUI is displayed and when the window resizes, the buttonList is cleared
	 * beforehand.
	 */
	public void initGui() {
		if(title.length()==0)this.mc.displayGuiScreen((GuiScreen) null);
		
		int backupScale = Minecraft.getMinecraft().gameSettings.guiScale;
		Minecraft.getMinecraft().gameSettings.guiScale = getDesiredScale();
		
		ScaledResolution scaled = new ScaledResolution(mc);
		this.width = scaled.getScaledWidth();
		this.height = scaled.getScaledHeight();
		
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		int y = this.height / 4 - 16 + 25 + message.length * mc.fontRendererObj.FONT_HEIGHT + 25;
		this.buttonList.add(this.doneBtn = new GuiButton(0, this.width / 2 - 4 - 150, y+55,
				150, 20,
				I18n.format("gui.done", new Object[0])));
		this.buttonList.add(this.cancelBtn = new GuiButton(1, this.width / 2 + 4, y+55,
				150, 20,
				I18n.format("gui.cancel", new Object[0])));
		this.inputTextField = new GuiTextField(2, this.fontRendererObj, this.width / 2 - 150,
				y, 300, 20);
		this.inputTextField.setMaxStringLength(50);
		this.inputTextField.setFocused(true);
		this.inputTextField.setText("");
		this.doneBtn.enabled = this.inputTextField.getText().trim().length() > 0;
		
		Minecraft.getMinecraft().gameSettings.guiScale = backupScale;
		
		scaled = new ScaledResolution(mc);
		this.width = scaled.getScaledWidth();
		this.height = scaled.getScaledHeight();
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for
	 * buttons)
	 */
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled) {
			if (button.id == 1) {
				this.mc.displayGuiScreen((GuiScreen) null);
				mc.addScheduledTask(()->this.mc.thePlayer.sendChatMessage("q"));
			} else if (button.id == 0) {
				this.mc.displayGuiScreen((GuiScreen) null);
				mc.addScheduledTask(()->this.mc.thePlayer.sendChatMessage(inputTextField.getText()));
			}
		}
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the
	 * equivalent of KeyListener.keyTyped(KeyEvent e). Args : character (character
	 * on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		this.inputTextField.textboxKeyTyped(typedChar, keyCode);
		this.doneBtn.enabled = this.inputTextField.getText().trim().length() > 0;

		if (keyCode != 28 && keyCode != 156) {
			if (keyCode == 1) {
				this.actionPerformed(this.cancelBtn);
			}
		} else {
			this.actionPerformed(this.doneBtn);
		}
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		int backupScale = Minecraft.getMinecraft().gameSettings.guiScale;
		Minecraft.getMinecraft().gameSettings.guiScale = getDesiredScale();
		
		ScaledResolution scaled = new ScaledResolution(mc);
		mouseX = mouseX*scaled.getScaledWidth()/width;
		mouseY = mouseY*scaled.getScaledHeight()/height;
		this.width = scaled.getScaledWidth();
		this.height = scaled.getScaledHeight();
		
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.inputTextField.mouseClicked(mouseX, mouseY, mouseButton);
		
		Minecraft.getMinecraft().gameSettings.guiScale = backupScale;
		
		scaled = new ScaledResolution(mc);
		this.width = scaled.getScaledWidth();
		this.height = scaled.getScaledHeight();
	}

	/**
	 * Draws the screen and all the components in it. Args : mouseX, mouseY,
	 * renderPartialTicks
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		int backupScale = Minecraft.getMinecraft().gameSettings.guiScale;
		Minecraft.getMinecraft().gameSettings.guiScale = getDesiredScale();
		
		ScaledResolution scaled = new ScaledResolution(mc);
		mouseX = mouseX*scaled.getScaledWidth()/width;
		mouseY = mouseY*scaled.getScaledHeight()/height;
		this.width = scaled.getScaledWidth();
		this.height = scaled.getScaledHeight();
		
		this.mc.entityRenderer.setupOverlayRendering();
		
		this.drawDefaultBackground();
		int y = this.height / 4 -16;
		this.drawCenteredString(this.fontRendererObj, title, this.width / 2, y, 16777215);
		for (int i = 0; i < message.length; i++) {
			this.drawString(this.fontRendererObj, message[i], this.width / 2 - 150,
					y + 25 + i * mc.fontRendererObj.FONT_HEIGHT, 0xFFFFFFFF);
		}
		this.inputTextField.drawTextBox();

		super.drawScreen(mouseX, mouseY, partialTicks);
		
		Minecraft.getMinecraft().gameSettings.guiScale = backupScale;
		
		scaled = new ScaledResolution(mc);
		this.width = scaled.getScaledWidth();
		this.height = scaled.getScaledHeight();
		
		this.mc.entityRenderer.setupOverlayRendering();
	}
	
	private int getDesiredScale() {
		int scale = ArterionModConfig.getGuiScale();
		if(scale==1||scale==2) {
			return scale;
		}else {
			return 2;
		}
	}
}