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
package org.integratedmodelling.modelling.corescience;

import org.integratedmodelling.corescience.CoreScience;
import org.integratedmodelling.corescience.interfaces.IContext;
import org.integratedmodelling.modelling.interfaces.IModel;
import org.integratedmodelling.modelling.model.DefaultAbstractModel;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.exception.ThinklabValidationException;
import org.integratedmodelling.thinklab.interfaces.applications.ISession;
import org.integratedmodelling.thinklab.interfaces.knowledge.IConcept;
import org.integratedmodelling.thinklab.interfaces.storage.IKBox;
import org.integratedmodelling.utils.Polylist;

/**
 * All that is required from an identification is to build a dependency structure. Its state
 * may be an instance (same as its observable).
 * 
 * @author Ferdinando Villa
 *
 */
public class ObservationModel extends DefaultAbstractModel {

	public ObservationModel(String namespace) {
		super(namespace);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setObservable(Object observableOrModel)
			throws ThinklabException {
		// TODO Auto-generated method stub
		super.setObservable(observableOrModel);
	}

	@Override
	public String toString() {
		return ("identification");
	}

	@Override
	public void validateMediatedModel(IModel model)
			throws ThinklabValidationException {

		super.validateMediatedModel(model);
		
		/*
		 * TODO a fairly sophisticated mediation may actually take place - a real semantic
		 * mediation of observables - but that's for another stage.
		 */
		throw new ThinklabValidationException("no mediation is allowed in identifications");
	}

	@Override
	public IConcept getCompatibleObservationType(ISession session) {
		return CoreScience.Observation();
	}

	@Override
	public IModel getConfigurableClone() {
		ObservationModel ret = new ObservationModel(namespace);
		ret.copy(this);
		return ret;
	}

	@Override
	public Polylist buildDefinition(IKBox kbox, ISession session, IContext context, int flags) throws ThinklabException {
		
		Polylist def = Polylist.listNotNull(
				CoreScience.OBSERVATION,
				Polylist.list(CoreScience.HAS_FORMAL_NAME, getLocalFormalName()),				
				Polylist.list(
					CoreScience.HAS_OBSERVABLE,
					Polylist.list(getObservableClass())));

		return addDefaultFields(def);
	}

	@Override
	public Polylist conceptualize() throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void validateSemantics(ISession session) throws ThinklabException {
		// TODO Auto-generated method stub
		
	}

}
