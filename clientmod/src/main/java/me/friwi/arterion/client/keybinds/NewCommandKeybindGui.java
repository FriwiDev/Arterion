package me.friwi.arterion.client.keybinds;

import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;

import me.friwi.arterion.client.gui.util.ParagraphUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

public class NewCommandKeybindGui extends GuiScreen {
	/** Text field containing the answer. */
	private GuiTextField inputTextField;
	/** "Done" button for the GUI. */
	private GuiButton doneBtn;
	private GuiButton cancelBtn;
	private GuiButton keyBtn;
	/** The parent Gui screen */
    protected GuiScreen parentScreen;
    private CommandKeybind bind;
    private boolean captureKey = false;
    private int key;


	public NewCommandKeybindGui(GuiScreen parent, CommandKeybind bind) {
		this.parentScreen = parent;
		this.bind = bind;
		this.key = bind==null?-1:bind.keycode;
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
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		int y = this.height / 4 - 16 + 25 + 25;
		this.buttonList.add(this.doneBtn = new GuiButton(0, this.width / 2 - 4 - 150, y+55,
				150, 20,
				I18n.format("gui.done", new Object[0])));
		this.buttonList.add(this.cancelBtn = new GuiButton(1, this.width / 2 + 4, y+55,
				150, 20,
				I18n.format("gui.cancel", new Object[0])));
		this.buttonList.add(this.keyBtn = new GuiButton(2, this.width / 2 - 152, y,
				304, 20,
				""));
		this.inputTextField = new GuiTextField(2, this.fontRendererObj, this.width / 2 - 150,
				y+22, 300, 20);
		this.inputTextField.setMaxStringLength(50);
		this.inputTextField.setFocused(true);
		this.inputTextField.setText(bind==null?"/":bind.command);
		this.doneBtn.enabled = this.inputTextField.getText().trim().length() > 0 && key!=-1;
		updateKeyButtonText();
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
				this.mc.displayGuiScreen(parentScreen);
			} else if (button.id == 0) {
				if(bind==null) {
					KeybindManager.addKeybind(new CommandKeybind(key, this.inputTextField.getText()));
				}else {
					bind.keycode = key;
					bind.command = this.inputTextField.getText();
					KeybindManager.save();
				}
				this.mc.displayGuiScreen(parentScreen);
			} else if (button.id == 2) {
				captureKey=!captureKey;
				updateKeyButtonText();
			}
		}
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the
	 * equivalent of KeyListener.keyTyped(KeyEvent e). Args : character (character
	 * on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if(captureKey) {
			if(keyCode!=Keyboard.KEY_ESCAPE) {
				key = keyCode;
			}
			captureKey=false;
			updateKeyButtonText();
			this.doneBtn.enabled = this.inputTextField.getText().trim().length() > 0 && key!=-1;
			return;
		}
		
		this.inputTextField.textboxKeyTyped(typedChar, keyCode);
		this.doneBtn.enabled = this.inputTextField.getText().trim().length() > 0 && key!=-1;

		if (keyCode != 28 && keyCode != 156) {
			if (keyCode == 1) {
				this.actionPerformed(this.cancelBtn);
			}
		} else {
			this.actionPerformed(this.doneBtn);
		}
	}

	private void updateKeyButtonText() {
		if(captureKey) {
			this.keyBtn.displayString = "\2477> Press Key <";
		}else {
			if(key==-1) {
				this.keyBtn.displayString = "\247cNone";
			}else {
				this.keyBtn.displayString = KeybindManager.getKeyText(key);
			}
		}
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.inputTextField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Draws the screen and all the components in it. Args : mouseX, mouseY,
	 * renderPartialTicks
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		int y = this.height / 4 -16;
		this.drawCenteredString(this.fontRendererObj, bind==null?"Create Keybind":"Edit Keybind", this.width / 2, y, 16777215);
		y+=25 + 25 - 10;
		this.drawString(this.fontRendererObj, "Please select a key and enter a command for your keybind", this.width / 2 - 148, y, 0xFFFFFFFF);
		this.inputTextField.drawTextBox();

		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}