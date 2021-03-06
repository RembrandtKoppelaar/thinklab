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
package org.integratedmodelling.thinklab.interfaces.knowledge;

import org.integratedmodelling.thinklab.SemanticType;

/**
 * IKnowledge defines the methods that are common to both IConcepts and
 * IInstances. As IConcept can be both concrete and abstract, and IInstances are always
 * concrete, IKnowledge has all the methods that pertain to IRelationships of this
 * with other IKnowledge objects. 
 * 
 * @author Ferdinando Villa
 *
 */
public interface IKnowledge extends IResource {

	/**
	 * The semantic type of the resource. IKnowledge objects always have a semantic type.
	 * @return a semantic type. Can't fail.
	 */
	public SemanticType getSemanticType();

	
	/**
	 * All IKnowledge objects have a local name 
	 * @return
	 */
	public String getLocalName();
	
	
    /**
     * True if this is subsumed by the passed resource.
     */
    public abstract boolean is(IKnowledge concept);

    /*
     * Use a string to identify the resource passed. Return false even if string
     * does not represent a resource.
     */
    public abstract boolean is(String semanticType);
    
	/**
	 * Set a specific annotation into this object.
	 * 
	 * @param property
	 * @param value
	 */
	public abstract void addAnnotation(String property, String value);
	
	/**
	 * Return value of said annotation property, or null if not 
	 * existing.
	 * 
	 * @param property
	 * @return
	 */
	public abstract String getAnnotation(String property);
    
}
