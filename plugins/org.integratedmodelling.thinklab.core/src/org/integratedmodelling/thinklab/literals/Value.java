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
package org.integratedmodelling.thinklab.literals;

import org.integratedmodelling.thinklab.KnowledgeManager;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.exception.ThinklabNoKMException;
import org.integratedmodelling.thinklab.exception.ThinklabValidationException;
import org.integratedmodelling.thinklab.exception.ThinklabValueConversionException;
import org.integratedmodelling.thinklab.interfaces.knowledge.IConcept;
import org.integratedmodelling.thinklab.interfaces.literals.IValue;
import org.integratedmodelling.utils.Polylist;

/**
 * <p>A generalized container for a value that always has a concept associated. The value may be defined from a literal or a basic
 * type, or be a concept or an instance in itself. It's the most general idea of a value, but it has an unbreakable
 * association to the knowledge base.</p>
 * 
 * <p>The Value can have a value (oh yes), which simply means that a Java object
 * can be associated to the concept and represent an instance implementation, substituting a full Instance object
 * with properties. Typically this applies to objects that are created from literals and belong to simple
 * types, such as text or numbers, although more complex ones (e.g. JTS shapes) are also possible.</p>
 * 
 * <p>The value may be as multiple as necessary, e.g. a Collection or a Polylist, as long as the associated concept is
 * unambiguous in suggesting that. If the multiplicity depends on the properties, then a full Instance should be
 * used.</p>
 *
 * <p>The least a Value can do is to be a Concept, so the base Value class is exactly that, and even the trivial constructor
 * assigns a concept (the most general one) to it.</p>
 *
 * <p>By its own nature, a Value is perfect to implement a stack for a complex language or a return type for any
 * action. In fact, it's used exactly for these purposes in JIMT, along with storage of literals in relationships.</p>
 * 
 * <p>Efficiency of the approach is not maximal, obviously, but if you want it efficient, use the C++ implementation,
 * not this one. On the other hand, the C++ implementation WILL drive you crazy, and this probably won't, unless
 * you're crazy already.</p>
 * 
 * <p>The IMA core provides implementations of numbers, text, booleans, concepts and object(instance) values. These
 * should be enough for a lot of applications, given that some degree of polymorphism is provided by the concept side.
 * Other packages (e.g. time and space) provide more. Note that implementing a Value subclass, unless
 * limited to specialization of the associated concept, isn't trivial due to
 * handling of operators, cloning, and proper checking methods, so should be done carefully.</p>
 * 
 * @author Ferdinando Villa
 */
public class Value implements IValue {
	
	
	/* (non-Javadoc)
     * @see org.integratedmodelling.ima.core.value.IValue#setToCommonConcept(org.integratedmodelling.ima.core.IConcept, org.integratedmodelling.ima.core.IConcept)
     */
	public void setToCommonConcept(IConcept setTo, IConcept mustBe) throws ThinklabValueConversionException {
		IConcept cc = null;
		try {
			cc = KnowledgeManager.get().getLeastGeneralCommonConcept(getConcept(), setTo);
		} catch (ThinklabNoKMException e) {
		}
		if (cc == null || !cc.is(mustBe)) {
			throw new ThinklabValueConversionException("concept " + concept.getSemanticType().toString() + 
												  " can't be set to " + setTo.getSemanticType().toString());
		}
		concept = cc;
	}
	
	public IConcept concept;
	public String ID = null;
	
    public Value()  {
        /* the zero of knowledge, ladies and gentlemen, and it's not null */
        concept = KnowledgeManager.Thing();
    }
    
    /* (non-Javadoc)
     * @see org.integratedmodelling.ima.core.value.IValue#clone()
     */
    public Object clone() {
    	return new Value(concept);
    }
    
    public Value(IConcept c) {
        concept = c;
    }
   	
    /* (non-Javadoc)
     * @see org.integratedmodelling.ima.core.value.IValue#isNumber()
     */
    public boolean isNumber() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.integratedmodelling.ima.core.value.IValue#isText()
     */
    public boolean isText() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.integratedmodelling.ima.core.value.IValue#isBoolean()
     */
    public boolean isBoolean() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.integratedmodelling.ima.core.value.IValue#isClass()
     */
    public boolean isClass() {
        return true;
    }
 
