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
package org.integratedmodelling.modelling.visualization;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.integratedmodelling.corescience.interfaces.IContext;
import org.integratedmodelling.corescience.interfaces.IExtent;
import org.integratedmodelling.geospace.extents.GridExtent;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.interfaces.knowledge.IConcept;

/**
 * Just like a FileVisualization, but takes an additional urlPrefix argument and allows
 * to retrieve images as URLs as well as files.
 * 
 * @author Ferdinando
 *
 */
public class WebVisualization extends FileVisualization {

	protected String urlPrefix;

	public WebVisualization(IContext context, String directory, String urlPrefix)
			throws ThinklabException {
		super(context, new File(directory));
		this.urlPrefix = urlPrefix;
	}
	
	public Collection<String> getStateUrls(IConcept c) {
		
		ArrayList<String> ret = new ArrayList<String>();
		for (String s : new String[] {	
				VisualizationFactory.PLOT_SURFACE_2D, VisualizationFactory.PLOT_CONTOUR_2D, 
				VisualizationFactory.PLOT_GEOSURFACE_2D, VisualizationFactory.PLOT_UNCERTAINTYSURFACE_2D, 
				VisualizationFactory.PLOT_GEOCONTOUR_2D, VisualizationFactory.PLOT_TIMESERIES_LINE, 
				VisualizationFactory.PLOT_TIMELAPSE_VIDEO}) {
			
			File f = new File(getStateDirectory(c) + File.separator + s);
			if (f.exists())
				ret.add(urlPrefix + "/" + archive.getStateRelativePath(c) + "/" + s);
		}
		return ret;
	}
	
	/**
	 * Return the state at the given click point in an image produced by this visualization. Assumes 
	 * the image is a spatial map and the states are raster cells; returns null if not.
	 * Transform image coordinates into state coordinates using thinklab/gis conventions.
	 * 
	 * @param imgX
	 * @param imgY
	 * @return
	 */
	public Object getDataAt(IConcept concept, int imgX, int imgY) {
		
		IExtent sp = context.getSpace();
		
		if (!(sp instanceof GridExtent))
			return null;
		
		Object ret = null;
		GridExtent grid = (GridExtent) sp;
		
		double pcx = (double)(getXPlotSize())/(double)(grid.getXCells());
		double pcy = (double)(getYPlotSize())/(double)(grid.getYCells());
		
		int dx = (int)((double)imgX/pcx);
		int dy = (int)((double)(getYPlotSize() - imgY)/pcy);
		
		int idx = grid.getIndex(dx, dy);
			
		ret = context.getState(concept).getValue(idx);
		
		return ret;
	}
	
	/**
	 * Return the URL of the image for the given concept and type, or null if not there.
	 * 
	 * @param c
	 * @param type
	 * @return
	 */
	public String getStateUrl(IConcept c, String type) {
			
		File f = new File(getStateDirectory(c) + File.separator + type);
		if (f.exists())
			return urlPrefix + "/" + archive.getStateRelativePath(c) + "/" + type;
		return null;
	}

	/**
	 * Return the URL of the path containing the concept files.
	 * 
	 * @param c
	 * @return
	 */
	public String getStateUrl(IConcept c) {
		
		File f = getStateDirectory(c);
		if (f.exists())
			return urlPrefix + "/" + archive.getStateRelativePath(c);
		return null;
	}

}
