package immunity;

import java.util.HashMap;
import java.util.Set;

public class EndosomeRabConversionStep {
	
	public static void rabConversion(Endosome endosome) {

		RabConversion rabConversion = RabConversion.getInstance();

		Set<String> metabolites = RabConversion.getInstance().getMetabolites();
/*		Para pasar a COPASI concentraciones de proteínas de membrana voy a tomar el criterio de:
			Proteína (en unidades de área como están)*0.0018.  0.0018 convierte las unidades de área utilizadas para moléculas a número de moléculas.  Luego hay que dividir por un volumen para tener una concentración, que es lo que va a manejar COPASI.
			Considero que la membrana tiene 5nm de espesor con lo que debo dividir por área organela(en nm2 *5 nm.
			En número de moléculas dividido por área*5 me da el número de moléculas por nm3.
			Para pasar a mMol es: numero de moléculas por nm3 /numero Avogadro * cantidad de nm3 que hay en un litro *1000 (mol a mmol)
			En un litro hay 10^24 nm3
			La cuenta es entonces	  nro mol * 10^24 *1000
						       area*5*6*10^23
			o sea			Factor en unidades de área *0.0018*10000  
						            area*30

			Supongamos que tengo 5000 en un endosoma de 5000 nm2
			La cuenta da: 
						5000*0.0018*10000
						5000*30
			La cuenta simplificada es factor en unidades de área *0.6/area = mM*/

		for (String met : metabolites) {
			if (met.endsWith("m")) {
				String Rab = met.substring(0, 4);
				if (endosome.rabContent.containsKey(Rab)) {
					double metValue = Math.round
							(endosome.rabContent.get(Rab) * 1000) / 1000;
					metValue = metValue*0.6/endosome.area;//convert to mM
					rabConversion.setInitialConcentration(met, metValue);

				} else {
					rabConversion.setInitialConcentration(met, 0.0);
					// System.out.println("COPASI INITIAL " + met + 0.0);
				}
			}
/*			O sea las unidades de volumen *10/30000 para pasar a moléculas .
			La cuenta es entonces	  nro mol * 10^24 *1000
						       volume*6*10^23

			o sea			Factor en unidades de volume *10*10000  
						            volume*30000*6
			La cuenta simplificada sería: Factor en unidades de volumen * 0.55/volume

			Para un factor que tiene el valor 30.000 en un volumen de 30.000,
			La cuenta es 30.000*0.55/30.000 que me da 0.55 mM que es lo que paso a Copasi
			De regreso reconvierto a unidades de volumen
			valor de Copasi*volumen/0.55*/

			if (met.endsWith("c")) {
				String Rab = met.substring(0, 4);
				if (Cell.getInstance().getRabCell().containsKey(Rab)) {
					double metValue = Math.round(Cell.getInstance()
							.getRabCell().get(Rab) * 1000) / 1000;
					metValue = metValue*0.55/Cell.volume;//convert to mM
					rabConversion.setInitialConcentration(met, metValue);
					// System.out.println("COPASI INITIAL " + met
					// + Cell.getInstance().rabCell.get(Rab));
				} else {
					rabConversion.setInitialConcentration(met, 0.0);
					// System.out.println("COPASI INITIAL " + met + 0.0);

				}
			}
			if (met.equals("area"))
				rabConversion.setInitialConcentration(met, 0.6);
			if (met.equals("Rab0"))
				rabConversion.setInitialConcentration(met, (Rab0(endosome)*0.6/endosome.area));
		}
/*		PM SUPERFICIE Y CELL VOLUME
		Considerando que la superficie de la PM es de 750 nm X el alto.  Qué alto considero??
		Puede ser 200 nm.  Para 200 me queda 150.000 ( o sea la superficie de 30 endosomas)

		Para la Cell, el volumen sería de 750x750x200= 112.500.000.  Otro modo es considerar que los endosomas ocupan 1% del volumen y que el citosol ocupa un 50% del volumen celular.  Con ello, la suma de los volúmenes de los endosomas en este sistema es de aproximadamente 2 millones.  SI esto es el 1%, entonces corresponden a un volumen celular de 200 millones.  De estos el 50% es citosol, que deja una estimación de 100 millones. 
		Entonces voy a considerar el cell.volume = 100x10^6 nm3*/

		System.out.println("COPASI INITIAL  membrane " + endosome.rabContent
				+ " soluble " + Cell.getInstance().getRabCell());
		rabConversion.runTimeCourse();
		for (String met : metabolites) {
			if (met.endsWith("m")) {
				String Rab = met.substring(0, 4);
				double metValue = rabConversion.getConcentration(met);
				metValue=metValue*endosome.area/0.6;
				endosome.rabContent.put(Rab, metValue);
				// System.out.println("COPASI FINAL " + met +
				// rabContent.get(Rab));
			}
			if (met.endsWith("c")) {
				String Rab = met.substring(0, 4);
				double metValue = rabConversion.getConcentration(met);
				metValue = metValue*Cell.getInstance().volume/0.55;
				Cell.getInstance().getRabCell().put(Rab, metValue);

				// System.out.println("COPASI FINAL " + met
				// + Cell.getInstance().getRabCell().get(Rab));
			}

		}

		System.out.println("COPASI FINAL membrane " + endosome.rabContent + " soluble "
				+ Cell.getInstance().getRabCell());

	}

	private static double Rab0(Endosome endosome) {
		double sum = 0;
		for (String rab : endosome.rabContent.keySet()) {
			sum = sum + endosome.rabContent.get(rab);
		}
		double Rab0 = endosome.area - sum;
		return Rab0;
	}

	
}
