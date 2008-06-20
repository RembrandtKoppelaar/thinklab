/**
 * AlgorithmInterpreterFactory.java
 * ----------------------------------------------------------------------------------
 * 
 * Copyright (C) 2008 www.integratedmodelling.org
 * Created: Jan 17, 2008
 *
 * ----------------------------------------------------------------------------------
 * This file is part of Thinklab.
 * 
 * Thinklab is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Thinklab is distributed in the hope that it will be useful,
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
 * @author    Ioannis N. Athanasiadis (ioannis@athanasiadis.info)
 * @date      Jan 17, 2008
 * @license   http://www.gnu.org/licenses/gpl.txt GNU General Public License v3
 * @link      http://www.integratedmodelling.org
 **/
package org.integratedmodelling.thinklab.interpreter;

import java.util.HashMap;
import java.util.Hashtable;

import org.integratedmodelling.thinklab.ConceptVisitor;
import org.integratedmodelling.thinklab.exception.ThinklabNoKMException;
import org.integratedmodelling.thinklab.exception.ThinklabUnknownLanguageException;
import org.integratedmodelling.thinklab.interfaces.IAlgorithmInterpreter;
import org.integratedmodelling.thinklab.interfaces.IConcept;
import org.integratedmodelling.thinklab.interfaces.ISession;
import org.integratedmodelling.thinklab.interfaces.IValue;
import org.integratedmodelling.thinklab.interfaces.IAlgorithmInterpreter.IContext;
import org.integratedmodelling.thinklab.plugin.PluginRegistry;
import org.integratedmodelling.thinklab.value.AlgorithmValue;

public class AlgorithmInterpreterFactory {

	private static AlgorithmInterpreterFactory AIF = null;

	// binds an Algorithm class to a plugin providing the interpreter
	Hashtable<String, String> interpreterBindings = new Hashtable<String, String>();

	// binds a session ID to an interpreter
	Hashtable<String, IAlgorithmInterpreter> interpreters = new Hashtable<String, IAlgorithmInterpreter>();
	
	// contexts can be cached, too
	HashMap<String, IContext> contexts = new HashMap<String, IContext>();
	
	private InterpreterPlugin getPlugin(IValue algorithm) throws ThinklabUnknownLanguageException {
		
		class AlgMatcher implements ConceptVisitor.ConceptMatcher {

			Hashtable<String, String> hash;

			public String plugin = null;

			public boolean match(IConcept c) {
				plugin = hash.get(c.getSemanticType().toString());
				return plugin != null;
			}

			public AlgMatcher(Hashtable<String, String> h) {
				hash = h;
			}
		}
		
		IConcept c = algorithm.getConcept();

		AlgMatcher matcher = new AlgMatcher(interpreterBindings);
		IConcept cc = ConceptVisitor.findMatchUpwards(matcher, c);

		if (cc == null) {
			throw new ThinklabUnknownLanguageException(
					"no language interpreter can be identified for " + c);
		}

		InterpreterPlugin plu = null;
		
		try {
			plu = (InterpreterPlugin) PluginRegistry.get().retrievePlugin(matcher.plugin);
		} catch (ThinklabNoKMException e) {
			// if we have plugins, we have a KM
		}

		if (plu == null) {
			throw new ThinklabUnknownLanguageException(
					"no language interpreter plugin installed for " + c);
		}
		
		return plu;
	}
	
	/**
	 * Bind an algorithm concept to a plugin ID. The plugin must be an InterpreterPlugin and
	 * is used to generate the interpreter for an algorithm of this class.
	 * @param semanticType The class of the algorithm (language interpreted).
	 * @param pluginID the name of the InterpreterPlugin that handles it.
	 */
	public void registerInterpreter(String semanticType, String pluginID) {
		interpreterBindings.put(semanticType, pluginID);			
	}
	
	/**
	 * Retrieve interpreter for given algorithm, using interpreter registry and class
	 * of algorithm. The same interpreter may be returned for the same session, as it is
	 * a Thinklab requirement that operations in the same session are synchronized.
	 * The IValue containing the algorithm as a string. May have been
	 * validated or not. The specific IConcept linked to the string will be used to
	 * select the interpreter.
	 * @param algorithm 
	 * @param session
	 * @return
	 */
	public IAlgorithmInterpreter getInterpreter(IValue algorithm,
			ISession session) throws ThinklabUnknownLanguageException {

		IAlgorithmInterpreter ret = interpreters.get(session.getSessionID());
		
		if (ret != null)
			return ret;
		
		InterpreterPlugin plu = getPlugin(algorithm);

		ret = plu.getInterpreter();

		if (ret == null)  {
			throw new ThinklabUnknownLanguageException(
					"interpreter creation for " + algorithm.getConcept() + " failed");
		}

		interpreters.put(session.getSessionID(), ret);
		
		return ret;
	}
	
	/**
	 * Call this if you want the interpreter to be renewed within the same
	 * session.
	 * 
	 * @param session
	 */
	public void deleteInterpreter(ISession session) {
		
		if (interpreters.containsKey(session.getSessionID())) {
			interpreters.remove(session.getSessionID());
		}
	}

	public static AlgorithmInterpreterFactory get() {

		if (AIF == null) {
			AIF = new AlgorithmInterpreterFactory();
		}
		return AIF;
		
	}

	public IContext getNewContext(AlgorithmValue algorithm, ISession session) throws ThinklabUnknownLanguageException {

		InterpreterPlugin plu = getPlugin(algorithm);

		IContext ret = plu.getNewContext(session);
		
		if (ret == null)  {
			throw new ThinklabUnknownLanguageException(
					"interpreter context creation for " + algorithm.getConcept() + " failed");
		}
		return ret;
	}


	public IContext getContext(AlgorithmValue algorithm, ISession session) throws ThinklabUnknownLanguageException {

		IContext ret = null;

		InterpreterPlugin plu = getPlugin(algorithm);

		if (contexts.containsKey(session.getSessionID())) {
			ret = contexts.get(session.getSessionID());
		} else {

			ret = plu.getNewContext(session);

			if (ret == null)  {
				throw new ThinklabUnknownLanguageException(
						"interpreter context creation for " + algorithm.getConcept() + " failed");
			}

			contexts.put(session.getSessionID(), ret);
		}
			
		return ret;
	}


}
