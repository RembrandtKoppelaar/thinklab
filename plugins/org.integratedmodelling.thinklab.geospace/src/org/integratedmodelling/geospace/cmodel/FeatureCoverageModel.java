/**
 * FeatureCoverageModel.java
 * ----------------------------------------------------------------------------------
 * 
 * Copyright (C) 2008 www.integratedmodelling.org
 * Created: Feb 18, 2008
 *
 * ----------------------------------------------------------------------------------
 * This file is part of ThinklabGeospacePlugin.
 * 
 * ThinklabGeospacePlugin is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ThinklabGeospacePlugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the software; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * ----------------------------------------------------------------------------------
 * 
 * @copyright 2008 www.integratedmodelling.org
 * @author    Ferdinando Villa (fvilla@uvm.edu)
 * @date      Feb 18, 2008
 * @license   http://www.gnu.org/licenses/gpl.txt GNU General Public License v3
 * @link      http://www.integratedmodelling.org
 **/
package org.integratedmodelling.geospace.cmodel;

import org.integratedmodelling.corescience.interfaces.IConceptualModel;
import org.integratedmodelling.corescience.interfaces.IExtent;
import org.integratedmodelling.corescience.interfaces.IExtentMediator;
import org.integratedmodelling.corescience.interfaces.IObservationContext;
import org.integratedmodelling.corescience.interfaces.IValueAggregator;
import org.integratedmodelling.corescience.interfaces.IValueMediator;
import org.integratedmodelling.geospace.extents.ArealExtent;
import org.integratedmodelling.geospace.extents.GridExtent;
import org.integratedmodelling.geospace.extents.ShapeExtent;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.exception.ThinklabValidationException;
import org.integratedmodelling.thinklab.interfaces.IValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Conceptual model for a coverage defined by non-overlapping features with associated values.
 * This is where things get slow at best, and messy at worst.
 * 
 * @author Ferdinando
 *
 */
public class FeatureCoverageModel extends SubdividedCoverageConceptualModel {

	public FeatureCoverageModel() {
		
	}
	
	public FeatureCoverageModel(
			double latLowerBound, double latUpperBound, 
			double lonLowerBound, double lonUpperBound,
			CoordinateReferenceSystem crs) {

		setBoundary(latUpperBound, latLowerBound, lonUpperBound, lonLowerBound);
		setCRS(crs);

	}

	public IExtent getExtent() throws ThinklabException {
		return new ShapeExtent(this.getBoundary(), this.getCRS(), this);
	}

	public IExtentMediator getExtentMediator(IExtent extent)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	public IValueAggregator getAggregator(IObservationContext ownContext,
			IObservationContext overallContext) {
		// TODO Auto-generated method stub
		return null;
	}

	public IValueMediator getMediator(IConceptualModel conceptualModel,
			IObservationContext ctx) throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	public IValue validateData(byte b) throws ThinklabValidationException {
		throw new ThinklabValidationException("cannot create a valid areal location from a number");
	}

	public IValue validateData(int b) throws ThinklabValidationException {
		throw new ThinklabValidationException("cannot create a valid areal location from a number");
	}

	public IValue validateData(long b) throws ThinklabValidationException {
		throw new ThinklabValidationException("cannot create a valid areal location from a number");
	}

	public IValue validateData(float b) throws ThinklabValidationException {
		throw new ThinklabValidationException("cannot create a valid areal location from a number");
	}

	public IValue validateData(double b) throws ThinklabValidationException {
		throw new ThinklabValidationException("cannot create a valid areal location from a number");
	}

	@Override
	protected IExtent createMergedExtent(ArealExtent orextent,
			ArealExtent otextent, CoordinateReferenceSystem crs2,
			Envelope common, boolean isConstraint)
			throws ThinklabException {
		
		ArealExtent ret = null;
		
		// if any is a GridExtent, or if we have two different feature collections, we need
		// to move this to a GridExtent.
		if (otextent instanceof GridExtent) {

			// raster wins
			GridExtent gext = new GridExtent(this, ((GridExtent)otextent));
			ret = gext;
			
		} else if (((ShapeExtent)orextent).hasDifferentFeatures((ShapeExtent)otextent)) {
			
			// we can't really handle this as a vector operation yet. 
			// Will determine a polygonal overlay at some point. For now we just turn to raster, but
			// we need a smart guess for the resolution.
			
		} else {
			
			// should check that they're exactly the same, or again rasterize
			
		}
		
		// TODO Auto-generated method stub
		return ret;
	}



}