    /* (non-Javadoc)
     * @see org.integratedmodelling.ima.core.value.IValue#isObject()
     */
    public boolean isObject() {
        return false;
    }
    
    public boolean isObjectReference() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.integratedmodelling.ima.core.value.IValue#isLiteral()
     */
    public boolean isLiteral() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.integratedmodelling.ima.core.value.IValue#asNumber()
     */
    public NumberValue asNumber() throws ThinklabValueConversionException {
        throw new ThinklabValueConversionException("value " + toString() + " cannot be converted to a number");
    }
    
    /* (non-Javadoc)
     * @see org.integratedmodelling.ima.core.value.IValue#asText()
     */
    public TextValue asText() throws ThinklabValueConversionException {
        throw new ThinklabValueConversionException("value " + toString() + " cannot be converted to text");
    }

    /* (non-Javadoc)
     * @see org.integratedmodelling.ima.core.value.IValue#asObject()
     */
    public ObjectValue asObject() throws ThinklabValueConversionException {
        throw new ThinklabValueConversionException("value " + toString() + " cannot be converted to an object");
    }
    
    /* (non-Javadoc)
     * @see org.integratedmodelling.ima.core.value.IValue#asBoolean()
     */
    public BooleanValue asBoolean() throws ThinklabValueConversionException {
        throw new ThinklabValueConversionException("value " + toString() + " cannot be converted to a boolean");
    }

    public ObjectReferenceValue asObjectReference() throws ThinklabValueConversionException {
        throw new ThinklabValueConversionException("value " + toString() + " cannot be converted to an object reference");
    }

    
    /* (non-Javadoc)
     * @see org.integratedmodelling.ima.core.value.IValue#toString()
     */
    @Override
	public String toString() {
		return concept.getSemanticType().toString();
	}

    /** 
     * Return a new Value of the appropriate type for the class of the passed object, which must be of a simple Java
     * type.
     * @param value the Object, which can only be Integer, Double, Float, Long, String or Boolean
     * @return the correspondent Value, which can be a NumberValue, a TextValue, or a BooleanValue
     * @throws ThinklabValidationException
     * @throws ThinklabNoKMException
     */
    public static IValue getValueForObject(Object value) throws ThinklabException {

        Value ret = null;
        
        if (value instanceof IValue)
        	return (IValue) value;
        
        if (value instanceof Integer) {
            ret = new NumberValue((Integer)value);
        } else if (value instanceof Float) {
            ret = new NumberValue((Float)value);
        } else if (value instanceof Double) {
            ret = new NumberValue((Double)value);
        } else if (value instanceof Long) {
            ret = new NumberValue((Long)value);            
        } else if (value instanceof String) {
            ret = new TextValue((String)value);
        } else if (value instanceof Boolean) {
            ret = new BooleanValue((Boolean)value);
        }  else if (value instanceof Polylist) {
            ret = new ListValue((Polylist)value);
        } else 

        	/*
        	 * FIXME we should make this translation more flexible, but this should be the best
        	 * catch-all case.
        	 */
        	ret = new TextValue(value.toString());
        	// throw new ThinklabValidationException("No automatic value generation for class " + value.getClass().toString());

        return ret;
    }

    /** 
     * Return a new Value of the appropriate type for the class of the passed object, which must be of a simple Java
     * type, setting the correspondent concept to the passed one. Concept is validated to make sure that it subsumes the
     * IMA type configured for the literal that represents it. 
     * @param value the Object, which can only be Integer, Double, Float, Long, String or Boolean
     * @param concept the concept expressed by the object, which must validate to a base IMA concept that fits it.
     * @return the correspondent Value, which can be a NumberValue, a TextValue, or a BooleanValue
     * @throws ThinklabValidationException
     * @throws ThinklabNoKMException
     */
    public static IValue getValidatedValueForObject(Object value, IConcept concept) 
    throws ThinklabException {

        IValue ret = null;
        
        if (value.getClass() == Integer.TYPE) {
            ret = new NumberValue((Integer)value);
        } else if (value.getClass() == Float.TYPE) {
            ret = new NumberValue((Float)value);
        } else if (value.getClass() == Double.TYPE) {
            ret = new NumberValue((Double)value);
        } else if (value.getClass() == Long.TYPE) {
            ret = new NumberValue((Long)value);            
        } else if (value.getClass() == String.class) {
            ret = new TextValue((String)value);
        } else if (value.getClass() == Boolean.TYPE) {
            ret = new BooleanValue((Boolean)value);
        } else 

        	/* 
        	 * FIXME
        	 * We should make this more flexible, but this is probably the best catch
        	 * clause for now. Problem is, things like RDFSLiteral get passed, and who
        	 * knows what they are here. But it's quite likely they're strings, until
        	 * we pluginize the translation.
        	 */
        	ret = new TextValue(value.toString());
        	// throw new ThinklabValidationException("No automatic value generation for class " + value.getClass().toString());

        if (ret != null)
            ret.setConceptWithValidation(concept);
        
        return ret;

    }

