/*******************************************************************************
 * Copyright (c) 2005, 2010 Stein K. Skytteren and Christian Schwarz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Stein K. Skytteren and Christian Schwarz - initial API and implementation
 *******************************************************************************/
package org.cubictest.model.formElement;

import static org.cubictest.model.IdentifierType.CHECKED;
import static org.cubictest.model.IdentifierType.ID;
import static org.cubictest.model.IdentifierType.LABEL;
import static org.cubictest.model.IdentifierType.TITLE;

import java.util.ArrayList;
import java.util.List;

import org.cubictest.model.IdentifierType;


/**
 * A checkbox on the page.
 * 
 * @author chr_schwarz
 */
public class Checkbox extends Checkable {
	
	@Override
	public String getType() {
		return "Checkbox";
	}

	@Override
	public List<IdentifierType> getIdentifierTypes() {	
		List<IdentifierType> list = new ArrayList<IdentifierType>();
		list.add(LABEL);
		list.add(ID);
		list.add(IdentifierType.NAME);
		list.add(CHECKED);
		list.add(TITLE);
		return list;
	}
}
