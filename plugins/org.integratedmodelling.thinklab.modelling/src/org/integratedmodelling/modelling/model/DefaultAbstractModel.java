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
package org.integratedmodelling.modelling.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.integratedmodelling.corescience.CoreScience;
import org.integratedmodelling.corescience.interfaces.IContext;
import org.integratedmodelling.corescience.interfaces.IExtent;
import org.integratedmodelling.corescience.interfaces.IObservation;
import org.integratedmodelling.corescience.interfaces.internal.Topology;
import org.integratedmodelling.corescience.literals.DistributionValue;
import org.integratedmodelling.corescience.metadata.Metadata;
import org.integratedmodelling.modelling.ObservationFactory;
import org.integratedmodelling.modelling.context.Context;
import org.integratedmodelling.modelling.exceptions.ThinklabModelException;
import org.integratedmodelling.modelling.interfaces.IModel;
import org.integratedmodelling.modelling.interfaces.IModelForm;
import org.integratedmodelling.modelling.interfaces.ITrainableModel;
import org.integratedmodelling.modelling.training.TrainingManager;
import org.integratedmodelling.thinklab.KnowledgeManager;
import org.integratedmodelling.thinklab.Thinklab;
import org.integratedmodelling.thinklab.constraint.Constraint;
import org.integratedmodelling.thinklab.constraint.DefaultConformance;
import org.integratedmodelling.thinklab.constraint.Restriction;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.exception.ThinklabValidationException;
import org.integratedmodelling.thinklab.interfaces.applications.ISession;
import org.integratedmodelling.thinklab.interfaces.knowledge.IConcept;
import org.integratedmodelling.thinklab.interfaces.knowledge.IInstance;
import org.integratedmodelling.thinklab.interfaces.knowledge.IOntology;
import org.integratedmodelling.thinklab.interfaces.knowledge.datastructures.IntelligentMap;
import org.integratedmodelling.thinklab.interfaces.query.IConformance;
import org.integratedmodelling.thinklab.interfaces.query.IQueryResult;
import org.integratedmodelling.thinklab.interfaces.storage.IKBox;
import org.integratedmodelling.thinklab.kbox.GroupingQueryResult;
import org.integratedmodelling.thinklab.owlapi.Session;
import org.integratedmodelling.utils.CamelCase;
import org.integratedmodelling.utils.MiscUtilities;
import org.integratedmodelling.utils.Polylist;

import clojure.lang.IFn;

public abstract class DefaultAbstractModel implements IModel {

	protected IModel mediated = null;
	protected ArrayList<IModel> dependents = new ArrayList<IModel>();
	protected ArrayList<IModel> observed = new ArrayList<IModel>();

