package me.friwi.arterion.client.keybinds;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class KeybindsGui extends GuiScreen
{
    /** The parent Gui screen */
    protected GuiScreen parentScreen;
    /** The List GuiSlot object reference. */
    private KeybindsGui.List list;
    private GuiButton back;
    private GuiButton edit;
    private GuiButton delete;
    private GuiButton create;

    public KeybindsGui(GuiScreen screen)
    {
        this.parentScreen = screen;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        this.buttonList.add(this.back = new GuiButton(1, this.width / 2 - 159, this.height - 38, 78, 20, "Back"));
        this.buttonList.add(this.edit = new GuiButton(2, this.width / 2 - 79, this.height - 38, 78, 20, "Edit"));
        this.buttonList.add(this.delete = new GuiButton(3, this.width / 2 + 1, this.height - 38, 78, 20, "Delete"));
        this.buttonList.add(this.create = new GuiButton(4, this.width / 2 + 81, this.height - 38, 78, 20, "Create"));
        this.list = new KeybindsGui.List(this.mc);
        this.list.registerScrollButtons(7, 8);
        if(KeybindManager.list.size()==0) {
        	edit.enabled = false;
        	delete.enabled = false;
        }
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.list.handleMouseInput();
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
            switch (button.id)
            {
                case 1:
                    this.mc.displayGuiScreen(this.parentScreen);
                    break;
                case 2:
                	Keybind editb = KeybindManager.list.get(list.selected);
                	if(editb!=null) {
                		this.mc.displayGuiScreen(new NewCommandKeybindGui(this, (CommandKeybind) editb));
                	}
                    break;
                case 3:
                	Keybind deleteb = KeybindManager.list.get(list.selected);
                	if(deleteb!=null) {
                		KeybindManager.delKeybind(deleteb);
                		if(KeybindManager.list.size()==0) {
                        	edit.enabled = false;
                        	delete.enabled = false;
                        	list.selected = 0;
                        }else if(KeybindManager.list.size()<=list.selected) {
                			list.selected = KeybindManager.list.size()-1;
                		}
                	}
                	break;
                case 4:
                	this.mc.displayGuiScreen(new NewCommandKeybindGui(this, null));
                    break;
                default:
                    this.list.actionPerformed(button);
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.list.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, "Keybinds", this.width / 2, 16, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @SideOnly(Side.CLIENT)
    class List extends GuiSlot
    {
        public int selected = 0;
    	
    	public List(Minecraft mcIn)
        {
            super(mcIn, KeybindsGui.this.width, KeybindsGui.this.height, 32, KeybindsGui.this.height - 65 + 4, 18);
            KeybindManager.sortKeybinds();
        }

        protected int getSize()
        {
            return KeybindManager.list.size();
        }

        /**
         * The element in the slot that was clicked, boolean for whether it was double clicked or not
         */
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
        {
            selected = slotIndex;
            if(isDoubleClick) {
            	KeybindsGui.this.actionPerformed(edit);
            }
        }

        /**
         * Returns true if the element passed in is currently selected
         */
        protected boolean isSelected(int slotIndex)
        {
            return selected==slotIndex;
        }

        /**
         * Return the height of the content being scrolled
         */
        protected int getContentHeight()
        {
            return this.getSize() * 18;
        }

        protected void drawBackground()
        {
            KeybindsGui.this.drawDefaultBackground();
        }

        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn)
        {
        	Keybind bind = KeybindManager.list.get(entryID);
            KeybindsGui.this.drawString(KeybindsGui.this.fontRendererObj, KeybindManager.getKeyText(bind.keycode), p_180791_2_ + 3, p_180791_3_ + 3, 16777215);
            KeybindsGui.this.drawString(KeybindsGui.this.fontRendererObj, ((CommandKeybind)bind).command, p_180791_2_ + 50, p_180791_3_ + 3, 16777215);
        }
    }
}