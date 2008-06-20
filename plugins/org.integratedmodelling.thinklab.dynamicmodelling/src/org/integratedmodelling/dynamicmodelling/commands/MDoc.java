/**
 * MDoc.java
 * ----------------------------------------------------------------------------------
 * 
 * Copyright (C) 2008 www.integratedmodelling.org
 * Created: Jan 21, 2008
 *
 * ----------------------------------------------------------------------------------
 * This file is part of ThinklabDynamicModellingPlugin.
 * 
 * ThinklabDynamicModellingPlugin is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ThinklabDynamicModellingPlugin is distributed in the hope that it will be useful,
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
package org.integratedmodelling.dynamicmodelling.commands;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Vector;

import org.integratedmodelling.dynamicmodelling.DynamicModellingPlugin;
import org.integratedmodelling.dynamicmodelling.interfaces.IModelLoader;
import org.integratedmodelling.dynamicmodelling.loaders.ModelDocumentationGenerator;
import org.integratedmodelling.dynamicmodelling.loaders.ModelOWLLoader;
import org.integratedmodelling.dynamicmodelling.model.Model;
import org.integratedmodelling.dynamicmodelling.simile.SimilePrologReader;
import org.integratedmodelling.thinklab.KnowledgeManager;
import org.integratedmodelling.thinklab.command.Command;
import org.integratedmodelling.thinklab.command.CommandDeclaration;
import org.integratedmodelling.thinklab.command.CommandPattern;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.exception.ThinklabIOException;
import org.integratedmodelling.thinklab.exception.ThinklabPluginException;
import org.integratedmodelling.thinklab.interfaces.IAction;
import org.integratedmodelling.thinklab.interfaces.ICommandOutputReceptor;
import org.integratedmodelling.thinklab.interfaces.ISession;
import org.integratedmodelling.thinklab.interfaces.IValue;
import org.integratedmodelling.utils.Polylist;

/**
 * Command to invoke the parser on a Simile file. It's a temporary command meant for testing only, 
 * which will be phased out after model files are seen as sources of knowledge for the load 
 * command (and the loadObjects function in sessions). 
 * 
 * @author Ferdinando Villa
 *
 */
public class MDoc extends CommandPattern {

	class MDocAction implements IAction {

		public IValue execute(Command command, ICommandOutputReceptor outputDest, ISession session, KnowledgeManager km) throws ThinklabException {
			
			String msource = command.getArgumentAsString("m1");
			String loader = "doc";
			IModelLoader l = null;
			
			if (command.hasOption("loader")) {
				loader = command.getOptionAsString("loader");

				/* look for loader */
				l = DynamicModellingPlugin.get().retrieveModelLoader(loader);
			
				if (l == null) {
					throw new ThinklabPluginException("no loader registered under name " + loader + " to interpret model");
				}
			}
			
			// default to documentation loader
			if (l == null) {
				DynamicModellingPlugin.get().retrieveModelLoader(loader);
			}
			
			l.loadModel(msource);
						
			return null;
		}
		
	}
	

	@Override
	public CommandDeclaration createCommand()  throws ThinklabException  {
		CommandDeclaration cd = new CommandDeclaration("mdoc", "parse a model file and create HTML documentation for it");
		
		cd.addOption("l", "loader", "loader", "alternative loader to use model knowledge", KnowledgeManager.Text().getSemanticType());
		cd.addMandatoryArgument("m1", "model URL", KnowledgeManager.Text().getSemanticType());		
		
		return cd;
	}

	@Override
	public IAction createAction() {
		return new MDocAction();
	}
	
}