	public DefaultAbstractModel(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public Set<IConcept> getObservables() {
		
		HashSet<IConcept> ret = new HashSet<IConcept>();
		this.collectObservables(ret);
		return ret;
	}

	private void collectObservables(HashSet<IConcept> coll) {

		if (mediated != null)
			((DefaultAbstractModel)mediated).collectObservables(coll);
		
		if (this instanceof Model) {
			for (IModel m : ((Model)this).models) {
				((DefaultAbstractModel)m).collectObservables(coll);
			}
		}
		
		for (IModel m : dependents) {
			((DefaultAbstractModel)m).collectObservables(coll);
		}
		
		coll.add(getObservableClass());
	}

	/*
	 * not null if this model has been generated by applying a scenario to
	 * another model.
	 */
	protected Scenario scenario = null;
	protected IConcept observable = null;

	// this is the defmodel <name>, complete with namespace (slash-separated)
	protected String name = null;

	protected Polylist observableSpecs = null;
	protected Object state = null;
	protected DistributionValue distribution = null;
	protected String id = null;
	protected String description = null;
	protected IFn whenClause = null;
	private LinkedList<Polylist> transformerQueue = new LinkedList<Polylist>();
	private boolean mediatesExternal = false;
	private boolean _validated = false;

	/*
	 * if the model was declared entifiable, this is the agent type that will
	 * incarnate the entities that can be produced from an observation of it.
	 */
	private String entityAgent = null;

	/*
	 * Any clause not intercepted by applyClause becomes metadata, which is
	 * communicated to the observation created.
	 */
	protected Metadata metadata = new Metadata();

	protected boolean isOptional = false;

	/*
	 * if scenarios can be applied to this model, the content of editable will
	 * be non-null and they will specify how we can be edited (e.g. a range of
	 * values for a state, or simply "true" for any edit).
	 */
	protected Boolean editable = null;
	protected String namespace;
	protected String localFormalName = null;
	private String trainingID;

	protected boolean isMediating() {
		return mediated != null || mediatesExternal;
	}

	public boolean isEditable() {
		return editable == null ? false : editable;
	}
	
	protected Polylist addDefaultFields(Polylist obs) {

		Metadata md = new Metadata(metadata);
		md.put(Metadata.DEFINING_MODEL, this);
		obs = ObservationFactory.addReflectedField(obs, "additionalMetadata", md);
		obs = ObservationFactory.addReflectedField(obs, "modelObservable", this.observableSpecs);
		
		return obs;
	}

	public void setMetadata(String kw, Object value) {
		metadata.put(kw.startsWith(":") ? kw.substring(1) : kw, value);
	}

	public void setName(String name) {
		String[] x = name.split("/");
		this.name = name;
		this.namespace = x[0];
		this.id = x[1];
	}

	@Override
	public Collection<IModel> getDependencies() {
		return dependents;
	}

	@Override
	public String getNamespace() {
		return this.namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}
	
	public boolean isStateful() {
		return false;
	}

	/**
	 * This one is invoked once before any use is made of the model, and is
	 * supposed to validate all concepts used in the model's definition. In
	 * order to allow facilitated and automated annotation, no model should
	 * perform concept validation at declaration; all validation should be done
	 * within this function.
	 * 
	 * Validation of concepts should be done using annotateConcept() so that
	 * annotation mode will be enabled.
	 * 
	 * @param session
	 *            TODO
	 * 
	 * @throws ThinklabException
	 */
	protected abstract void validateSemantics(ISession session)
			throws ThinklabException;

	public void setObservable(Object observableOrModel)
			throws ThinklabException {

		if (observableOrModel instanceof IModel) {
			this.mediated = (IModel) observableOrModel;
			this.observable = ((DefaultAbstractModel) observableOrModel).observable;
			this.observableSpecs = ((DefaultAbstractModel) observableOrModel).observableSpecs;
		} else if (observableOrModel instanceof IConcept) {
			this.observable = (IConcept) observableOrModel;
			this.observableSpecs = Polylist.list(this.observable);
		} else {
			this.observable = ModelFactory.annotateConcept(namespace, observableOrModel.toString());
			this.observableSpecs = Polylist.list(this.observable);
		}

		this.id = observable.toString().toLowerCase();
	}

	@Override
	public void applyClause(String keyword, Object argument)
			throws ThinklabException {

		// System.out.println(this + "processing clause " + keyword + " -> " +
		// argument);

		if (keyword.equals(":context")) {
			Collection<?> c = (Collection<?>) argument;
			for (Object o : c) {
				addDependentModel((IModel) o);
			}
		} else if (keyword.equals(":observed")) {
			Collection<?> c = (Collection<?>) argument;
			for (Object o : c) {
				addObservedModel((IModel) o);
			}
		} else if (keyword.equals(":as")) {
			setLocalFormalName(argument.toString());
		} else if (keyword.equals(":when")) {
			whenClause = (IFn) argument;
		} else if (keyword.equals(":editable")) {
			editable = (Boolean)argument;
		} else if (keyword.equals(":optional")) {
			isOptional = (Boolean) argument;
		} else if (keyword.equals(":required")) {
			isOptional = !((Boolean) argument);
		} else if (keyword.equals(":agent")) {
			entityAgent = argument.toString();
		} else {
			metadata.put(keyword.substring(1), argument);
		}
	}

	public boolean isEntifiable() {
		return entityAgent != null;
	}

	public String getEntityType() {
		return entityAgent;
	}

	/**
	 * This is called for each model defined for us in a :context clause, after
	 * the dependent has been completely specified.
	 * 
	 * All dependencies should be Models; if not, they're automatically wrapped into a
	 * Model for consistency (so that the scenarios can work properly).
	 * 
	 * @param model
	 */
	public void addDependentModel(IModel model) {
		// null-tolerant so we can deal with the silly "functional comments" in
		// clojure
		if (model != null)
			dependents.add(wrapModel(model));
	}

	private IModel wrapModel(IModel model) {

		if (model instanceof Model)
			return model;

		// wrap into a Model
		return new Model((DefaultAbstractModel)model);
	}

	public void addObservedModel(IModel model) {
		// null-tolerant so we can deal with the silly "functional comments" in
		// clojure
		if (model != null)
			observed.add(model);
	}

	/**
	 * If the resulting observation is to be transformed by a transformer obs,
	 * add a transformer definition from defmodel (e.g. :cluster (def)) in the
	 * transformer queue.
	 * 
	 * @param definition
	 */
	public void enqueueTransformer(Polylist definition) {
		transformerQueue.addLast(definition);
	}

	/**
	 * This handles the :as clause. If we don't have one, our id is the
	 * de-camelized name of our observable class.
	 * 
	 * @param id
	 */
	public void setLocalFormalName(String id) {
		this.localFormalName = id;
	}

	protected void validateMediatedModel(IModel model)
			throws ThinklabValidationException {
		if (getObservableClass().equals(model.getObservableClass())) {
			throw new ThinklabValidationException(
					"a model cannot mediate another that observes the same concept: "
							+ model.getObservableClass());
		}
	}

	@Override
	public IConcept getObservableClass() {
		return observable;
	}

	@Override
	public boolean isResolved() {
		return state != null || mediated != null || distribution != null;
	}

	/*
	 * Copy the relevant fields when a clone is created before configuration
	 */
	protected void copy(DefaultAbstractModel model) {

		id = model.id;
		namespace = model.namespace;
		localFormalName = model.localFormalName;
		mediated = model.mediated;
		observable = model.observable;
		observableSpecs = model.observableSpecs;
		editable = model.editable;
		metadata = model.metadata;
		name = model.name;
		isOptional = model.isOptional;
		whenClause = model.whenClause;
		state = model.state;
		mediatesExternal = model.mediatesExternal;
		distribution = model.distribution;
	}

	/**
	 * Generate a query that will select the requested observation type and
	 * restrict the observable to the specifications we got for this model. Use
	 * passed conformance table to define the observable constraint. Optionally
	 * add in an extent restriction.
	 * 
	 * @param extentRestriction
	 * @param conformancePolicies
	 * @param session
	 * @param context
	 *            .getTopologies()
	 * @return
	 * @throws ThinklabException
	 */
	public Constraint generateObservableQuery(
			IntelligentMap<IConformance> conformancePolicies, ISession session,
			IContext context) throws ThinklabException {

		Constraint c = new Constraint(
				this.getCompatibleObservationType(session));

		IInstance inst = session.createObject(observableSpecs);
		IConformance conf = conformancePolicies == null ? new DefaultConformance()
				: conformancePolicies.get(inst.getDirectType());

		c = c.restrict(new Restriction(CoreScience.HAS_OBSERVABLE, conf
				.getConstraint(inst)));

		if (context.getExtents().size() > 0) {

			ArrayList<Restriction> er = new ArrayList<Restriction>();
			for (IExtent o : context.getExtents()) {
				Restriction r = o.getConstraint("intersects");
				if (r != null)
					er.add(r);
			}

			if (er.size() > 0) {
				c = c.restrict(er.size() == 1 ? er.get(0) : Restriction.AND(er
						.toArray(new Restriction[er.size()])));
			}
		}

		/*
		 * if we need this, we are mediators even if there was no mediated model
		 * in the specs
		 */
		mediatesExternal = true;
		
		return c;
	}

	@Override
	public IQueryResult observe(IKBox kbox, ISession session, Object... params)
			throws ThinklabException {

		validateConcepts(session);

		IntelligentMap<IConformance> conformances = null;
		ArrayList<Topology> extents = new ArrayList<Topology>();
		IContext context = null;

		if (params != null)
			for (Object o : params) {
				if (o instanceof IntelligentMap<?>) {
					conformances = (IntelligentMap<IConformance>) o;
				} else if (o instanceof IInstance) {
					// put away all the extents we passed
					IObservation obs = ObservationFactory
							.getObservation((IInstance) o);
					if (obs instanceof Topology) {
						extents.add((Topology) obs);
					}
				} else if (o instanceof Topology) {
					extents.add((Topology) o);
				} else if (o instanceof IContext) {
					context = (IContext) o;
					;
				}
			}

		if (context == null)
			context = Context.getContext(extents);

		return observeInternal(kbox, session, conformances, context, false);

	}

	/**
	 * This will be called once before any observation is made, or it can be
	 * called from the outside API to ensure that all concepts are valid.
	 * 
	 * @param session
	 * @throws ThinklabException
	 */
	public void validateConcepts(ISession session) throws ThinklabException {

		if (!_validated) {
			
			validateSemantics(session);

			/*
			 * validate mediated
			 */
			if (mediated != null) {

				((DefaultAbstractModel) mediated).validateConcepts(session);

				/*
				 * TODO FIXME shouldn't we call validateMediated() at this
				 * point? Nothing seems to be calling it anymore.
				 */
				metadata.merge(((DefaultAbstractModel) mediated).metadata);
			}

			/*
			 * validate dependents and observed
			 */
			for (IModel m : dependents)
				((DefaultAbstractModel) m).validateConcepts(session);

			for (IModel m : observed)
				((DefaultAbstractModel) m).validateConcepts(session);

			_validated = true;
		}
	}
	

	/**
	 * Return the model for the passed observable if it is in any dependency. Must be 
	 * a model capable of being used for evidence, i.e. get to data directly or through
	 * mediation only. If not found, return null.
	 * 
	 * @param obs
	 * @return
	 */
	public IModel findEvidenceDependencyFor(IConcept obs) {

		if (getObservableClass().equals(obs) && TrainingManager.get().isEvidenceModel(this))
			return this;
		
		if (this instanceof Model) {
			for (IModel md : ((Model)this).models) {
				IModel m = ((DefaultAbstractModel)md).findEvidenceDependencyFor(obs);
				if (m != null)
					return m;
			}
		}
		
		for (IModel md : this.dependents) {
			IModel m = ((DefaultAbstractModel)md).findEvidenceDependencyFor(obs);
			if (m != null)
				return m;
		}

		return null;
	}


	/*
	 * this should be protected, but...
	 */
	public ModelResult observeInternal(IKBox kbox, ISession session,
			IntelligentMap<IConformance> cp, IContext context,
			boolean acceptEmpty) throws ThinklabException {

		ModelResult ret = new ModelResult(this, kbox, session, context);

		/*
		 * if we're resolved, the model result contains all we need to know
		 */
		if (state != null || distribution != null)
			return ret;

		/*
		 * if mediated, realize mediated and add it
		 */
		if (mediated != null) {

			ModelResult res = 
				((DefaultAbstractModel) mediated)
					.observeInternal(kbox, session, cp, context, acceptEmpty);

			if (res == null || res.getTotalResultCount() == 0) {

				if (acceptEmpty)
					return null;

				throw new ThinklabModelException("model: cannot observe "
						+ ((DefaultAbstractModel) mediated).observable
						+ " in kbox " + kbox);
			}

			ret.addMediatedResult(res);
		}

		/*
		 * query dependencies
		 */
		for (IModel dep : dependents) {

			ModelResult d = ((DefaultAbstractModel) dep).observeInternal(kbox,
					session, cp, context,
					/*((DefaultAbstractModel) dep).isOptional*/false);

			// can only return null if optional is true for the dependent
			if (d != null)
				ret.addDependentResult(d);
		}

		/*
		 * if no state, dependents and no mediated, we need to find an
		 * observable to mediate
		 */
		if (mediated == null && dependents.size() == 0) {

			if (Thinklab.debug(session)) {
				session.print("---  " + getName()
						+ ": looking up observations for: " + observable);
			}

			if (kbox == null) {
				if (acceptEmpty)
					return null;
				else
					throw new ThinklabModelException("model: cannot observe "
							+ observable + ": no kbox given");
			}

			Constraint query = generateObservableQuery(cp, session, context);

			if (Thinklab.debug(session)) {
				session.print("---  query: " + query);
			}

			IQueryResult rs = new GroupingQueryResult(kbox.query(query,
					new String[] { "dataset" }, 0, -1), "dataset");

			if (Thinklab.debug(session)) {
				session.print("---  result: " + rs);
			}

			if (rs == null || rs.getTotalResultCount() == 0)
				if (acceptEmpty)
					return null;
				else
					throw new ThinklabModelException("model: cannot observe "
							+ observable + " in kbox " + kbox);

			ret.addMediatedResult(rs);
		}

		ret.initialize();

		return ret;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return namespace + "/" + id;
	}

	/**
	 * 
	 * @param scenario
	 * @return
	 * @throws ThinklabException
	 */
	@Override
	public IModel applyScenario(Scenario scenario) throws ThinklabException {
		return fixDependencies(applyScenarioInternal(scenario, new Session()));
	}

	/**
	 * A temporary hack that turns those models that depend on their same concepts into others that
	 * depend on derived ones. These models should just be illegal and the same thing implemented as
	 * a transformation with dependencies, but we don't have that yet.
	 * 
	 * Changes the passed model - should be called on a clone. Only used in scenarios as those
	 * transformations are not good programming otherwise.
	 * 
	 * @param applyScenarioInternal
	 * @return
	 * @throws ThinklabException 
	 */
	private IModel fixDependencies(IModel model) throws ThinklabException {
		HashSet<IConcept> deps = new HashSet<IConcept>();
		fixDependenciesInternal(model, deps);
		return model;
	}

	private void fixDependenciesInternal(IModel model, HashSet<IConcept> deps) throws ThinklabException {

		if (model instanceof Model && deps.contains(model.getObservableClass())) {
			
			IOntology o = model.getObservableClass().getOntology();

			String label = model.getObservableClass().toString();
			
			// remove concept adjustments from duplicated concepts in scenarios
			if (label.contains("___")) {
				int lc = label.indexOf("___");
				label = label.substring(0, lc);
			}
			
			IConcept c = null;
			for (int i = 1; ; i++) {
				c = KnowledgeManager.get().retrieveConcept(label + "___" + i);
				boolean ok = c == null;
				if (!ok)
					ok = !deps.contains(c);
				
				if (ok) {
					if (c == null) {
						c = o.createConcept(model.getObservableClass().getLocalName() + "___" + i, 
								new IConcept[]{model.getObservableClass()}, false);
					}
					break;
				}
			}
			
			((DefaultAbstractModel)model).setObservable(c);
		}

		deps.add(model.getObservableClass());
		
		if (model instanceof Model) {
			for (IModel m : ((Model)model).getObservers()) {
				fixDependenciesInternal(m, deps);
			}

			/*
			 * TODO see if we need to do the same with context models - hopefully not.
			 */
		} else {
			for (IModel m : model.getDependencies()) {
				fixDependenciesInternal(m, deps);
			}
		}
	}

	public IModel createTrainedClone(String trainingId, ISession session)
			throws ThinklabException {

		/*
		 * clone me as a return value
		 */
		DefaultAbstractModel ret = null;
		try {
			ret = (DefaultAbstractModel) this.clone();
		} catch (CloneNotSupportedException e) {
			// yeah, right
		}
		
		/*
		 * if I am trainable, apply the training, then proceed adding dependencies
		 */
		if (this instanceof ITrainableModel) {
			if (((ITrainableModel)ret).applyTraining(trainingId))
				session.print("training " + trainingId + " applied to " + this.getName());
		}

		/*
		 * Add models to the clone:
		 * if we haven't found a matching contingency, just keep the original ones.
		 */
		if (this instanceof Model) {
			for (IModel orm : ((Model)this).models) {
				((Model)ret).defModel(((DefaultAbstractModel)orm).createTrainedClone(trainingId, session), null);	
			}
		}
		
		/*
		 * ...and add our mediated model if any	
		 */
		DefaultAbstractModel elMediated = (DefaultAbstractModel) this.mediated;
		if (this.mediated != null)
			((DefaultAbstractModel)ret).mediated = elMediated.createTrainedClone(trainingId, session);
					
		/*
		 * Add as dependents the scenario-transformed versions of the original model.
		 */
		for (IModel d : dependents) {
			((DefaultAbstractModel)ret).addDependentModel(((DefaultAbstractModel)d).createTrainedClone(trainingId, session));
		}

		/*
		 * give the clone our ID so it will work in references; remember the training ID
		 */
		if (ret != null) {
			ret.id = id;
			ret.namespace = namespace;
			ret.scenario = scenario;
			ret.trainingID = trainingId;
			ret.localFormalName = localFormalName;
		}

		return ret;
	}
	
	
	protected IModel applyScenarioInternal(Scenario scenario, Session session)
			throws ThinklabException {

		/*
		 * if I am in the scenario, clone the scenario's, not me, unless I am a
		 * Model which requires to be preserved for functionality
		 */
		if (!(this instanceof Model)) {
			throw new ThinklabValidationException("attempting to transform non-model " + getName() + " with a scenario");
		}

		/*
		 * if there is anything resembling self in the scenario, just return that.
		 */
		IInstance match = session.createObject(observableSpecs);
		for (IModel scm : scenario.models) {
			
			IInstance im = session.createObject(((DefaultAbstractModel)scm).observableSpecs);			
			if (im.isConformant(match, null)) {
				return scm;
			}
		}
		
		/*
		 * clone me as a return value
		 */
		Model ret = null;
		try {
			ret = (Model) this.clone();
		} catch (CloneNotSupportedException e) {
			// yeah, right
		}
		
		
		/*
		 * Scan contingencies. If any contingency matches a model in the scenario,
		 * add the contingencies of that scenario model as our own and return. 
		 */
		for (IModel orm : ((Model)this).models) {
			for (IModel scm : scenario.models) {
			
				IInstance om = session.createObject(((DefaultAbstractModel)orm).observableSpecs);				
				IInstance im = session.createObject(((DefaultAbstractModel)scm).observableSpecs);
			
				if (im.isConformant(om, null)) {
					for (IModel msc : ((Model)scm).models)
						ret.defModel(msc, null);	
					return ret;
				}
			}
		}

		/*
		 * Add models to the clone:
		 * if we haven't found a matching contingency, just keep the original ones.
		 */
		for (IModel orm : ((Model)this).models) {
			ret.defModel(transformDependencies(orm, scenario, session), null);
		}
		
		/*
		 * ...and add our mediated model if any	
		 */
		DefaultAbstractModel elMediated = (DefaultAbstractModel) this.mediated;
		if (this.mediated != null)
			ret.mediated = 
				elMediated instanceof Model ?
					elMediated.applyScenarioInternal(scenario, session) :
					transformDependencies(elMediated, scenario, session);
					
		/*
		 * Add as dependents the scenario-transformed versions of the original model.
		 */
		for (IModel d : dependents) {
			ret.addDependentModel(
				((Model) d).applyScenarioInternal(scenario, session));
		}

		/*
		 * give the clone our ID so it will work in references, and store the
		 * scenario to constrain queries as needed.
		 */
		if (ret != null) {
			ret.id = id;
			ret.namespace = namespace;
			ret.scenario = scenario;
			ret.localFormalName = localFormalName;
		}

		return ret;
	}
	
	/*
	 * this one applies a scenario to a model used as mediated or contingency, which is not a
	 * Model. It should leave everything as is but use the scenario to transform the dependencies.
	 */
	private DefaultAbstractModel transformDependencies(IModel model, Scenario scenario, Session session) throws ThinklabException {

		if (model == null)
			return null;
		
		/*
		 * clone me as a return value
		 */
		DefaultAbstractModel ret = null;
		try {
			ret = (DefaultAbstractModel) ((DefaultAbstractModel) model).clone();
		} catch (CloneNotSupportedException e) {
			// yeah, right
		}
		
		
		DefaultAbstractModel elMediated = (DefaultAbstractModel) ((DefaultAbstractModel)model).mediated;
		if (elMediated != null)
			ret.mediated = 
				elMediated instanceof Model ?
					elMediated.applyScenarioInternal(scenario, session) :
					transformDependencies(elMediated, scenario, session);
		
		/*
		 * Add as dependents the scenario-transformed versions of the original model.
		 */
		for (IModel d : ((DefaultAbstractModel)model).dependents) {
			ret.addDependentModel(
				((DefaultAbstractModel)d).applyScenarioInternal(scenario, session));
		}
		return ret;
	}

	/**
	 * This one gets called when a scenario contains a model that has a
	 * conformant observable as us. In that case, the model is passed and we are
	 * expected to return a clone or a wrapper so that it expresses our same
	 * semantics. E.g., any passed measurement must be wrapped so that its units
	 * are the same as ours. If the model is not compatible, return null.
	 * 
	 * @param m
	 * @return
	 */
	protected IModel validateSubstitutionModel(IModel m) {
		return null;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return getConfigurableClone();
	}

	public void dump(PrintStream out) {
		dumpInternal(out, 0, "", null);
	}

	public void dump(PrintStream out, int level) {
		dumpInternal(out, level, "", null);
	}

	protected void dumpInternal(PrintStream out, int i, String role, IConcept obs) {

		boolean singlemod = 
			this instanceof Model && ((Model)this).models != null && ((Model) this).models.size() ==1;
		
		if (singlemod) {
			((DefaultAbstractModel)(((Model) this).models.get(0))).
				dumpInternal(out, i, role, this.getObservableClass());
		
		} else {
			
			if (obs == null)
				obs = this.getObservableClass();
			
			out.println(MiscUtilities.spaces(i) + role + this + "(" + obs + ")");
		}
		
		if (mediated != null) {
			((DefaultAbstractModel) mediated).dumpInternal(out, i + 3, "M ", null);
		}

		if (dependents.size() > 0) {
			for (IModel m : dependents)
				((DefaultAbstractModel) m).dumpInternal(out, i + 3, "D ", null);
		}

		if (this instanceof Model && ((Model)this).models != null && ((Model) this).models.size() > 1) {
			for (IModel m : ((Model) this).models)
				((DefaultAbstractModel) m).dumpInternal(out, i + 3, "* ", null);
		}

	}

	public void setMediatedModel(IModel m) {
		this.mediated = m;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof IModel ? getName().equals(
				((IModelForm) obj).getName()) : false;
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	public String getLocalFormalName() {
		if (localFormalName == null)
			return CamelCase.toLowerCase(getObservableClass().getLocalName(),
					'-');
		return localFormalName;
	}

	public IModel getMediated() {
		return mediated;
	}
	
	public Map<? extends String, ? extends Object> getMetadata() {
		return metadata;
	}

}
