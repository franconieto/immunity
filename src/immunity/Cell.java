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

public class Cell {
	// a single Cell is created
	private static Cell instance;
	static {
		instance = new Cell();
	}
	
//	Cell characteristics
	public static double rcyl = CellProperties.getInstance().getCellK().get("rcyl");//10.0; // radius tubule
//	public radius new endosome formed by uptake = radius Kind1, generally 20.0; // 
	public static double mincyl = 6 * Math.PI * rcyl * rcyl; // surface minimum cylinder
	// two radius large (almost a sphere)
	public static double rIV = 15; // Internal vesicle radius
//	public static double vEndo = 4d / 3d * Math.PI * Math.pow(rEndo, 3); //volume new endosome
//	public static double sEndo = 4d * Math.PI * Math.pow(rEndo, 2); // surface new endosome
	// mincyl surface = 1884.95 volume = 6283.18
	public static double orgScale = CellProperties.getInstance().getCellK().get("orgScale");
//  When orgScale=1 zoom =0, when > 1 zoom in , when <1 zoom out
//	global cell and non-cell quantities
/*	PM SUPERFICIE Y CELL VOLUME
	Considerando que la superficie de la PM es de 750 nm X el alto.  Qué alto considero??
	Puede ser 200 nm.  Para 200 me queda 150.000 ( o sea la superficie de 30 endosomas)

	Para la Cell, el volumen sería de 750x750x200= 112.500.000.  Otro modo es considerar que los endosomas ocupan 1% del volumen y que el citosol ocupa un 50% del volumen celular.  Con ello, la suma de los volúmenes de los endosomas en este sistema es de aproximadamente 2 millones.  SI esto es el 1%, entonces corresponden a un volumen celular de 200 millones.  De estos el 50% es citosol, que deja una estimación de 100 millones. 
	Entonces voy a considerar el cell.volume = 100x10^6 nm3*/

	public static double volume = 1E08/orgScale; //volume in nm3
	public double tMembrane = 0;// membrane that is not used in endosomes
	public HashMap<String, Double> rabCell = new HashMap<String, Double>();// contains rabs free in cytosol
	public HashMap<String, Double> membraneCell = new HashMap<String, Double>(); // contains membrane factors within the cell 
	public HashMap<String, Double> solubleCell = new HashMap<String, Double>();// contains soluble factors within the cell

	// Constructor
	public Cell() {
// Contains factors that are in the cell without specifying organelle or position.
// It is modified by Endosome that uses and changes cytosolic Rabs
// contents.	tMembranes, membrane and soluble content recycling,
		solubleCell.putAll(CellProperties.getInstance().getSolubleCell());
		rabCell.putAll(CellProperties.getInstance().getInitRabCell());
		tMembrane = CellProperties.getInstance().cellK.get("tMembrane");
	}
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		if (Math.random() < 0.01){
			System.out.println("llamo CELL MREGDIFF");
			CellMregDiffStep.mregDiff(Cell.getInstance());			
		}

	}
	// GETTERS AND SETTERS (to get and set Cell contents)
	public static Cell getInstance() {
		return instance;
	}

	public double gettMembrane() {
		return tMembrane;
	}

	public void settMembrane(double tMembrane) {
		this.tMembrane = tMembrane;
	}
	
	public HashMap<String, Double> getRabCell() {
		return rabCell;
	}
	
	public HashMap<String, Double> getMembraneCell() {
		return membraneCell;
	}
	public HashMap<String, Double> getSolubleCell() {
		return solubleCell;
	}
//	public void setRabCell(HashMap<String, Double> rabCell) {
//	this.rabCell = rabCell;
//}



//	public void setMembraneRecycle(HashMap<String, Double> membraneRecycle) {
//		this.membraneRecycle = membraneRecycle;
//	}



//	public void setSolubleRecycle(HashMap<String, Double> solubleRecycle) {
//		this.solubleRecycle = solubleRecycle;
//	}


}