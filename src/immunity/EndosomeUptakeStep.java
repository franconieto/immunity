package immunity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections.map.HashedMap;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class EndosomeUptakeStep {
	private static ContinuousSpace<Object> space;
	private static Grid<Object> grid;
	private static Object membreneMet;
	private static HashMap<String, Double> rabContent;
	public static boolean loadUptake = true;
	
	public static void uptake(Endosome endosome) {
		//		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		//		if (tick < 100) return;
		space = endosome.getSpace();
		grid = endosome.getGrid();
		HashMap<String, String> rabCode = new HashMap<String, String>();
		rabCode.put("RabA", "kind1");
		rabCode.put("RabB", "kind2");
		rabCode.put("RabC", "kind3");
		rabCode.put("RabD", "kind4");
		rabCode.put("RabE", "kind5");

		double membraneFlux = CellProperties.getInstance().cellK.get("membraneFlux"); // if on then a cistern is formed from zero for membrane flux model
		if (membraneFlux == 1d){
			double maturationTrigger = CellProperties.getInstance().cellK.get("maturationTrigger");
			double totalRabA = Results.getInstance().getTotalRabs().get("RabA");
			double initialTotalRabA = Results.getInstance().getInitialTotalRabs().get("RabA");
			//		System.out.println("totalRabs  "+totalRabs);
//			if (totalRabA > maturationTrigger * initialTotalRabA) {// new RabA cistern only if 50% has matured
//				return;
//			}
//			else
			{
			String selectedRab = "RabA";
			newOrganelle(endosome, selectedRab, rabCode, membraneFlux);
			return;
			}
		}

		Cell cell = Cell.getInstance();
		HashMap<String, Double> totalRabs = new HashMap<String, Double>(Results.getInstance().getTotalRabs());
		HashMap<String, Double> initialTotalRabs = new HashMap<String, Double>(Results.getInstance().getInitialTotalRabs());
		//		System.out.println("totalRabs  "+totalRabs);
		HashMap<String, Double> deltaRabs = new HashMap<String, Double>();  


		for (String rab : totalRabs.keySet()){
			//			System.out.println("ErrorRabs  "+ rab + "   " +initialTotalRabs.get(rab) +"     "+ totalRabs.get(rab));
			double value = initialTotalRabs.get(rab) - totalRabs.get(rab);
			deltaRabs.put(rab, value);
		}

		//	System.out.println("Initial Rabs  "+ initialTotalRabs + " \n delta Rabs"+ deltaRabs);
		double largeDelta = 0d;
		String selectedRab = "";
		for (String rab : deltaRabs.keySet()){
			if (deltaRabs.get(rab)>largeDelta) {
				selectedRab=rab;
				largeDelta=deltaRabs.get(rab);
			}	
		}
		//		System.out.println("selected Rab for uptake "+ selectedRab);
		//		If no rab was selected or the surface required is small (less than a sphere of 60 nm radius, 
		//		no uptake is required
		if (selectedRab.equals("")|| deltaRabs.get(selectedRab)<45000) return;
		enlargeOrganelleGolgiVesicular(endosome, selectedRab, rabCode, membraneFlux);

	}
		
	private static void newUptake(Endosome endosome, String selectedRab) {
		space = endosome.getSpace();
		grid = endosome.getGrid();
		double cellLimit = 3d * Cell.orgScale;
		System.out.println("UPTAKE INITIAL ORGANELLES " +	InitialOrganelles.getInstance().getInitOrgProp().get("kind1"));
		HashMap<String, Double> initOrgProp = new HashMap<String, Double>(
				InitialOrganelles.getInstance().getInitOrgProp().get("kind1"));
		
//		System.out.println("PROPIEDADES RAB A  "+initOrgProp);
//		System.out.println("A VER?" + InitialOrganelles.getInstance().getInitOrgProp().get("kind2"));
		HashMap<String, Double> rabCell = Cell.getInstance().getRabCell();

		if (!rabCell.containsKey("RabA") || Math.random()>rabCell.get("RabA")){
			return;}
	//			double rabCellA = rabCell.get("RabA");
	// cytosolic RabA is always provided by the -> RabAc reaction.  Only in a KD will go down
	//		Then no uptake if no RabA in cyto.  The uptake is proportional to the amount of RabA	
	//		Uptake generate a new RabA organelle.  The size is the radius in the csv file for RabA
	//		initial organelles.  The content is specified in concentration. One (1) is approx 1 mM.
	//		This units are converted to membrane or volume units by multiplying by the area (rabs and
	//		membrane content) or volume (soluble content).  For uptake it is controlled that there is enough
	//		membrane and there is RabA in the cell

		double maxRadius = initOrgProp.get("maxRadius");
		double minRadius = Cell.rcyl*1.1;
		double a = RandomHelper.nextDoubleFromTo(minRadius,maxRadius);				
		double c = a + a  * Math.random() * initOrgProp.get("maxAsym");

		double f = 1.6075;
		double af= Math.pow(a, f);
		double cf= Math.pow(c, f);
		double area = 4d* Math.PI*Math.pow((af*af+af*cf+af*cf)/3, 1/f);
		double volume = 4d/3d*Math.PI*a*a*c;
		initOrgProp.put("area", area);
		double value = Results.instance.getTotalRabs().get("RabA");
		value = value + area;
		Results.instance.getTotalRabs().put("RabA", value);
		initOrgProp.put("volume", volume);
//		System.out.println("PROPIEDADES RAB A  "+initOrgProp);
//		try {
//			Thread.sleep(4000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}


		HashMap<String, Double> rabContent = new HashMap<String, Double>();
	//			UPTAKE ENDOSOME JUST RABA
		rabContent.put("RabA", area);

	// 		Soluble and membrane content of the kind1, but cMHCI and mHCI depends now on PM content
		HashMap<String, Double> membraneContent = new HashMap<String,Double>();
		Set<String> membraneMet = new HashSet<String>(CellProperties.getInstance().getMembraneMet());
		for (String mem : membraneMet){
			double valueInEn = 0d;
			double valueInPM =0d;
			double valueInTotal = 0d;

			if (PlasmaMembrane.getInstance().getMembraneRecycle().containsKey(mem))
			{
				double valuePM = PlasmaMembrane.getInstance().getMembraneRecycle().get(mem);
				valueInPM = valuePM * CellProperties.getInstance().getUptakeRate().get(mem) * area/ PlasmaMembrane.getInstance().getPlasmaMembraneArea();	

				if (valueInPM >= area) 
				{
					membraneContent.put(mem, area);
					// decrease PM content
					PlasmaMembrane.getInstance().getMembraneRecycle().put(mem, valuePM - area);
					//			System.out.println(mem + valuePM + "   UPTAKE DECREASE 1111  " + valueInPM);
					continue;
				}
				// decrease PM content
				PlasmaMembrane.getInstance().getMembraneRecycle().put(mem, valuePM-valueInPM);
				//			System.out.println(mem+valuePM +"           UPTAKE DECREASE 2222222222222222222222222222  " + valueInPM);
				valueInTotal = valueInPM;
			}
			if (InitialOrganelles.getInstance().getInitMembraneContent().get("kind1").containsKey(mem))
			{
				valueInEn = InitialOrganelles.getInstance().getInitMembraneContent().get("kind1").get(mem)*area;
				valueInTotal = valueInEn + valueInPM;
			}
			if (valueInTotal >= area) 	
			{
				valueInTotal= area;
			}
			membraneContent.put(mem, valueInTotal);	

		}
//	System.out.println("RRRRRRRRRRRRRRRRRRRRRREEEEEEEEEEEEEEEEEEEESSSSSSSSSSSSSSSS "+ membraneContent);
		HashMap<String, Double> solubleContent = new HashMap<String,Double>();
		Set<String> solubleMet = new HashSet<String>(CellProperties.getInstance().getSolubleMet());
		for (String sol : solubleMet){
			double valueInEn = 0d;
			double valueInPM =0d;
			
			if (PlasmaMembrane.getInstance().getSolubleRecycle().containsKey(sol))
			{
				double valuePM = PlasmaMembrane.getInstance().getSolubleRecycle().get(sol);
				valueInPM = valuePM * volume/ PlasmaMembrane.getInstance().getPlasmaMembraneVolume();	

				if (valueInPM >= volume) 
				{
				solubleContent.put(sol, volume);
				// decrease PM content
				PlasmaMembrane.getInstance().getSolubleRecycle().put(sol, valuePM - volume);
				continue;
				}
				// decrease PM content
				PlasmaMembrane.getInstance().getSolubleRecycle().put(sol, valuePM-valueInPM);
			}
			if (InitialOrganelles.getInstance().getInitSolubleContent().get("kind1").containsKey(sol))
			{
				valueInEn = InitialOrganelles.getInstance().getInitSolubleContent().get("kind1").get(sol)*volume;
				valueInEn = valueInEn + valueInPM;
			}
			if (valueInEn >= volume) 	valueInEn= volume;
			solubleContent.put(sol, valueInEn);	

		}
//		HashMap<String, Double> solubleContent = new HashMap<String, Double>(InitialOrganelles.getInstance().getInitSolubleContent().get("kind1"));
//		for (String sol : solubleContent.keySet()){
//			double ss = solubleContent.get(sol);
//			solubleContent.put(sol, ss*volume);
//		}
		solubleContent.put("proton", 3.98E-5*volume); //pH 7.4
	// new endosome incorporate PM components in a proportion area new/area PM
	//					"Fully conformed MHC-I proteins internalize with the
	//					rate 0.002–0.004 min−1 (0.2–0.4% loss of initially surface expressed
	//					molecules per minute, i.e. 12–24% per hour) which is 5–8-fold slower
	//					than IR of their open forms (0.011–0.022 min−1), 
	//					Molecular Immunology 55 (2013) 149– 152 (Pero Lucin)"
	//One 60 radius endosomes has an area of about 45000 nm.  This is about 7.5% of the 1500 x 400 nm of
	//the PM considered at the 1 organelle scale.  So to internalize 0.3%. a factor of 0.04 (0.3%/7.5%) is applied.
	//Hence, factor = endosome area/ PM area * 0.04 for cMHCIa and pept-mHCI, and endosome area/ PM area * 0.2 for mHCI
	/*				
My problem is that I do not know the rate of uptake that depends on how much Kind1(Rab5) organelles are 
switched to Kind4(Rab7).  I guess is that the rate will have to be relative.  1 for open MHCI and 0.15 for closed.


	 */					
	/*			double cMHCIvalueIn = PlasmaMembrane.getInstance().getMembraneRecycle().get("cMHCI");
		double cMHCIvalue = cMHCIvalueIn * 0.4 * area/ (double) PlasmaMembrane.getInstance().area;
		membraneContent.put("cMHCI", cMHCIvalue);
		PlasmaMembrane.getInstance().getMembraneRecycle().put("cMHCI", cMHCIvalueIn - cMHCIvalue);

		double mHCIvalueIn = PlasmaMembrane.getInstance().getMembraneRecycle().get("mHCI");
		double mHCIvalue = mHCIvalueIn * 1 * area/ (double) PlasmaMembrane.getInstance().area;
		membraneContent.put("mHCI", mHCIvalue);
		PlasmaMembrane.getInstance().getMembraneRecycle().put("mHCI", mHCIvalueIn - mHCIvalue);
	 *///			
	//			for (String met : PlasmaMembrane.getInstance().getMembraneRecycle().keySet()){
	//				double valueIn = PlasmaMembrane.getInstance().getMembraneRecycle().get(met);
	//				value = 0.001 * valueIn * initOrgProp.get("area")/PlasmaMembrane.getInstance().area;
	//				if (value > area) {value = area;}


//		System.out.println("PLASMA MEMBRANE "+PlasmaMembrane.getInstance().getMembraneRecycle());

		//			Cell.getInstance().settMembrane(tMembrane);

		// Cell.getInstance().setRabCell(rabCell);
		Context<Object> context = ContextUtils.getContext(endosome);

		Endosome bud = new Endosome(space, grid, rabContent, membraneContent,
				solubleContent, initOrgProp);
		context.add(bud);
		//			tMembrane = tMembrane - bud.initOrgProp.get("area");
//		bud.area = initOrgProp.get("area");
//		bud.volume = initOrgProp.get("volume");
//		bud.size = initOrgProp.get("maxRadius");// radius of a sphere with the volume of the
		// cylinder
		bud.speed = 1d / bud.size;
		bud.heading = -90;// heading down
		// NdPoint myPoint = space.getLocation(bud);
		double rnd = Math.random();
		double upPosition = 25 + rnd* (25 - 4 * cellLimit);
		endosome.getSpace().moveTo(bud, rnd * 50, upPosition);
		endosome.getGrid().moveTo(bud, (int) rnd * 50, (int) upPosition);
		
		//			System.out.println(area + "NEW UPTAKE" + bud.membraneContent);
		//			try {
		//			TimeUnit.SECONDS.sleep(5);
		//		} catch (InterruptedException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
	PlasmaMembrane.getInstance().getPlasmaMembraneTimeSeries().clear();
		
	}

	private static void newOrganelle(Endosome endosome, String selectedRab, HashMap<String, String> rabCode, double membraneFlux) {
		String kind = rabCode.get(selectedRab);
		HashMap<String, Double> initOrgProp = new HashMap<String, Double>(InitialOrganelles.getInstance().getInitOrgProp().get(kind));
		HashMap<String, Double> membraneContent = new HashMap<String, Double>(InitialOrganelles.getInstance().getInitMembraneContent().get(kind));
		HashMap<String, Double> solubleContent = new HashMap<String, Double>(InitialOrganelles.getInstance().getInitSolubleContent().get(kind));
		HashMap<String, Double> rabContent = new HashMap<String, Double>(InitialOrganelles.getInstance().getInitRabContent().get(kind));
//		HashMap<String, Double> initialTotalRab = new HashMap<String, Double>(Results.initialTotalRabs);
//		HashMap<String, Double> totalRab = new HashMap<String, Double>(Results.totalRabs);
//		double initialTotalArea = 0d;
//		double totalArea = 0d;
		double area = 0d;
		double volume = 0d;
		if (membraneFlux == 1d){
			// if membraneFlux is ON then a new ERGIC like structure is formed as a kind1 organelle
			
//			To conditionate the size of the new organelle to the total area of the system
//			for (String rab : initialTotalRab.keySet()){
//				initialTotalArea = initialTotalArea + initialTotalRab.get(rab);
//			}
//			for (String rab : totalRab.keySet()){
//				totalArea = totalArea + totalRab.get(rab);
//			}
//			area = initialTotalArea - totalArea;
			double maxRadius = initOrgProp.get("maxRadius");
////			To find the radius of a cistern 20nm high from the area
////			area = 2*PI*radius^2 + 2*PI*radius*20
////			quadratic equation 0 = a x^2 + b x + c
////			solution x = (-b+/- sqr(b^2 - 4*a*c)/2*a
////			quadratic equation 0 = 2*PI* r^2 + 2*PI*20* r - area
//			double aq = 2d*Math.PI;
//			double bq = 40*Math.PI;
//			double cq = -area;
//			double dq =  bq * bq - 4 * aq * cq;
//			double root1 = (-bq + Math.sqrt(dq))/(2*aq);
//			System.out.println("RADIO UPTAKE"+root1);
			area = Math.PI*Math.pow(maxRadius, 2)*2d + 2d*maxRadius*Math.PI*20d; //area of a cistern as a flat cylinder 20 nm high with a radius of maxRadius
//			volume = Math.PI*Math.pow(root1, 2)* 20d;// volume of the cylinder
			volume = Math.PI*Math.pow(maxRadius, 2)* 20d;
			double value = Results.instance.getTotalRabs().get(selectedRab);
			value = value + area;
			Results.instance.getTotalRabs().put(selectedRab, value);
			initOrgProp.put("area", area);
			initOrgProp.put("volume", volume);
			
						for (String rab : rabContent.keySet()){
							double rr = rabContent.get(rab);
							rabContent.put(rab, rr*area);
						}
//						HashMap<String, Double> membraneContent = new HashMap<String, Double>(InitialOrganelles.getInstance().getInitMembraneContent().get(kind));
						for (String mem : membraneContent.keySet()){
//							if (membraneContent.get(mem) == 0) {
//								InitialOrganelles.getInstance().getInitMembraneContent().get(kind).remove(mem);
//								//membraneContent.remove(mem);
//								}
//							else if (mem.equals("membraneMarker")){
//								membraneContent.put(mem, 1d);
//								InitialOrganelles.getInstance().getInitMembraneContent().get(kind).remove("membraneMarker");
//							}
//							else {
							double mm = membraneContent.get(mem);
							membraneContent.put(mem, mm*area);
							}
						
			System.out.println(selectedRab + InitialOrganelles.getInstance().getInitMembraneContent().get(kind)+ " membraneFlux	iniciales " +membraneContent);
//						HashMap<String, Double> solubleContent = new HashMap<String, Double>(InitialOrganelles.getInstance().getInitSolubleContent().get(kind));
						for (String sol : solubleContent.keySet()){
//							if (solubleContent.get(sol).equals(0d)) {
//								InitialOrganelles.getInstance().getInitMembraneContent().get(kind).remove(sol);
//								//solubleContent.remove(sol);
//								}
//							else if (sol.equals("solubleMarker") ){
//								solubleContent.put(sol, 1d);
//								InitialOrganelles.getInstance().getInitSolubleContent().get(kind).remove("solubleMarker");
//							}
//							else {
								double ss = solubleContent.get(sol);
							solubleContent.put(sol, ss*volume);
							}
						

			
		}
		else {

		//		double tMembrane = Cell.getInstance().gettMembrane();
		double maxRadius = initOrgProp.get("maxRadius");
		double maxAsym = initOrgProp.get("maxAsym");
		double minRadius = Cell.rcyl*1.1;
		double a = RandomHelper.nextDoubleFromTo(minRadius,maxRadius);				
		double c = a + a  * Math.random()* maxAsym;
		double f = 1.6075;
		double af= Math.pow(a, f);
		double cf= Math.pow(c, f);
		area = 4d* Math.PI*Math.pow((af*af+af*cf+af*cf)/3, 1/f);
		volume = 4d/3d*Math.PI*a*a*c;
		double value = Results.instance.getTotalRabs().get(selectedRab);
		value = value + area;
		Results.instance.getTotalRabs().put(selectedRab, value);
		initOrgProp.put("area", area);
		initOrgProp.put("volume", volume);	
//		HashMap<String, Double> rabContent = new HashMap<String, Double>();
		rabContent.put(selectedRab, area);
// If membraneFlux is not ON

		HashSet<String> solubleMet = new HashSet<String>(CellProperties.getInstance().getSolubleMet());
		HashSet<String> membraneMet = new HashSet<String>(CellProperties.getInstance().getMembraneMet());
//	This is getting the keyset of the membrane metabolisms
// MEMBRANE CONTENT.  For a new organelle, with the Rab that was selected to compensate lost, the membrane content is taken from the total
// membrane content associated to this rab/total area of the rab.  This is an average of the membrane content associated to the specific
// Rab.  Marker is set to zero.
		for (String mem : membraneMet){
			//				System.out.println(mem + "  MMEEMM " + selectedRab + "\n " + Results.getInstance().getContentDist());
			value = Results.getInstance().getContentDist().get(mem+selectedRab)
					/Results.getInstance().getTotalRabs().get(selectedRab);
			membraneContent.put(mem, value * area);
		}
		membraneContent.put("membraneMarker", 0d);
// SOLUBLE CONTENT.  For a new organelle, with the Rab that was selected to compensate lost, the soluble content is taken from the total
// soluble content associated to this rab/total volume surrounded by the rab.  This is an average of the soluble content associated to the specific
// Rab.  Marker and mvb is set to zero
		for (String sol : solubleMet){
			value = Results.getInstance().getContentDist().get(sol+selectedRab)
					/Results.getInstance().getTotalVolumeRabs().get(selectedRab);
			solubleContent.put(sol, value * volume);
		}
		solubleContent.put("mvb", 0d);
		solubleContent.put("solubleMarker", 0d);
		}
		Context<Object> context = ContextUtils.getContext(endosome);
		Endosome bud = new Endosome(endosome.getSpace(), endosome.getGrid(), rabContent, membraneContent,
				solubleContent, initOrgProp);
		System.out.println(bud.xcoor + " POSICIÓN "+bud.ycoor);
		System.out.println(space.toString() + grid.toString() + rabContent + membraneContent + solubleContent + initOrgProp);
		context.add(bud);
		bud.area = area;// initOrgProp.get("area");
		bud.volume = volume; //initOrgProp.get("volume");
//		bud.size = initOrgProp.get("maxRadius");// radius of a sphere with the volume of the
		// cylinder
		
		if (membraneFlux == 1d){
			bud.speed = 0d;
			bud.heading = -90;// heading down
			// NdPoint myPoint = space.getLocation(bud);
			endosome.getSpace().moveTo(bud, 25, 2);
			endosome.getGrid().moveTo(bud, 25, 2);		
		}
		else{
		bud.speed = 1d / bud.size;
		bud.heading = -90;// heading down
		// NdPoint myPoint = space.getLocation(bud);
		double rnd = Math.random();
		endosome.getSpace().moveTo(bud, rnd * 50, 10 + rnd* 30);
		endosome.getGrid().moveTo(bud, (int) rnd * 50, (int) (10 + rnd* 30));
		}
	}
	
	private static void newOrganelleGolgiVesicular(Endosome endosome, String selectedRab, HashMap<String, String> rabCode, double membraneFlux) {
		String kind = rabCode.get(selectedRab);
		HashMap<String, Double> initOrgProp = new HashMap<String, Double>(InitialOrganelles.getInstance().getInitOrgProp().get(kind));
		HashMap<String, Double> membraneContent = new HashMap<String, Double>(InitialOrganelles.getInstance().getInitMembraneContent().get(kind));
		HashMap<String, Double> solubleContent = new HashMap<String, Double>(InitialOrganelles.getInstance().getInitSolubleContent().get(kind));
		HashMap<String, Double> rabContent = new HashMap<String, Double>(InitialOrganelles.getInstance().getInitRabContent().get(kind));

		double area = 0d;
		double volume = 0d;
		double maxRadius = initOrgProp.get("maxRadius");
		area = Math.PI*Math.pow(maxRadius, 2)*2d + 2d*maxRadius*Math.PI*20d; //area of a cistern as a flat cylinder 20 nm high with a radius of maxRadius
		volume = Math.PI*Math.pow(maxRadius, 2)* 20d;
		double value = Results.instance.getTotalRabs().get(selectedRab);
		value = value + area;
		Results.instance.getTotalRabs().put(selectedRab, value);
		initOrgProp.put("area", area);
		initOrgProp.put("volume", volume);			
		for (String rab : rabContent.keySet()){
			double rr = rabContent.get(rab);
			rabContent.put(rab, rr*area);
		}
		for (String mem : membraneContent.keySet()){
			//							if (membraneContent.get(mem) == 0) {
			//								InitialOrganelles.getInstance().getInitMembraneContent().get(kind).remove(mem);
			//								//membraneContent.remove(mem);
			//								}
			//							else if (mem.equals("membraneMarker")){
			//								membraneContent.put(mem, 1d);
			//								InitialOrganelles.getInstance().getInitMembraneContent().get(kind).remove("membraneMarker");
			//							}
			//							else {
			double mm = membraneContent.get(mem);
			membraneContent.put(mem, mm*area);
		}

		System.out.println(selectedRab + InitialOrganelles.getInstance().getInitMembraneContent().get(kind)+ " membraneFlux	iniciales " +membraneContent);
		//						HashMap<String, Double> solubleContent = new HashMap<String, Double>(InitialOrganelles.getInstance().getInitSolubleContent().get(kind));
		for (String sol : solubleContent.keySet()){
			//							if (solubleContent.get(sol).equals(0d)) {
			//								InitialOrganelles.getInstance().getInitMembraneContent().get(kind).remove(sol);
			//								//solubleContent.remove(sol);
			//								}
			//							else if (sol.equals("solubleMarker") ){
			//								solubleContent.put(sol, 1d);
			//								InitialOrganelles.getInstance().getInitSolubleContent().get(kind).remove("solubleMarker");
			//							}
			//							else {
			double ss = solubleContent.get(sol);
			solubleContent.put(sol, ss*volume);
		}



		Context<Object> context = ContextUtils.getContext(endosome);
		Endosome bud = new Endosome(endosome.getSpace(), endosome.getGrid(), rabContent, membraneContent,
				solubleContent, initOrgProp);
		System.out.println(bud.xcoor + " POSICIÓN "+bud.ycoor);
		System.out.println(space.toString() + grid.toString() + rabContent + membraneContent + solubleContent + initOrgProp);
		context.add(bud);
		bud.area = area;// initOrgProp.get("area");
		bud.volume = volume; //initOrgProp.get("volume");
		//		bud.size = initOrgProp.get("maxRadius");// radius of a sphere with the volume of the
		// cylinder


		bud.speed = 0d;
		bud.heading = -90;// heading down
		// NdPoint myPoint = space.getLocation(bud);
		endosome.getSpace().moveTo(bud, 25, 2);
		endosome.getGrid().moveTo(bud, 25, 2);		


	}	
	
	private static void enlargeOrganelleGolgiVesicular(Endosome endosome, String selectedRab, HashMap<String, String> rabCode, double membraneFlux) {
		String kind = rabCode.get(selectedRab);
		HashMap<String, Double> initOrgProp = new HashMap<String, Double>(InitialOrganelles.getInstance().getInitOrgProp().get(kind));
		HashMap<String, Double> membraneContent = new HashMap<String, Double>(InitialOrganelles.getInstance().getInitMembraneContent().get(kind));
		HashMap<String, Double> solubleContent = new HashMap<String, Double>(InitialOrganelles.getInstance().getInitSolubleContent().get(kind));
		HashMap<String, Double> rabContent = new HashMap<String, Double>(InitialOrganelles.getInstance().getInitRabContent().get(kind));
		
		double area = 0d;
		double volume = 0d;
		double maxRadius = initOrgProp.get("maxRadius");
		area = Math.PI*Math.pow(maxRadius, 2)*2d + 2d*maxRadius*Math.PI * 20d; //area of a cistern as a flat cylinder 20 nm high with a radius of maxRadius
		volume = Math.PI*Math.pow(maxRadius, 2)* 20d; // ojo este volumen no lo puedo agregar para enlarge. El volumen final debe calcularse del área de la cisterna
		double value = Results.instance.getTotalRabs().get(selectedRab);
		value = value + area;
		Results.instance.getTotalRabs().put(selectedRab, value);
		initOrgProp.put("area", area);
		initOrgProp.put("volume", volume);			
		for (String rab : rabContent.keySet()){
			double rr = rabContent.get(rab);
			rabContent.put(rab, rr*area);
		}
//		CARGO DIRECTAMENTE LO QUE QUIERO QUE SE INCORPORE A LA CISTERNA GRANDE.  ESTO ES DIFERENTE
//		A UPTAKE.  EN CSV, HAY QUE CARGAR LA CANTIDAD 
//		for (String mem : membraneContent.keySet()){
//			double mm = membraneContent.get(mem);
//			membraneContent.put(mem, mm*area);
//		}
//
////		System.out.println(selectedRab + InitialOrganelles.getInstance().getInitMembraneContent().get(kind)+ " membraneFlux	iniciales " +membraneContent);
//		for (String sol : solubleContent.keySet()){
//			double ss = solubleContent.get(sol);
//			solubleContent.put(sol, ss*volume);
//		}
		Endosome bud = new Endosome(endosome.getSpace(), endosome.getGrid(), rabContent, membraneContent,
				solubleContent, initOrgProp);
		bud.area = area;// initOrgProp.get("area");
//		bud.volume = volume; //initOrgProp.get("volume");
//		SELECT THE LARGEST ENDOSOME WITH THE SELECTED KIND AND FUSE THE NEW BUD TO THIS ENDOSOME
		List<Endosome> allEndosomes = new ArrayList<Endosome>();
		Context<Object> context = ContextUtils.getContext(endosome);
		for (Object obj : context) {	
			if (obj instanceof Endosome) {
				allEndosomes.add((Endosome) obj);
			}
		}
		double maxArea = 0d;
		Endosome selectedEnd = null;
		for (Endosome end : allEndosomes) {
			String maxRab = Collections.max(end.rabContent.entrySet(), Map.Entry.comparingByValue()).getKey();
			if (end.rabContent.containsKey(selectedRab) &&
					//maxRab.equals(selectedRab) &&
					end.rabContent.get(selectedRab) > maxArea) {
				maxArea = end.rabContent.get(selectedRab);
				selectedEnd = end;			
			}
		}
//System.out.println("Endosome" + allEndosomes);
//System.out.println("selectedEndosome" + selectedEnd);
		selectedEnd.area = selectedEnd.area + bud.area;
//		selectedEnd.volume = selectedEnd.volume + bud.volume; This is incorrect.  The volume must be estimated from the area of the cistern
////	To find the radius of a cistern 20nm high from the area
////	area = 2*PI*radius^2 + 2*PI*radius*20
////	quadratic equation 0 = a x^2 + b x + c
////	solution x = (-b+/- sqr(b^2 - 4*a*c)/2*a
////	quadratic equation 0 = 2*PI* r^2 + 2*PI*20* r - area
	double aq = 2d*Math.PI;
	double bq = 40*Math.PI;
	double cq = -selectedEnd.area;
	double dq =  bq * bq - 4 * aq * cq;
	double root1 = (-bq + Math.sqrt(dq))/(2*aq);
	selectedEnd.volume = Math.PI*root1*root1*20; // volume or a flat cistern 20 nm high from the radius		
		selectedEnd.rabContent = sumRabContent(selectedEnd, bud);
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		if (tick >30000
				&& loadUptake) {
			selectedEnd.membraneContent = sumMembraneContent(selectedEnd, bud);
			selectedEnd.solubleContent = sumSolubleContent(selectedEnd, bud);
			if (selectedRab.equals("RabA")){
				loadUptake = false;
			}
			
		}

		
	}
	private static HashMap<String, Double> sumRabContent(Endosome endosome1,
			Endosome endosome2) {
		// HashMap<String, Double> map3 = new HashMap<String, Double>();
		// map3.putAll(endosome1.rabContent);
		// map3.forEach((k, v) -> endosome2.rabContent.merge(k, v, (v1, v2) ->
		// v1 + v2));
		// return map3;

		HashMap<String, Double> rabSum = new HashMap<String, Double>();
		for (String key1 : endosome1.rabContent.keySet()) {
			if (endosome2.rabContent.containsKey(key1)) {
				double sum = endosome1.rabContent.get(key1)
						+ endosome2.rabContent.get(key1);
				rabSum.put(key1, sum);
			} else
				rabSum.put(key1, endosome1.rabContent.get(key1));
		}
		for (String key2 : endosome2.rabContent.keySet()) {
			if (!endosome1.rabContent.containsKey(key2)) {
				rabSum.put(key2, endosome2.rabContent.get(key2));
			}
		}

		// System.out.println("rabContentSum" + endosome1.rabContent);
		return rabSum;
	}

	private static HashMap<String, Double> sumMembraneContent(Endosome endosome1,
			Endosome endosome2) {
		HashMap<String, Double> memSum = new HashMap<String, Double>();
		for (String key1 : endosome1.membraneContent.keySet()) {
			if (endosome2.membraneContent.containsKey(key1)) {
				double sum = endosome1.membraneContent.get(key1)
						+ endosome2.membraneContent.get(key1);
				if (sum > endosome1.area) sum = endosome1.area;
				memSum.put(key1, sum);
			} else
				memSum.put(key1, endosome1.membraneContent.get(key1));
		}
		for (String key2 : endosome2.membraneContent.keySet()) {
			if (!endosome1.membraneContent.containsKey(key2)) {
				double sum = endosome2.membraneContent.get(key2);
				if (sum > endosome1.area) sum = endosome1.area;
				memSum.put(key2, sum);
			}
		}
//		// endosome1.membraneContent = memSum;
//		
//		System.out.println("MemEnd 1" +endosome1.membraneContent +
//				"\n MemEnd 2"+ endosome2.membraneContent+ 
//				" \n MemSum" + memSum);
		return memSum;
	}

	private static HashMap<String, Double> sumSolubleContent(Endosome endosome1,
			Endosome endosome2) {
		HashMap<String, Double> solSum = new HashMap<String, Double>();
		for (String key1 : endosome1.solubleContent.keySet()) {
			if (endosome2.solubleContent.containsKey(key1)) {
				double sum = endosome1.solubleContent.get(key1)
						+ endosome2.solubleContent.get(key1);
				if (sum > endosome1.volume) sum = endosome1.volume;
				solSum.put(key1, sum);
			} else
				solSum.put(key1, endosome1.solubleContent.get(key1));
		}
		for (String key2 : endosome2.solubleContent.keySet()) {
			if (!endosome1.solubleContent.containsKey(key2)) {
				double sum = endosome2.solubleContent.get(key2);
				if (sum > endosome1.volume) sum = endosome1.volume;
				solSum.put(key2, sum);
			}
		}
		// endosome1.solubleContent = solSum;
		// System.out.println("solubleContentSum" + endosome1.solubleContent);
		return solSum;
	}

}
