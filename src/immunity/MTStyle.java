package immunity;

import java.awt.Color;
import java.awt.Font;

import javax.media.opengl.GL2;

import repast.simphony.visualizationOGL2D.StyleOGL2D;
import saf.v3d.ShapeFactory2D;
import saf.v3d.render.RenderState;
import saf.v3d.scene.Position;
import saf.v3d.scene.VShape;
import saf.v3d.scene.VSpatial;

public class MTStyle implements StyleOGL2D<MT> {

	ShapeFactory2D factory;
	private double String;
	
	@Override
	public void init(ShapeFactory2D factory) {
		this.factory = factory;

	}

	@Override
	public VSpatial getVSpatial(MT object, VSpatial spatial) {
		////double s = object.getArea();
		//double v = object.getVolume();
		//int svr =(int) ((s * s * s) / (v * v)/ (113d));
		//VSpatial createCircle = this.factory.createRectangle(10, 10 * svr);
		//return createCircle;
		return null;
	}

	@Override
	public Color getColor(MT object) {
		//int f = Math.abs( (int)object.getArea() % 256 );
		return new Color(0, 0, 0);
	}

	@Override
	public int getBorderSize(MT object) {
		return 0;
	}

	@Override
	public Color getBorderColor(MT object) {
		return new Color(100);
	}

	@Override
	public float getRotation(MT object) {
		return (float) Math.random() * 360f;
	}

	@Override
	public float getScale(MT object) {
		
		return (float) 10; //object.size() / 10f;
	}

	@Override
	public String getLabel(MT object) {
		return "radius "; // + Math.abs((int)object.size());
	}
	@Override
	public Font getLabelFont(MT object) {
		return new Font("sansserif", Font.PLAIN, 14);
	}

	@Override
	public float getLabelXOffset(MT object) {
		return 0;
	}

	@Override
	public float getLabelYOffset(MT object) {
		return 0;
	}

	@Override
	public Position getLabelPosition(MT object) {
		return Position.CENTER;
	}

	@Override
	public Color getLabelColor(MT object) {
		return new Color(100);
	}

}
