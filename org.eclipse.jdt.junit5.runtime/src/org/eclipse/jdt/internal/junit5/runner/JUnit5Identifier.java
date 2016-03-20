/*******************************************************************************
 * Copyright (c) 2016 Harald Wellmann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Harald Wellmann <hwellmann.de@gmail.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.junit5.runner;

import org.eclipse.jdt.internal.junit.runner.ITestIdentifier;
import org.junit.gen5.launcher.TestIdentifier;

public class JUnit5Identifier implements ITestIdentifier {
	
	private TestIdentifier testIdentifier;
	
	public JUnit5Identifier(TestIdentifier testIdentifier) {
		this.testIdentifier = testIdentifier;
	}

	@Override
	public String getName() {
		String name = testIdentifier.getName();
		int hash = name.indexOf('#');
		int paren = name.indexOf('(');
		if (hash == -1 || paren == -1) {
			return testIdentifier.getDisplayName();
		}

		String className = name.substring(0, hash);
		String methodName = name.substring(hash + 1, paren);
		return String.format("%s(%s)", methodName, className);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((testIdentifier == null) ? 0 : testIdentifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JUnit5Identifier other = (JUnit5Identifier) obj;
		if (testIdentifier == null) {
			if (other.testIdentifier != null)
				return false;
		} else if (!testIdentifier.equals(other.testIdentifier))
			return false;
		return true;
	}
}
