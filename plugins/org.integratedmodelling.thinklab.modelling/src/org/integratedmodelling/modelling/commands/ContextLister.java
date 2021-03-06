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
package org.integratedmodelling.modelling.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.integratedmodelling.modelling.ModelMap;
import org.integratedmodelling.modelling.model.ModelFactory;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.interfaces.annotations.ListingProvider;
import org.integratedmodelling.thinklab.interfaces.commands.IListingProvider;
import org.integratedmodelling.utils.WildcardMatcher;

@ListingProvider(label="contexts",itemlabel="context")
public class ContextLister implements IListingProvider {

	private boolean _source;
	private String _match;
	
	@Override
	public Collection<?> getListing() throws ThinklabException {
		
		ArrayList<String> ret = new ArrayList<String>();
		for (Object o : ModelFactory.get().contextsById.keySet()) {
			if (_match != null && !(new WildcardMatcher().match(o.toString(), _match)))
				continue;
			ret.add(o.toString());
		}
		Collections.sort(ret);
		return ret;
	}

	@Override
	public Collection<?> getSpecificListing(String item) throws ThinklabException {

		ArrayList<String> ret = new ArrayList<String>();
		
		for (Object o : ModelFactory.get().contextsById.keySet()) {
			if (new WildcardMatcher().match(o.toString(),item))
				ret.add(getModelEntry(item, ret, new HashSet<String>()));
		}
		
		return ret;
	}
	
	private String getModelDescription(String model) throws ThinklabException {

		if (_source) {
			return ModelMap.getSource(model);
		}		
		return model;
	}
	
	private String getModelEntry(String model, ArrayList<String> res, HashSet<String> refs) throws ThinklabException {
		
		return getModelDescription(model);	
	}


	@Override
	public void notifyParameter(String parameter, String value) {

		if (parameter.equals("source") && value.equals("true")) 
			this._source = true;
		else if (parameter.equals("match"))
			this._match = value;
	}

}
