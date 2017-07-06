package immunity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ISchedulableAction;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class PlasmaMembrane {
	private static ContinuousSpace<Object> space;
	private static Grid<Object> grid;
	double scale = CellProperties.getInstance().getCellK().get("orgScale");
/*	PM SUPERFICIE Y CELL VOLUME
	Considerando que la superficie de la PM es de 750 nm X el alto.  Qué alto considero??
	Puede ser 200 nm.  Para 200 me queda 150.000 ( o sea la superficie de 30 endosomas)

	Para la Cell, el volumen sería de 750x750x200= 112.500.000.  Otro modo es considerar que los endosomas ocupan 1% del volumen y que el citosol ocupa un 50% del volumen celular.  Con ello, la suma de los volúmenes de los endosomas en este sistema es de aproximadamente 2 millones.  SI esto es el 1%, entonces corresponden a un volumen celular de 200 millones.  De estos el 50% es citosol, que deja una estimación de 100 millones. 
	Entonces voy a considerar el cell.volume = 100x10^6 nm3*/

	public double area = 150000/scale;//750x200 nm2
	// a single Cell is created
	private static PlasmaMembrane instance;
	static {
		instance = new PlasmaMembrane(space, grid);
	}
	

	public HashMap<String, Double> membraneRecycle = new HashMap<String, Double>(CellProperties.getInstance().getMembraneRecycle()); // contains membrane recycled 
	public HashMap<String, Double> solubleRecycle = new HashMap<String, Double>();// contains soluble recycled
	public int pmcolor = 0;
	public int red = 0;
	public int green = 0;	
	public int blue = 0;
	public double c2 = CellProperties.getInstance().getMembraneRecycle().get("pLANCL2");


	// Constructor
	public PlasmaMembrane(ContinuousSpace<Object> space, Grid<Object> grid) {
// Contains the contents that are in the cell.  It is modified by Endosome that uses and changes the cell
// contents.	tMembranes, membrane and soluble content recycling, cytosolic Rabs	
		CellProperties cellProperties = CellProperties.getInstance();
		membraneRecycle.putAll(cellProperties.membraneRecycle);
		System.out.println("membraneRecycle "+ membraneRecycle);
		for (String met : cellProperties.solubleMet ){
		solubleRecycle.put(met,  0.0);
		}
			
	}
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		if (Math.random() < 0.001){
			System.out.println("llamo PM LANCL2");
			PlasmaMembraneLANCL2metabolismStep.LANCL2metabolism(PlasmaMembrane.getInstance());			
		}
		this.changeColor();

		}
	
	public void changeColor() {
		double c1 = 0;
		if (PlasmaMembrane.getInstance().getMembraneRecycle().containsKey("pLANCL2")){
		c1 = PlasmaMembrane.getInstance().getMembraneRecycle().get("pLANCL2");
		}
		this.pmcolor = (int) (c1/c2*240);
		System.out.println(PlasmaMembrane.getInstance().getMembraneRecycle()+"\n COLOR PLASMA  " + pmcolor+" " + c1 +" " + c2);
		}
	
/*	I will consider only red for pLANCL2.  PROBLEM, what could be the area of the PM?
 * public double getRed() {
		// double red = 0.0;
		String contentPlot = CellProperties.getInstance().getColorContent()
				.get("red");

		if (membraneRecycle.containsKey(contentPlot)) {
			double red = membraneRecycle.get(contentPlot) / area;
			return red;
		} else {return 0;}
	}

	public double getGreen() {
		// double red = 0.0;
		String contentPlot = CellProperties.getInstance().getColorContent()
				.get("green");

		if (membraneRecycle.containsKey(contentPlot)) {
			double green = membraneRecycle.get(contentPlot) / area;
			// System.out.println("mHCI content" + red);
			return green;

		} else  {return 0;}
	}

	public double getBlue() {
		// double red = 0.0;
		String contentPlot = CellProperties.getInstance().getColorContent()
				.get("blue");

		if (membraneRecycle.containsKey(contentPlot)) {
			double blue = membraneRecycle.get(contentPlot) / area;
			return blue;
			} else {return 0;}
	}

*/	
	// GETTERS AND SETTERS (to get and set Cell contents)
	public static PlasmaMembrane getInstance() {
		return instance;
	}

	
//	public void setRabCell(HashMap<String, Double> rabCell) {
//	this.rabCell = rabCell;
//}

	public HashMap<String, Double> getMembraneRecycle() {
		return membraneRecycle;
	}

//	public void setMembraneRecycle(HashMap<String, Double> membraneRecycle) {
//		this.membraneRecycle = membraneRecycle;
//	}

	public HashMap<String, Double> getSolubleRecycle() {
		return solubleRecycle;
	}

	public int getPmcolor() {
		return pmcolor;
	}

//	public void setSolubleRecycle(HashMap<String, Double> solubleRecycle) {
//		this.solubleRecycle = solubleRecycle;
//	}


}