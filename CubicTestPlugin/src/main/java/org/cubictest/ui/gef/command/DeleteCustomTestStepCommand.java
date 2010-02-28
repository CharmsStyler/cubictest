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
package org.cubictest.ui.gef.command;

import org.cubictest.model.CustomTestStepHolder;


/**
 * @author Stein K. Skytteren
 *
 *
 * A command that deletes an <code>AbstractPage</code>.
 */
public class DeleteCustomTestStepCommand extends DeleteTransitionNodeCommand {
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute(){
		super.execute();
		test.removeCustomTestSteps((CustomTestStepHolder) transitionNode);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo(){
		super.undo();
		test.addCustomTestStep((CustomTestStepHolder) transitionNode);
	}
}
