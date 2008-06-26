/**
 * SearchEnginePlugin.java
 * ----------------------------------------------------------------------------------
 * 
 * Copyright (C) 2008 www.integratedmodelling.org
 * Created: Jan 21, 2008
 *
 * ----------------------------------------------------------------------------------
 * This file is part of ThinklabSearchEnginePlugin.
 * 
 * ThinklabSearchEnginePlugin is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ThinklabSearchEnginePlugin is distributed in the hope that it will be useful,
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
 * @date      Jan 21, 2008
 * @license   http://www.gnu.org/licenses/gpl.txt GNU General Public License v3
 * @link      http://www.integratedmodelling.org
 **/
package org.integratedmodelling.searchengine;

import java.util.ArrayList;
import java.util.Properties;

import org.integratedmodelling.thinklab.KnowledgeManager;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.plugin.ThinklabPlugin;

public class SearchEnginePlugin extends ThinklabPlugin {

	static final String ID = "org.integratedmodelling.thinklab.searchengine";
	
	ArrayList<SearchEngine> engines = new ArrayList<SearchEngine>();
	
	/**
	 * Set to a true value to enable indexing of individuals contained in
	 * ontologies. Kbox indexing is not affected by this property.
	 */
	public static final String SEARCHENGINE_INDEX_INDIVIDUALS_PROPERTY = 
		"searchengine.%.index.individuals";
	
	/**
	 * Set to the path where you want the Lucene index to be created. Default
	 * is scratch path + /index.
	 */
	public static final String SEARCHENGINE_INDEX_PATH_PROPERTY = 
		"searchengine.%.index.path";
	
	/**
	 * If set to a true value, concepts without comments or labels are indexed using
	 * their id. Otherwise they're ignored. Default is false.
	 */
	public static final String SEARCHENGINE_INDEX_UNCOMMENTED_PROPERTY = 
		"searchengine.%.index.uncommented";
	
	/**
	 * Class to use for the analyzer; if not supplied, the standard
	 * Lucene analyzer (English) is used.
	 */
	public static final String SEARCHENGINE_ANALYZER_CLASS_PROPERTY = 
		"searchengine.%.analyzer.class";
	
	
	public static final String SEARCHENGINE_INDEX_TYPES_PROPERTY =
		"searchengine.%.index.types";
	
	/**
	 * Comma-separated list of kbox URLs that should be indexed
	 */
	public static final String SEARCHENGINE_KBOX_LIST_PROPERTY = 
		"searchengine.%.kbox";
	
	/**
	 * Ontologies listed here will be included unless "all" is one of the ontologies, then
	 * all will be included unless listed here with a ! in front of them.
	 */
	public static final String SEARCHENGINE_INDEX_ONTOLOGIES_PROPERTY = 
		"searchengine.%.index.ontologies";
	
	public static SearchEnginePlugin get() {
		return (SearchEnginePlugin) getPlugin(ID );
	}
	
	/**
	 * Get your engine here, passing the necessary configuration properties. 
	 * 
	 * @param id
	 * @param properties
	 * @return
	 * @throws ThinklabException
	 */
	public SearchEngine createSearchEngine(String id, Properties properties) throws ThinklabException {
		
		SearchEngine engine = new SearchEngine(id, properties);
		engines.add(engine);
		return engine;
	}


	@Override
	protected void load(KnowledgeManager km) throws ThinklabException {

		// create all search engines defined in the plugin properties. Others may
		// be created, typically as kbox wrappers. In that case, the kbox properties
		// define the engine's parameters.
		String engines = this.getProperties().getProperty("searchengine.new");
		
		if (engines != null) {
			String[] eng = engines.split(",");
			
			for (String e : eng) {
				createSearchEngine(e, getProperties());
			}
		}
	}

	@Override
	protected void unload() throws ThinklabException {
		// drop all search engines, close them		
		engines.clear();
	}	

}
