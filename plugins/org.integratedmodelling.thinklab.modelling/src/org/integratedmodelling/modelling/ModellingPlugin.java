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
package org.integratedmodelling.modelling;

import org.integratedmodelling.modelling.loader.ModelResourceLoader;
import org.integratedmodelling.modelling.model.ModelFactory;
import org.integratedmodelling.modelling.visualization.VisualizationFactory;
import org.integratedmodelling.thinklab.KnowledgeManager;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.plugin.ThinklabPlugin;

public class ModellingPlugin extends ThinklabPlugin {

	private static final String USE_CACHE_PROPERTY = "modelling.use.cache";

	// this is set externally in a session to mean that we are annotating 
	// models, so concepts should be created for them when undefined instead
	// of complaining.
//	public static final String ANNOTATION_UNDERWAY = "annotation.underway";

	public static String PLUGIN_ID = "org.integratedmodelling.thinklab.modelling";
	
	private ModelFactory manager = null;
	
	// TODO move cache to ModelFactory
	private ObservationCache cache = null;

	public static final String STATEFUL_MERGER_OBSERVATION = "modeltypes:MergerObservation";

	public static final String UNITS_ANNOTATION = "modeltypes:hasUnitDescription";
	public static final String RANGE_ANNOTATION = "modeltypes:hasRangeDescription";
	public static final String EDITABLE_ANNOTATION = "modeltypes:isEditable";
	
	public static ModellingPlugin get() {
		return (ModellingPlugin) getPlugin(PLUGIN_ID);
	}
	
	@Override
	protected void load(KnowledgeManager km) throws ThinklabException {

		boolean persistent = false;
		manager = new ModelFactory();
		if (getProperties().containsKey(USE_CACHE_PROPERTY) &&
			Boolean.parseBoolean(getProperties().getProperty(USE_CACHE_PROPERTY))) {
			persistent = true;
		}
		cache = new ObservationCache(getScratchPath(), persistent);
		
		/*
		 * add whatever defaults we have in the colormap chooser
		 */
		VisualizationFactory.get().loadColormapDefinitions(getProperties());
		
		/*
		 * install resource loader for future plugins
		 */
		installResourceLoader(new ModelResourceLoader());
		
	}

	@Override
	protected void unload() throws ThinklabException {
	}

	public ModelFactory getModelManager() {
		return manager;
	}
	
	// TODO move cache to ModelFactory
	public ObservationCache getCache() {
		return cache;
	}

}
