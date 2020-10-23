package me.friwi.arterion.client.gui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.lwjgl.opengl.GL11;

import me.friwi.arterion.client.ArterionClientMod;
import me.friwi.arterion.client.ArterionModConfig;
import me.friwi.arterion.client.ClassEnum;
import me.friwi.arterion.client.data.FriendlyPlayerEntry;
import me.friwi.arterion.client.data.FriendlyPlayerList;
import me.friwi.arterion.client.data.KillFeedEntry;
import me.friwi.arterion.client.data.KillFeedList;
import me.friwi.arterion.client.data.ModValueEnum;
import me.friwi.arterion.client.data.Objective;
import me.friwi.arterion.client.data.SkillDataEntry;
import me.friwi.arterion.client.data.SkillDataList;
import me.friwi.arterion.client.gui.util.ChatColor;
import me.friwi.arterion.client.gui.util.HeadCacheUtil;
import me.friwi.arterion.client.gui.util.TimeFormatUtil;
import me.friwi.arterion.client.keybinds.CommandKeybind;
import me.friwi.arterion.client.keybinds.Keybind;
import me.friwi.arterion.client.keybinds.KeybindManager;
import me.friwi.arterion.client.labymod.LabyKeybinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class OverlayGui extends Gui {
	private Minecraft mc;
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	private ItemStack clock = null;
	private ItemStack gold = null;
	private ItemStack map = null;
	private ItemStack compass = null;
	private static final ResourceLocation xp_bar = new ResourceLocation(ArterionClientMod.TEXMODID,
			"arterionclientmod/textures/gui/xp_bar.bmp");
	private static final ResourceLocation xp_bar_bg = new ResourceLocation(ArterionClientMod.TEXMODID,
			"arterionclientmod/textures/gui/xp_bar_bg.bmp");
	private static final ResourceLocation hp_bar = new ResourceLocation(ArterionClientMod.TEXMODID,
			"arterionclientmod/textures/gui/hp_bar.bmp");
	private static final ResourceLocation mana_bar = new ResourceLocation(ArterionClientMod.TEXMODID,
			"arterionclientmod/textures/gui/mana_bar.bmp");
	private static final ResourceLocation kill_arrow = new ResourceLocation(ArterionClientMod.TEXMODID,
			"arterionclientmod/textures/gui/kill_arrow.png");

	public static final int slotWidth = 66-16;
	public static final int slotHeight = 66-16;
	private final int slotDisabledColor = 0xFF555555;

	private final int friendlyPlayerHeight = 20;
	private final int friendlyPlayerSpacer = 5;

	private final int killSpacer = 3;
	private final int killHeight = 16;

	public OverlayGui(Minecraft mc) {
		this.mc = mc;
	}

	public void render() {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, -25f);
		GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);
		int backupScale = Minecraft.getMinecraft().gameSettings.guiScale;
		Minecraft.getMinecraft().gameSettings.guiScale = ArterionModConfig.getGuiScale();
		this.mc.entityRenderer.setupOverlayRendering();

		this.zLevel = -2000;

		ScaledResolution scaled = new ScaledResolution(mc);
		int width = scaled.getScaledWidth();
		int height = scaled.getScaledHeight();

		// Top BG
		this.drawRect(0, 0, width, 18, 0xFF000000);
		this.drawRect(0, 18, width, 19, 0xFFFFFFFF);

		// Server status or objective
		String status = ModValueEnum.SERVER_STATUS.getString();
		if (Objective.isActive()) {
			status = Objective.buildMessage();
		}
		this.drawCenteredString(mc.fontRendererObj, status, width / 2, 6, 0xFFFFFFFF);

		// Class & lvl
		int x = 0;
		ClassEnum selectedClass = ModValueEnum.SELECTED_CLASS.getClassEnum();
		if (selectedClass == ClassEnum.NONE) {
			String str = "\2477" + ModValueEnum.SELECTED_CLASS_NAME.getString();
			this.drawString(mc.fontRendererObj, str, 3, 6, 0xFFFFFFFF);
			x = 3 + mc.fontRendererObj.getStringWidth(str);
		} else {
			String str = "\2472" + ModValueEnum.LEVEL.getInt();
			if(ModValueEnum.PRESTIGE_LEVEL.getInt()>0) {
				str += " \2477(\247c"+ModValueEnum.PRESTIGE_LEVEL.getInt()+"\2477)";
			}
			this.drawItemStack(selectedClass.getStack(), 3, 1);
			this.drawString(mc.fontRendererObj, str, 3 + 16 + 3, 6, 0xFFFFFFFF);
			x = 3 + 16 + 3 + mc.fontRendererObj.getStringWidth(str);
		}

		x += 20;

		// Gold
		if (gold == null)
			gold = new ItemStack(Items.gold_ingot);
		String num = "\247f" + String.format(Locale.ENGLISH, "%.2f", (ModValueEnum.GOLD.getInt() + 0f) / 100f);
		int point = num.indexOf('.');
		if (point != -1) {
			if (num.endsWith("00")) {
				num = num.substring(0, point);
			} else if (num.endsWith("0")) {
				num = num.substring(0, point + 2);
			}
		}
		this.drawItemStack(gold, x, 1);
		this.drawString(mc.fontRendererObj, num, x + 16 + 3, 6, 0xFFFFFFFF);

		// Time
		if (clock == null)
			clock = new ItemStack(Items.clock);
		String time = timeFormat.format(new Date());
		int clockWidth = mc.fontRendererObj.getStringWidth(time);
		this.drawString(mc.fontRendererObj, time, width - 3 - clockWidth, 6, 0xFFFFFFFF);
		this.drawItemStack(clock, width - 3 - clockWidth - 3 - 16, 1);

		x = width - 3 - clockWidth - 3 - 16 - 20;

		// Region
		if (map == null)
			map = new ItemStack(Items.map);
		String region = ModValueEnum.REGION.getString();
		int regionWidth = mc.fontRendererObj.getStringWidth(region);
		this.drawString(mc.fontRendererObj, region, x - regionWidth, 6, 0xFFFFFFFF);
		this.drawItemStack(map, x - regionWidth - 3 - 16, 1);

		x = x - regionWidth - 3 - 16 - 20;

		// Compass
		if (compass == null)
			compass = new ItemStack(Items.compass);
		String dir = "\2477?";
		Entity entity = this.mc.getRenderViewEntity();
		EnumFacing enumfacing = entity.getHorizontalFacing();
		switch (enumfacing) {
		case NORTH:
			dir = "\247fN";
			break;
		case SOUTH:
			dir = "\247fS";
			break;
		case WEST:
			dir = "\247fW";
			break;
		case EAST:
			dir = "\247fE";
		}
		int dirWidth = mc.fontRendererObj.getStringWidth(dir);
		this.drawString(mc.fontRendererObj, dir, x - dirWidth, 6, 0xFFFFFFFF);
		this.drawItemStack(compass, x - dirWidth - 3 - 16, 1);

		// XP Bar
		this.mc.getTextureManager().bindTexture(xp_bar_bg);
		this.drawModalRectWithCustomSizedTexture(0, 19, 0, 0, width, 5, 1, 5);
		this.mc.getTextureManager().bindTexture(xp_bar);
		this.drawModalRectWithCustomSizedTexture(0, 19, 0, 0, width * ModValueEnum.XP_PER_MILLE.getInt() / 1000, 5, 1,
				5);
		this.drawRect(0, 24, width, 25, 0xFFFFFFFF);

		// Skill slots
		int slotCount = SkillDataList.getSkills().length;
		for (int i = 0; i < SkillDataList.getSkills().length; i++) {
			if (SkillDataList.getSkills()[i] != null) {
				this.drawSkillSlot(i, SkillDataList.getSkills()[i], width / 2 - slotCount * slotWidth / 2 + i * slotWidth,
						25);
			}
		}

		// Render own hp
		GlStateManager.color(1f, 1f, 1f, 1f);
		int ownWidth = 150;
		int ownX = 0;
		int ownHeight = 38;
		int ownY = 30;
		int headXOffset = -8;
		int barX = ownX + headXOffset + (int) ((ownHeight * 3f / 2f) / 2);
		int barWidth = ownX + ownWidth - 2 - barX;
		int headEndX = ownX + headXOffset + ownHeight;
		this.drawRect(ownX, ownY, ownX + ownWidth + 1, ownY + ownHeight + 1, 0xFF000000);
		GlStateManager.pushMatrix();
		GlStateManager.translate(ownX + headXOffset, ownY - ownHeight / 6f - 2.5f, 0);
		GlStateManager.scale((ownHeight + 0f) / 16f * 3f / 2f, (ownHeight + 0f) / 16f * 3f / 2f, 1);
		this.drawItemStack(HeadCacheUtil.supplyItemStack(Minecraft.getMinecraft().getSession().getProfile().getId())[0],
				0, 0);
		GlStateManager.popMatrix();
		this.mc.getTextureManager().bindTexture(hp_bar);
		float healthperc = Minecraft.getMinecraft().thePlayer.getHealth()
				/ Minecraft.getMinecraft().thePlayer.getMaxHealth();
		int health = (int) (healthperc * ModValueEnum.MAXHEALTH.getInt());
		this.drawModalRectWithCustomSizedTexture(barX, ownY + 2, 0, 0, (int) ((barWidth + 1) * healthperc), 16, 1, 16);
		this.drawCenteredString(mc.fontRendererObj, health + " / " + ModValueEnum.MAXHEALTH.getInt(),
				(headEndX + ownX + ownWidth) / 2, ownY + 2 + 4, 0xFFFFFFFF);
		this.mc.getTextureManager().bindTexture(mana_bar);
		float manaperc = (ModValueEnum.MANA.getInt() + 0f) / (ModValueEnum.MAXMANA.getInt() + 0f);
		this.drawModalRectWithCustomSizedTexture(barX, ownY + 2 + 16 + 3, 0, 0, (int) ((barWidth + 1) * manaperc), 16,
				1, 16);
		this.drawCenteredString(mc.fontRendererObj, ModValueEnum.MANA.getInt() + " / " + ModValueEnum.MAXMANA.getInt(),
				(headEndX + ownX + ownWidth) / 2, ownY + 2 + 4 + 16 + 3, 0xFFFFFFFF);

		// Render friendlies
		GlStateManager.color(1f, 1f, 1f, 1f);
		int size = FriendlyPlayerList.getFriendlyPlayers().size();
		int i = 0;
		int friendlyX = 0;
		int friendlyY = ownY + ownHeight + 10; // 10 is initial spacer
		for (FriendlyPlayerEntry other : FriendlyPlayerList.getFriendlyPlayers()) {
			drawFriendlyPlayerEntry(other, friendlyX, friendlyY + i * (friendlyPlayerHeight + friendlyPlayerSpacer));
			i++;
		}

		// Killfeed
		int killBeginX = width - 10;
		int killBeginY = 25 + 10;
		i = 0;
		for (KillFeedEntry entry : KillFeedList.getKillFeedEntries()) {
			drawKillFeedEntry(entry, killBeginX, killBeginY + i * (killHeight + killSpacer));
			i++;
		}

		// Fix occasional render bugs
		this.mc.getTextureManager().bindTexture(SkillDataList.getSkillIcon(-1, false));
		this.drawModalRectWithCustomSizedTexture(-10000, -10000, 0, 0, -10000, -10000, slotWidth - 2, slotHeight - 2);

		GL11.glPopAttrib();
		GlStateManager.color(1f, 1f, 1f, 1f);

		this.zLevel = 0f;
		Minecraft.getMinecraft().gameSettings.guiScale = backupScale;
		GlStateManager.popMatrix();
		this.mc.entityRenderer.setupOverlayRendering();
	}

	private void drawKillFeedEntry(KillFeedEntry entry, int x, int y) {
		int killWidth = 4 + // Left of killer
				(entry.getKillerName().isEmpty() ? 0 : mc.fontRendererObj.getStringWidth(entry.getKillerName()) + 4) // Killer,
																														// including
																														// spacer
																														// to
																														// icon
				+ 8 + 3 + 4 // Killicon, arrow, spacer to killed name
				+ mc.fontRendererObj.getStringWidth(entry.getKilledName()) // Killed
				+ 4 + 2; // Right of killer

		x -= killWidth;

		int black = 0xAA000000;

		// Background
		this.drawRect(x + 1, y, x + killWidth - 1, y + 1, black);
		this.drawRect(x + 1, y + killHeight - 1, x + killWidth - 1, y + killHeight, black);
		this.drawRect(x, y + 1, x + 1, y + killHeight - 1, black);
		this.drawRect(x + killWidth - 1, y + 1, x + killWidth, y + killHeight - 1, black);
		this.drawRect(x + 2, y + 2, x + killWidth - 2, y + killHeight - 2, black);

		// Border
		int border = entry.getBorderColor();
		this.drawRect(x + 1, y + 1, x + killWidth - 1, y + 2, border);
		this.drawRect(x + 1, y + killHeight - 2, x + killWidth - 1, y + killHeight - 1, border);
		this.drawRect(x + 1, y + 1, x + 2, y + killHeight - 1, border);
		this.drawRect(x + killWidth - 2, y + 1, x + killWidth - 1, y + killHeight - 1, border);

		GlStateManager.color(1f, 1f, 1f, 1f);

		int currX = x + 4;
		int yOffs = 4;

		// Killer
		if (!entry.getKillerName().isEmpty()) {
			this.drawString(mc.fontRendererObj, entry.getKillerName(), currX, y + yOffs, 0xFFFFFFFF);
			currX += mc.fontRendererObj.getStringWidth(entry.getKillerName()) + 4;
		}

		// Icon
		this.drawRect(currX, y + yOffs, currX + 8, y + yOffs + 8, black);
		GlStateManager.color(1f, 1f, 1f, 1f);
		GlStateManager.enableBlend();
		this.mc.getTextureManager().bindTexture(kill_arrow);
		this.drawModalRectWithCustomSizedTexture(currX + 8, y + yOffs, 0, 0, 3, 8, 3, 8);
		if (entry.getIcon() == null) {
			this.mc.getTextureManager().bindTexture(entry.getKillfeedIcon());
			this.drawModalRectWithCustomSizedTexture(currX, y + yOffs, 0, 0, 8, 8, 8, 8);
		} else {
			GlStateManager.pushMatrix();
			GlStateManager.translate(currX, y + yOffs, 0);
			GlStateManager.scale(8f / 16f, 8f / 16f, 1);
			this.drawItemStack(entry.getIcon(), 0, 0);
			GlStateManager.popMatrix();
		}
		currX += 8 + 5 + 4;

		// Killed
		GlStateManager.color(1f, 1f, 1f, 1f);
		this.drawString(mc.fontRendererObj, entry.getKilledName(), currX, y + yOffs, 0xFFFFFFFF);

		GlStateManager.color(1f, 1f, 1f, 1f);
	}

	private void drawFriendlyPlayerEntry(FriendlyPlayerEntry other, int x, int y) {
		int friendlyWidth = 150;
		int headXOffset = -2;
		GlStateManager.color(1f, 1f, 1f, 1f);
		this.drawRect(x, y, x + friendlyWidth + 1, y + friendlyPlayerHeight, 0xFF000000);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + headXOffset, y - friendlyPlayerHeight / 6f - 0.5f, 0);
		GlStateManager.scale((friendlyPlayerHeight - 2f + 0f) / 16f * 3f / 2f,
				(friendlyPlayerHeight - 2f) / 16f * 3f / 2f, 1);
		this.drawItemStack(HeadCacheUtil.supplyItemStack(other.getPlayer())[0], 0, 0);
		GlStateManager.popMatrix();
		// Hp bar
		GlStateManager.color(1f, 1f, 1f, 1f);
		int barX = x + headXOffset + (int) (((friendlyPlayerHeight - 2) * 3f / 2f) / 2);
		int barWidth = x + friendlyWidth - 2 - barX;
		int headEndX = x + headXOffset + friendlyPlayerHeight;
		this.mc.getTextureManager().bindTexture(hp_bar);
		float healthperc = (other.getHealth() + 0f) / (other.getMaxhealth() + 0f);
		this.drawModalRectWithCustomSizedTexture(barX, y + 2, 0, 0, (int) ((barWidth + 1) * healthperc),
				friendlyPlayerHeight - 4, 1, friendlyPlayerHeight - 4);
		// Name
		GlStateManager.color(1f, 1f, 1f, 1f);
		String name = other.getName();
		if (name.length() > 14)
			name = name.substring(0, 14);
		this.drawString(mc.fontRendererObj, name, headEndX + 5, y + 2 + 4, 0xFFFFFFFF);
		// Class icon
		this.drawItemStack(other.getSelectedClass().getStack(), barX + barWidth - 16, y + 2);
		// Level
		GlStateManager.color(1f, 1f, 1f, 1f);
		String level = "\247f" + other.getLevel();
		this.drawString(mc.fontRendererObj, level, barX + barWidth - 16 - 2 - mc.fontRendererObj.getStringWidth(level),
				y + 2 + 4, 0xFFFFFFFF);
	}

	private void drawSkillSlot(int slot, SkillDataEntry skillDataEntry, int x, int y) {
		// Border
		int outerColor = skillDataEntry.hasCooldown() || skillDataEntry.getSkill() == -1 || !skillDataEntry.isEnabled() || skillDataEntry.getMana()>ModValueEnum.MANA.getInt() ? slotDisabledColor
				: skillDataEntry.getDelayColor();
		this.drawRect(x, y, x + 1, y + slotHeight, outerColor);
		this.drawRect(x + slotWidth - 1, y, x + slotWidth, y + slotHeight, outerColor);
		this.drawRect(x, y + slotHeight - 2, x + slotWidth, y + slotHeight, outerColor);

		// Skill icon
		GlStateManager.color(1f, 1f, 1f, 1f);
		this.mc.getTextureManager().bindTexture(SkillDataList.getSkillIcon(skillDataEntry.getSkill(), !skillDataEntry.isEnabled()));
		this.drawModalRectWithCustomSizedTexture(x + 1, y, 0, 0, slotWidth - 2, slotHeight - 2, slotWidth - 2,
				slotHeight - 2);

		// Cooldown bg
		if (skillDataEntry.hasCooldown()) {
			float percentage = (skillDataEntry.getCooldown() + 0f) / (skillDataEntry.getMaxCooldown() + 0f);
			this.drawRect(x + 1, y, x + slotWidth - 1, y + slotHeight - 2, 0x7F555555);
			this.drawRect(x + 1, y + (int) (percentage * (slotHeight - 2)), x + slotWidth - 1, y + slotHeight - 2,
					0x7F555555);
		} else
		
		// Mana bg
		if (skillDataEntry.isEnabled() && skillDataEntry.getMana()>ModValueEnum.MANA.getInt()) {
			this.drawRect(x + 1, y, x + slotWidth - 1, y + slotHeight - 2, 0x7F11117F);
			this.drawRect(x + 1, y, x + slotWidth - 1, y + slotHeight - 2, 0x7F11117F);
		}

		// Delay
		if (skillDataEntry.hasDelay()) {
			GlStateManager.color(1f, 1f, 1f, 1f);
			String delay = TimeFormatUtil.formatMillis(skillDataEntry.getDelay());
			this.drawCenteredString(mc.fontRendererObj, delay, x + slotWidth / 2, y + (slotHeight - 2) / 2 - 10,
					skillDataEntry.getDelayColor());
			GlStateManager.color(1f, 1f, 1f, 1f);
			float percentage = (skillDataEntry.getDelay() + 0f) / (skillDataEntry.getTotalDelay() + 0f);
			int delayx = x + 5;
			int delayy = y + (slotHeight - 2) / 2 + 2;
			int delayWidth = slotWidth - 10;
			int delayHeight = 8;
			this.drawRect(delayx, delayy, delayx + 1, delayy + delayHeight, 0xFFFFFFFF);
			this.drawRect(delayx + delayWidth, delayy, delayx + delayWidth + 1, delayy + delayHeight, 0xFFFFFFFF);
			this.drawRect(delayx, delayy, delayx + delayWidth, delayy + 1, 0xFFFFFFFF);
			this.drawRect(delayx, delayy + delayHeight, delayx + delayWidth, delayy + delayHeight + 1, 0xFFFFFFFF);
			delayx += 2;
			delayWidth -= 4;
			this.drawRect((int) (delayx + (delayWidth / 2f) - (percentage * delayWidth / 2f)), delayy + 2,
					(int) (delayx + (delayWidth / 2f) + (percentage * delayWidth / 2f)) + 1, delayy + delayHeight - 1,
					skillDataEntry.getDelayColor());
		} else if (skillDataEntry.hasCooldown()) {
			// Cooldown text
			GlStateManager.color(1f, 1f, 1f, 1f);
			String cooldown = TimeFormatUtil.formatMillis(skillDataEntry.getCooldown());
			this.drawCenteredString(mc.fontRendererObj, cooldown, x + slotWidth / 2, y + (slotHeight - 2) / 2 - 4,
					0xFFAAAAAA);
		} else if (skillDataEntry.isEnabled() && skillDataEntry.getMana()>ModValueEnum.MANA.getInt()) {
			//Mana missing
			int missing = skillDataEntry.getMana()-ModValueEnum.MANA.getInt();
			String mana = "\2477"+missing+" \2479Mana";
			this.drawCenteredString(mc.fontRendererObj, mana, x + slotWidth / 2, y + (slotHeight - 2) / 2 - 4,
					0xFFFFFFFF);
		}

		// Skill keybind
		String key = null;
		if(ArterionClientMod.IS_LABY) {
			key = LabyKeybinds.getLabyKeybind(slot+1, skillDataEntry.getSkillName());
		}else {
			int code = -10000;
			for(Keybind keybind : KeybindManager.list) {
				if(keybind instanceof CommandKeybind) {
					String command = ((CommandKeybind)keybind).command;
					if(command.toLowerCase().startsWith("/skillslot ")) {
						String slotStr = command.substring("/skillslot ".length());
						if(slotStr.equals((slot+1)+"")) {
							code = keybind.keycode;
							key = ChatColor.stripColor(KeybindManager.getKeyText(code));
							break;
						}
					}
					if(command.toLowerCase().startsWith("/skill ")) {
						String nameStr = command.substring("/skill ".length());
						if(nameStr.replace("_", " ").equalsIgnoreCase(ChatColor.stripColor(skillDataEntry.getSkillName()))) {
							code = keybind.keycode;
							key = ChatColor.stripColor(KeybindManager.getKeyText(code));
							break;
						}
					}
				}
			}
		}
		if (key!=null) {
			int keyWidth = 4 + mc.fontRendererObj.getStringWidth(key) + 3;
			int keyHeight = 15;

			x = x + slotWidth/2 - keyWidth/2;
			y = y + slotHeight - keyHeight/2;

			int black = 0xFF000000;
			int color = outerColor;

			// Background
			this.drawRect(x + 1, y, x + keyWidth - 1, y + 1, black);
			this.drawRect(x + 1, y + keyHeight - 1, x + keyWidth - 1, y + keyHeight, black);
			this.drawRect(x, y + 1, x + 1, y + keyHeight - 1, black);
			this.drawRect(x + keyWidth - 1, y + 1, x + keyWidth, y + keyHeight - 1, black);
			this.drawRect(x + 2, y + 2, x + keyWidth - 2, y + keyHeight - 2, black);

			// Border
			this.drawRect(x + 1, y + 1, x + keyWidth - 1, y + 2, color);
			this.drawRect(x + 1, y + keyHeight - 2, x + keyWidth - 1, y + keyHeight - 1, color);
			this.drawRect(x + 1, y + 1, x + 2, y + keyHeight - 1, color);
			this.drawRect(x + keyWidth - 2, y + 1, x + keyWidth - 1, y + keyHeight - 1, color);
			
			// Key itself
			this.drawString(mc.fontRendererObj, key, x+4, y + 4, color);
		}

		GlStateManager.color(1f, 1f, 1f, 1f);
	}

	/**
	 * Render an ItemStack. Args : stack, x, y
	 */
	private void drawItemStack(ItemStack stack, int x, int y) {
		RenderHelper.disableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();

		RenderItem itemRender = mc.getRenderItem();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 32.0F);
		this.zLevel = -170.0F;
		itemRender.zLevel = -170.0F;
		itemRender.renderItemAndEffectIntoGUI(stack, x, y);
		this.zLevel = -2000.0F;
		itemRender.zLevel = 0.0F;
		GlStateManager.popMatrix();
	}
}
