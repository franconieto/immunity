package immunity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class EndosomeTetherStep {

		private static ContinuousSpace<Object> space;
		private static Grid<Object> grid;
		
		public static void tether (Endosome endosome) {
			HashMap<String, Double> rabContent = new HashMap<String, Double>(endosome.getRabContent());
			HashMap<String, Double> membraneContent = new HashMap<String, Double>(endosome.getMembraneContent());
			HashMap<String, Double> solubleContent = new HashMap<String, Double>(endosome.getSolubleContent());
			space = endosome.getSpace();
			grid = endosome.getGrid();
			double cellLimit = 3 * Cell.orgScale;

		GridPoint pt = grid.getLocation(endosome);
		// I calculated that the 50 x 50 grid is equivalent to a 750 x 750 nm
		// square
		// Hence, size/15 is in grid units
		int gridSize = (int) Math.round(endosome.size*Cell.orgScale / 15d);
		GridCellNgh<Endosome> nghCreator = new GridCellNgh<Endosome>(grid, pt,
				Endosome.class, gridSize, gridSize);
		// System.out.println("SIZE           "+gridSize);

		List<GridCell<Endosome>> cellList = nghCreator.getNeighborhood(true);
		if (cellList.size()<2)return;//if only one return
		List<Endosome> endosomesToTether = new ArrayList<Endosome>();
		for (GridCell<Endosome> gr : cellList) {
			// include all endosomes
			for (Endosome end : gr.items()) {
				if (EndosomeAssessCompatibility.compatibles(endosome, (Endosome) end)) {
					endosomesToTether.add(end);
				}
			}
		}
		
		// new list with just the compatible endosomes (same or compatible rabs)
		if (endosomesToTether.size()<2)return; //if only one, return
		// select the largest endosome
		Endosome largest = endosome;
		for (Endosome end : endosomesToTether) {
//			System.out.println(endosome.size+" "+end.size);
			if (end.size > largest.size) {
				largest = end;
			}
		}
//		Takes at random a membrane domain of the largest endosome.  Larger domains have larger probability of being selected
//		if the domain is a Golgi domain, then tether probability is 1, else is 0.1
		if (Math.random() > tetherProbability(largest)) return;
		
		// assign the speed and heading of the largest endosome to the gropu

		for (Endosome end : endosomesToTether) {

			Random r = new Random();
			double rr = r.nextGaussian();
			end.heading = rr * 30d + largest.heading;
	//		EndosomeMove.moveTowards(end);
		}
	}
//		Takes at random a membrane domain of the largest endosome.  
//		Larger domains have larger probability of being selected
		public static double tetherProbability(Endosome endosome) {
//			Picks a Rab domain according to the relative area of the domains in the organelle
//			More abundant Rabs have more probability of being selected
//			Returns the moving properties on MT of this domain 
			double rnd = Math.random();// select a random number
			double mtd = 0d;
//			Start adding the rabs domains present in the organelle until the value is larger than the random number selected
			for (String rab : endosome.rabContent.keySet()) {
				mtd = mtd + endosome.rabContent.get(rab) / endosome.area;
				if (rnd <= mtd) {
					String tetherRab = CellProperties.getInstance().rabOrganelle.get(rab);//   mtTropism.get(rab);
					double tetherProbability = 0d;
					if (tetherRab.contains("Golgi")) tetherProbability = 1d; else tetherProbability = 0.1;
					return tetherProbability;
				}
			}
			return 0;// never used
		}
	
}
