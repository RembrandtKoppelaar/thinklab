/**
 * Copyright 2011 The ARIES Consortium (http://www.ariesonline.org) and
 * www.integratedmodelling.org. 

   This file is part of Thinklab.

   Thinklab is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published
   by the Free Software Foundation, either version 3 of the License,
   or (at your option) any later version.

   Thinklab is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Thinklab.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.integratedmodelling.modelling.implementations.observations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.integratedmodelling.corescience.CoreScience;
import org.integratedmodelling.corescience.context.ObservationContext;
import org.integratedmodelling.corescience.implementations.datasources.ClassData;
import org.integratedmodelling.corescience.implementations.observations.Observation;
import org.integratedmodelling.corescience.interfaces.IObservation;
import org.integratedmodelling.corescience.interfaces.IObservationContext;
import org.integratedmodelling.corescience.interfaces.IProbabilisticObservation;
import org.integratedmodelling.corescience.interfaces.IState;
import org.integratedmodelling.corescience.interfaces.internal.IStateAccessor;
import org.integratedmodelling.corescience.interfaces.internal.IndirectObservation;
import org.integratedmodelling.corescience.interfaces.internal.MediatingObservation;
import org.integratedmodelling.corescience.interfaces.internal.Topology;
import org.integratedmodelling.corescience.literals.GeneralClassifier;
import org.integratedmodelling.corescience.metadata.Metadata;
import org.integratedmodelling.modelling.ModellingPlugin;
import org.integratedmodelling.thinklab.KnowledgeManager;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.interfaces.annotations.InstanceImplementation;
import org.integratedmodelling.thinklab.interfaces.knowledge.IConcept;
import org.integratedmodelling.thinklab.interfaces.knowledge.IInstance;
import org.integratedmodelling.thinklab.interfaces.knowledge.IRelationship;
import org.integratedmodelling.thinklab.interfaces.literals.IValue;
import org.integratedmodelling.utils.MiscUtilities;
import org.integratedmodelling.utils.Pair;
import org.integratedmodelling.utils.Polylist;

/**
 * Built by the classification model. Fairly nasty to use otherwise, but very flexible and 
 * beautifully defined in Clojure.
 * 
 * @author Ferdinando
 */
