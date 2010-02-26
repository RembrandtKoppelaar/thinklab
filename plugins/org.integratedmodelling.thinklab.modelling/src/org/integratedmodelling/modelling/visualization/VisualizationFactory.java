package org.integratedmodelling.modelling.visualization;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.integratedmodelling.corescience.implementations.datasources.ClassData;
import org.integratedmodelling.corescience.interfaces.IState;
import org.integratedmodelling.corescience.metadata.Metadata;
import org.integratedmodelling.geospace.implementations.observations.RasterGrid;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.exception.ThinklabIOException;
import org.integratedmodelling.thinklab.exception.ThinklabResourceNotFoundException;
import org.integratedmodelling.thinklab.exception.ThinklabValidationException;
import org.integratedmodelling.thinklab.interfaces.knowledge.IConcept;
import org.integratedmodelling.utils.CamelCase;
import org.integratedmodelling.utils.Pair;
import org.integratedmodelling.utils.image.ColorMap;
import org.integratedmodelling.utils.image.ColormapChooser;
import org.integratedmodelling.utils.image.ImageUtil;

/**
 * Will become a central access point for all visualization operations. For now has just what I need
 * at the moment.
 * 
 * @author Ferdinando
 *
 */
public class VisualizationFactory {

	static public final String COLORMAP_PROPERTY_PREFIX = "thinklab.colormap";
	
	static VisualizationFactory _this = new VisualizationFactory();
	ColormapChooser colormapChooser = new ColormapChooser(COLORMAP_PROPERTY_PREFIX);
	
	public static VisualizationFactory get() {
		return _this;
	}
	
	public void loadColormapDefinitions(Properties properties) throws ThinklabException {
		colormapChooser.load(properties);
	}
	

	public ColorMap getColormap(IConcept c, int levels) throws ThinklabException {
		return colormapChooser.get(c, levels);
	}
	
	public ColorMap getColormap(IConcept c, int levels, ColorMap def) throws ThinklabException {
		ColorMap ret = getColormap(c, levels);
		if (ret == null)
			ret = def;
		return ret;
	}
	
	/**
	 * Called when we don't have a set colormap for this observable.
	 * 
	 * @param state
	 * @param nlevels
	 * @return
	 * @throws ThinklabException 
	 */
	public ColorMap getDefaultColormap(IConcept observable, IState state, int nlevels) throws ThinklabException {
		
		String[] categories = (String[])state.getMetadata(Metadata.CATEGORIES);
		Boolean isBoolean   = (Boolean)state.getMetadata(Metadata.BOOLEAN);
		
		ColorMap ret = null;
		
		if ((isBoolean != null && isBoolean) || nlevels < 3) {
			ret = ColorMap.getColormap("Blues_z()", nlevels);	
		} else {
			ret = categories == null ? 
				(nlevels < 10 ? 
					ColorMap.getColormap("YlOrRd()", nlevels) : 
					ColorMap.jet(nlevels)) : // default for ordinal data
				ColorMap.random(nlevels); // default for categorical data
		}
		
		if (ret == null) {
			throw new ThinklabResourceNotFoundException(
					"cannot determine color table for " + 
					observable + 
					"; please add colormap entry");
		}
					
		return ret;
	}
	
	public String makeSurfacePlot(IConcept observable, IState state, 
			String fileOrNull,
			int x, int y, 
			RasterGrid space) throws ThinklabException {

		if (fileOrNull == null) {
			try {
				fileOrNull = File.createTempFile("img", ".png").toString();
			} catch (IOException e) {
				throw new ThinklabIOException(e);
			}
		}
		
		int[] idata = Metadata.getImageData(state);
		
		int nlevels = (Integer)state.getMetadata(Metadata.IMAGE_LEVELS);
		int[] iarange = (int[])state.getMetadata(Metadata.ACTUAL_IMAGE_RANGE);
		double[] dtrange = (double[])state.getMetadata(Metadata.THEORETICAL_IMAGE_RANGE);
		double[] darange = (double[])state.getMetadata(Metadata.ACTUAL_DATA_RANGE);

		ColorMap cmap = getColormap(observable, nlevels, getDefaultColormap(observable, state, nlevels));

		ImageUtil.createImageFile(ImageUtil.upsideDown(idata, space.getColumns()), 
				space.getColumns(), x, y, cmap, fileOrNull);

		state.setMetadata(Metadata.COLORMAP, cmap);
		
		return fileOrNull;
	}
	
	public String makeUncertaintyMask(IConcept observable,  IState state,  String fileOrNull,
			int x, int y, RasterGrid space) throws ThinklabException {
		
		double[] data = (double[]) state.getMetadata(Metadata.UNCERTAINTY);
		double[] odat = state.getDataAsDoubles();
		
		if (data == null)
			return null;

		if (fileOrNull == null) {
			try {
				fileOrNull = File.createTempFile("img", ".png").toString();
			} catch (IOException e) {
				throw new ThinklabIOException(e);
			}
		}
		
		int len = data.length;
		int[] idata = new int[len];
		
		int imin = 0, imax = 0;
		for (int i = 0; i < len; i++) {
			
			if (Double.isNaN(data[i]) || Double.isNaN(odat[i]))
				idata[i] = 0;
			else {
				idata[i] = (int)((1 - data[i])*255.0);
			}
			
			if (i == 0) {
				imin = idata[0];
				imax = idata[0];
			} else {
				if (idata[i] > imax) imax = idata[i];
				if (idata[i] < imin) imin = idata[i];
			}
		}
		
		if ((imax - imin) <= 0)
			// nothing to show
			return null;

		ImageUtil.createImageFile(ImageUtil.upsideDown(idata, space.getColumns()), 
				space.getColumns(), x, y, ColorMap.alphamask(256), fileOrNull);

		return fileOrNull;
	}

	/**
	 * Return a pair with an array of image files and one of descriptions; each file
	 * is a rectangle with the uniform color of a state, and each description describes
	 * the data with that color.
	 * 
	 * Must be called after calling one of the display functions that embed a colormap
	 * in the metadata. If not, an exception is thrown.
	 * 
	 * TODO should return null for continuous, undiscretized data models.
	 * 
	 * @param state
	 * @return
	 * @throws ThinklabException 
	 */
	public Pair<File[], String[]> getLegend(IState state, int totalLength, int height, String fileBaseName) throws ThinklabException {
		
		ColorMap cmap = (ColorMap) state.getMetadata(Metadata.COLORMAP);
		
		if (cmap == null) {
			throw new ThinklabValidationException("internal: getLegend called on a state without colormap");
		}

		double[] darange = (double[])state.getMetadata(Metadata.ACTUAL_DATA_RANGE);
		HashMap<IConcept, Integer> ranking = 
			(HashMap<IConcept, Integer>) state.getMetadata(Metadata.RANKING);

		int nlevels = cmap.getVisibleColorCount();
		int w = totalLength/nlevels;
		
		File[] imgs = cmap.getColorLegend(height, w, fileBaseName);
		String[] descs = new String[imgs.length];
		
		int n = 0;
		for (int i = (cmap.hasTransparentZero() ? 1 : 0); i < cmap.getColorCount(); i++) {
			String desc = "";
			if (ranking != null){
				// TODO rankings with data ranges if any
			} else if (state instanceof ClassData) {
				IConcept c = ((ClassData)state).getCategory(i);
				if (c != null) {
					desc = c.getLabel();
					if (desc == null || desc.equals(""))
						desc = CamelCase.toUpperCase(c.toString(), ' ');
				} 
			}
			descs[n++] = desc;
		}
		
		return new Pair<File[], String[]>(imgs, descs);
	}
}
