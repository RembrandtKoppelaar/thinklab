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
package org.integratedmodelling.geospace.commands;

import org.integratedmodelling.geospace.Geospace;
import org.integratedmodelling.thinklab.command.Command;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.interfaces.annotations.ThinklabCommand;
import org.integratedmodelling.thinklab.interfaces.applications.ISession;
import org.integratedmodelling.thinklab.interfaces.commands.ICommandHandler;
import org.integratedmodelling.thinklab.interfaces.literals.IValue;

@ThinklabCommand(
		name="checksrid", 
		argumentNames="srid", 
		argumentTypes="thinklab-core:Text", 
		argumentDescriptions="SRID code to check (with authority)")
public class CheckSRID implements ICommandHandler {

	@Override
	public IValue execute(Command command, ISession session)
			throws ThinklabException {
		String srid = command.getArgumentAsString("srid");
		org.opengis.referencing.crs.CoordinateReferenceSystem crs = null;
		
		try {
			crs = Geospace.getCRSFromID(srid);			
		} catch (ThinklabException e) {
		}

		if (crs == null) {
			session.getOutputStream().println("SRID " + srid + " not recognized");
		} else {
			session.getOutputStream().println("SRID " + srid + "OK" + "\n" + crs);
		}
 		return null;
	}

}
