package immunity;

import org.COPASI.CCompartment;
import org.COPASI.CCopasiDataModel;
import org.COPASI.CCopasiMessage;
import org.COPASI.CCopasiMethod;
import org.COPASI.CCopasiObjectName;
import org.COPASI.CCopasiParameter;
import org.COPASI.CCopasiReportSeparator;
import org.COPASI.CCopasiRootContainer;
import org.COPASI.CCopasiStaticString;
import org.COPASI.CCopasiTask;
import org.COPASI.CMetab;
import org.COPASI.CModel;
import org.COPASI.CModelEntity;
import org.COPASI.CReaction;
import org.COPASI.CRegisteredObjectName;
import org.COPASI.CReportDefinition;
import org.COPASI.CReportDefinitionVector;
import org.COPASI.CTrajectoryMethod;
import org.COPASI.CTrajectoryProblem;
import org.COPASI.CTrajectoryTask;
import org.COPASI.ReportItemVector;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.util.collections.IndexedIterable;

import org.COPASI.*;

public class EndosomeAntigenPresentationStepTest {
	private Endosome endosome;
	@Before
	public void setUp() throws Exception {
		Schedule schedule = new Schedule ();
		RunEnvironment.init( schedule , null , null , true );
		Context context = new DefaultContext();
		
		CellBuilder cellBuilder = new CellBuilder();
		context = cellBuilder.build(context);
		RunState.init().setMasterContext(context);
		IndexedIterable objects = context.getObjects(Endosome.class);

		this.endosome = (Endosome) objects.get(0);

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInternalVesicle() {
		this.endosome.area = 20000;
		this.endosome.volume = 40000;
		this.endosome.rabContent.clear();
		this.endosome.solubleContent.put("ova", 5000d);
		this.endosome.solubleContent.put("preP", 5000d);
		this.endosome.solubleContent.put("pept", 5000d);
		this.endosome.membraneContent.put("mHCI", 5000d);

		for (int i = 0; i<5; i++){
			this.endosome.solubleContent.put("p1", i*500d);
			this.endosome.membraneContent.put("p2", i*500d);
			System.out.println("\nTEST   antes    \n "+this.endosome.solubleContent + this.endosome.membraneContent);
			EndosomeAntigenPresentationStep.antigenPresentation(this.endosome);
			System.out.println("\nTEST   despues    \n "+this.endosome.solubleContent + this.endosome.membraneContent);

		//assertSame(this.endosome.solubleContent, this.endosome.solubleContent);
		//		assertNotSame(initial, this.endosome.solubleContent);
		}
		}	
	
	}
//Fails
//MESSAGE Native code library failed to load. 
//java.lang.UnsatisfiedLinkError: no CopasiJava in java.library.path
