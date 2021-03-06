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

import java.util.Collection;

import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.interfaces.annotations.ListingProvider;
import org.integratedmodelling.thinklab.interfaces.commands.IListingProvider;
import org.integratedmodelling.utils.image.ColorMap;

@ListingProvider(label="colormaps")
public class ColormapLister implements IListingProvider {

	@Override
	public Collection<?> getListing() throws ThinklabException {
		return ColorMap.paletteNames;
	}

	@Override
	public Collection<?> getSpecificListing(String item) throws ThinklabException {
		// TODO implement listing of single colormap
		return null;
	}

	@Override
	public void notifyParameter(String parameter, String value) {
		// TODO Auto-generated method stub
		
	}

}