    /** 
     * Return a new Value of the appropriate type for the class of the passed object, which must be of a simple Java
     * type, setting the correspondent concept to the passed one. Concept is validated to make sure that it subsumes the
     * IMA type configured for the literal that represents it. 
     * @param value the Object, which can only be Integer, Double, Float, Long, String or Boolean
     * @param concept the concept expressed by the object, which must validate to a base IMA concept that fits it.
     * @return the correspondent Value, which can be a NumberValue, a TextValue, or a BooleanValue
     * @throws ThinklabValidationException
     * @throws ThinklabNoKMException
     */
    public static IValue getNonValidatedValueForObject(Object value, IConcept concept) 
    throws ThinklabException {

        IValue ret = null;
        
        if (value.getClass() == Integer.TYPE) {
            ret = new NumberValue((Integer)value);
        } else if (value.getClass() == Float.TYPE) {
            ret = new NumberValue((Float)value);
        } else if (value.getClass() == Double.TYPE) {
            ret = new NumberValue((Double)value);
        } else if (value.getClass() == Long.TYPE) {
            ret = new NumberValue((Long)value);            
        } else if (value.getClass() == String.class) {
            ret = new TextValue((String)value);
        } else if (value.getClass() == Boolean.TYPE) {
            ret = new BooleanValue((Boolean)value);
        } else 
            throw new ThinklabValidationException("No automatic value generation for class " + value.getClass().toString());

        if (ret != null)
            ret.setConceptWithoutValidation(concept);
        
        return ret;

    }

    /* (non-Javadoc)
     * @see org.integratedmodelling.ima.core.value.IValue#setConceptWithValidation(org.integratedmodelling.ima.core.IConcept)
     */
    public void setConceptWithValidation(IConcept concept) throws ThinklabValidationException {

    	// FIXME USE CLASSTREE?
        if (!concept.is(this.concept)) 
            throw new ThinklabValidationException("concept " + concept.getSemanticType().toString() + 
                                         " is not a " + this.concept.getSemanticType().toString());            
        this.concept = concept;
    }

    /* (non-Javadoc)
     * @see org.integratedmodelling.ima.core.value.IValue#setConceptWithoutValidation(org.integratedmodelling.ima.core.IConcept)
     */
    public void setConceptWithoutValidation(IConcept concept) {
        this.concept = concept;
    }
    
    /* (non-Javadoc)
     * @see org.integratedmodelling.ima.core.value.IValue#isPODType()
     */
    public boolean isPODType() {
    	return 
            (this.getClass() ==  TextValue.class) ||
            (this.getClass() == NumberValue.class) || 
            (this.getClass() == BooleanValue.class);
    }
    
    /* (non-Javadoc)
     * @see org.integratedmodelling.ima.core.value.IValue#isPODType()
     */
    public static boolean isPOD(IConcept c) {
    	return 
            c.is(KnowledgeManager.Number()) ||
            c.is(KnowledgeManager.Text()) ||
            c.is(KnowledgeManager.Boolean());
    }

    public boolean isList() {
        return false;
    }

	public String getID() {
		return ID;
	}

	public void setID(String localName) {
		ID = localName;
	}

	/**
	 * Return the concept that our value represents.
	 */
	public IConcept getConcept() {
		return concept;
	}
	
	@Override
	public Object demote() {
		return concept;
	}
   
    
}