@InstanceImplementation(concept="modeltypes:ModeledClassification")
public class ModeledClassification 
	extends Observation 
	implements MediatingObservation, IProbabilisticObservation {
	
	// set through reflection - must be public
	public ArrayList<Pair<GeneralClassifier, IConcept>> classifiers = 
		new ArrayList<Pair<GeneralClassifier,IConcept>>();
	
	IConcept cSpace = null;
	double[] continuousDistribution = null;

	protected boolean hasNilClassifier = false;

	@Override
	public String toString() {
		return ("classification(" + getObservableClass() + "): " + cSpace);
	}

	/**
	 * TODO 
	 * FIXME
	 * this may not be necessary; this is intended as a mediator class only
	 * @author Ferdinando Villa
	 *
	 */
	public class ClassificationAccessor implements IStateAccessor {

		int index = 0;
		
		@Override
		public Object getValue(int idx, Object[] registers) {
			Object o = getDataSource().getValue(index++, registers);

			for (Pair<GeneralClassifier, IConcept> p : classifiers) {
				if (p.getFirst().classify(o))
					return p.getSecond();
			}
			
			ModellingPlugin.get().logger().warn(
					"value " + o + " does not classify as a valid " + getObservableClass() +
					": datasource will have null values");
			
			return null;
		}

		@Override
		public boolean isConstant() {
			return false;
		}

		@Override
		public boolean notifyDependencyObservable(IObservation o,
				IConcept observable, String formalName)
				throws ThinklabException {
			return !(o instanceof Topology);
		}

		@Override
		public void notifyDependencyRegister(IObservation observation,
				IConcept observable, int register, IConcept stateType)
				throws ThinklabException {
		}

		@Override
		public void notifyState(IState dds, IObservationContext overallContext,
				IObservationContext ownContext) throws ThinklabException  {
			// TODO Auto-generated method stub
			
		}
	}
	

	public class ClassificationMediator implements IStateAccessor {

		int index = 0;
		
		@Override
		public Object getValue(int idx, Object[] registers) {

			Object o = registers[index];
			
			if (o instanceof Number && Double.isNaN(((Number)o).doubleValue()))
				o = null;
			
			if (o == null && !hasNilClassifier)
				return null;

			for (Pair<GeneralClassifier, IConcept> p : classifiers) {
				if (p.getFirst().classify(o))
					return p.getSecond();
			}

			// null means "no data"; it can be caught using with a nil classifier						
			return null;
		}

		@Override
		public boolean isConstant() {
			return false;
		}

		@Override
		public boolean notifyDependencyObservable(IObservation o, IConcept observable, String formalName)
				throws ThinklabException {
			return true;
		}

		@Override
		public void notifyDependencyRegister(IObservation observation, IConcept observable,
				int register, IConcept stateType) throws ThinklabException {	
			index = register;
		}
		
		@Override
		public String toString() {
			return "[Classifier " + classifiers + " @ " + index + " ]";
		}

		@Override
		public void notifyState(IState dds, IObservationContext overallContext,
				IObservationContext ownContext)  throws ThinklabException {
			// TODO Auto-generated method stub
			
		}
	}

	
	@Override
	public IStateAccessor getAccessor(IObservationContext context) {
		return new ClassificationAccessor();
	}

	@Override
	public IConcept getStateType() {
		return cSpace;
	}

	@Override
	public void initialize(IInstance i) throws ThinklabException {

		metadata.put(Metadata.CONTINUOUS, Boolean.FALSE);
		
		super.initialize(i);

		/* 
		 * these should be in already through reflection, but let's keep
		 * the OWL way supported just in case.
		 */
		for (IRelationship r : i.getRelationships("modeltypes:hasClassifier")) {
			String[] rz = r.getValue().toString().split("->");
			Pair<GeneralClassifier, IConcept> cls = 
				new Pair<GeneralClassifier, IConcept>(
					new GeneralClassifier(rz[1]), 
					KnowledgeManager.get().requireConcept(rz[0]));
			classifiers.add(cls);					
		}
		
		/*
		 * we have no guarantee that the universal classifier, if there,
		 * will be last, given that it may come from an OWL multiproperty where
		 * the orderding isn't guaranteed.
		 * 
		 * scan the classifiers and if we have a universal classifier make sure
		 * it's the last one, to avoid problems.
		 */
		int unidx = -1; int iz = 0;
		for (Pair<GeneralClassifier, IConcept> cls : classifiers) {
			if (cls.getFirst().isUniversal()) {
				unidx = iz;
			}
			iz++;
		}
		
		if (unidx >= 0 && unidx < classifiers.size() -1) { 
			ArrayList<Pair<GeneralClassifier, IConcept>> nc =
				new ArrayList<Pair<GeneralClassifier,IConcept>>();
			for (iz = 0; iz < classifiers.size(); iz++) {
				if (iz != unidx)
					nc.add(classifiers.get(iz));
			}
			nc.add(classifiers.get(unidx));
			classifiers = nc;
		}
		
		/*
		 * check if we have a nil classifier; if we don't we don't bother classifying
		 * nulls and save some work.
		 */
		this.hasNilClassifier = false;
		for (Pair<GeneralClassifier, IConcept> cl : classifiers) {
			if (cl.getFirst().isNil()) {
				this.hasNilClassifier = true;
				break;
			}
		}
		
		IValue def = i.get(CoreScience.HAS_CONCEPTUAL_SPACE);
		if (def != null)
			cSpace = def.getConcept();

		def = i.get("modeltypes:encodesContinuousDistribution");
		if (def != null)
			continuousDistribution = MiscUtilities.parseDoubleVector(def.toString());

		// TODO remove?
		if (continuousDistribution != null && getDataSource() != null && (getDataSource() instanceof IState))
			((IState)getDataSource()).getMetadata().put(
					Metadata.CONTINUOS_DISTRIBUTION_BREAKPOINTS, 
					continuousDistribution); 

		if (continuousDistribution != null)
			metadata.put(Metadata.CONTINUOS_DISTRIBUTION_BREAKPOINTS, 
					continuousDistribution);

		if (classifiers != null) {

			metadata.put(Metadata.CLASSIFIERS, classifiers);
			
			IConcept[] rnk = null;
			/*
			 * remap the values to ranks and determine how to rewire the input
			 * if necessary, use classifiers instead of lexicographic order to
			 * infer the appropriate concept order
			 */
			ArrayList<GeneralClassifier> cla = new ArrayList<GeneralClassifier>();
			ArrayList<IConcept> con = new ArrayList<IConcept>();
			for (Pair<GeneralClassifier, IConcept> op : classifiers) {
				cla.add(op.getFirst());
				con.add(op.getSecond());
			}

			Pair<double[], IConcept[]> pd = 
				Metadata
					.computeDistributionBreakpoints(cSpace, cla, con);
			if (pd != null) {
				if (pd.getSecond()[0] != null) {
					rnk = pd.getSecond();
				}
			}

			HashMap<IConcept, Integer> ranks = null;
			if (rnk == null) {	
				ranks = Metadata.rankConcepts(cSpace, metadata);
			} else {
				ranks = Metadata.rankConcepts(cSpace, rnk, metadata);
			}

		}
		
	}

	@Override
	public Polylist conceptualize() throws ThinklabException {
		
		ArrayList<Object> arr = new ArrayList<Object>();
		
		arr.add("observation:Classification");
		arr.add(Polylist.list(CoreScience.HAS_CONCEPTUAL_SPACE, Polylist.list(cSpace)));
		arr.add(Polylist.list(CoreScience.HAS_OBSERVABLE, Polylist.list(cSpace)));
		
		return Polylist.PolylistFromArrayList(arr);
	}

	@Override
	public IState createState(int size, IObservationContext context) throws ThinklabException {
		
		IState ret = new ClassData(getObservableClass(), size, classifiers, (ObservationContext)context);
//
//		/*
//		 * TODO other metadata
//		 */
//		if (continuousDistribution != null)
//			ret.getMetadata().put(
//					Metadata.CONTINUOS_DISTRIBUTION_BREAKPOINTS, 
//					continuousDistribution); 
//		
		ret.getMetadata().merge(this.metadata);
		return ret;
	}

	@Override
	public IStateAccessor getMediator(IndirectObservation observation, IObservationContext context)
			throws ThinklabException {
		return new ClassificationMediator();
	}

	@Override
	public List<Pair<GeneralClassifier, IConcept>> getClassifiers() {
		return classifiers;
	}
}