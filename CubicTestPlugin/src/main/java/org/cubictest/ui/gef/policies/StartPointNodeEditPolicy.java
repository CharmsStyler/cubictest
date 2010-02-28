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
package org.cubictest.ui.gef.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ReconnectRequest;

/**
 * @author Stein K. Skytteren
 *
 */
public class StartPointNodeEditPolicy extends AbstractNodeEditPolicy {
	
	protected Command getConnectionCompleteCommand(){
		return null;
	}

	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		return null;
	}
}
