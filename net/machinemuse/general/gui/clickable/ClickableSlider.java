package net.machinemuse.general.gui.clickable;

import java.util.List;

import net.machinemuse.general.geometry.Colour;
import net.machinemuse.general.geometry.MuseRenderer;
import net.machinemuse.general.geometry.Point2D;
import net.machinemuse.powersuits.item.ModularCommon;
import net.minecraft.nbt.NBTTagCompound;

public class ClickableSlider extends Clickable {
	protected NBTTagCompound moduleTag;
	protected String name;
	protected double width;
	protected static final int cornersize = 2;
	
	public ClickableSlider(Point2D topmiddle, double width, NBTTagCompound moduleTag, String name) {
		this.moduleTag = moduleTag;
		this.name = name;
		this.position = topmiddle;
		this.width = width;
	}
	
	@Override public void draw() {
		MuseRenderer.drawCenteredString(name, position.x(), position.y());
		MuseRenderer.drawFrameRect(
				position.x() - width / 2.0 - cornersize, position.y() + 10,
				position.x() + width / 2.0 + cornersize, position.y() + 24,
				Colour.LIGHTBLUE, Colour.DARKBLUE, 1, cornersize);
		double value = ModularCommon.getOrSetModuleProperty(moduleTag, name, 0);
		
		MuseRenderer.drawFrameRect(
				position.x() - width / 2.0 - cornersize, position.y() + 10,
				position.x() + width * (value - 0.5) + cornersize, position.y() + 24,
				Colour.LIGHTBLUE, Colour.ORANGE, 1, cornersize);
	}
	
	@Override public boolean hitBox(double x, double y) {
		double xval = position.x() - x;
		double yval = position.y() + 17 - y;
		if (Math.abs(xval) < width / 2 && Math.abs(yval) < 7) {
			return true;
		} else {
			return false;
		}
	}
	public String getName() {
		return name;
	}
	public double getValue() {
		double val = 0;
		if (moduleTag.hasKey(name)) {
			val = moduleTag.getDouble(name);
		}
		return val;
	}
	public void moveSlider(double x, double y) {
		double xval = position.x() - x;
		double xratio = 0.5 - (xval / width);
		xratio = Math.max(Math.min(xratio, 1.0), 0.0); // Clamp
		moduleTag.setDouble(name, xratio);
		
	}
	
	@Override public List<String> getToolTip() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
