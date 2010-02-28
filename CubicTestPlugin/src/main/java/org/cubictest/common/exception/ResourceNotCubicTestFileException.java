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
package org.cubictest.common.exception;

public class ResourceNotCubicTestFileException extends CubicException {
	private static final long serialVersionUID = 1L;
	
	public ResourceNotCubicTestFileException() {
		super("");
	}
	
	public ResourceNotCubicTestFileException(String message) {
		super(message);
	}
	
	@Override
	public String toString() {
		return "Resource is not a CubicTest test file!";
	}
}
