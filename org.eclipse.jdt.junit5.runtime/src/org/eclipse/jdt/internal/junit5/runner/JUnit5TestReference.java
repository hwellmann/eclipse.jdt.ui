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

import java.util.Set;

import org.eclipse.jdt.internal.junit.runner.ITestIdentifier;
import org.eclipse.jdt.internal.junit.runner.ITestReference;
import org.eclipse.jdt.internal.junit.runner.IVisitsTestTrees;
import org.eclipse.jdt.internal.junit.runner.TestExecution;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestDiscoveryRequest;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;

public class JUnit5TestReference implements ITestReference {

	private Launcher launcher;
	private TestPlan testPlan;
	private TestDiscoveryRequest request;

	public JUnit5TestReference(Launcher launcher, TestDiscoveryRequest request, TestPlan testPlan) {
		this.launcher = launcher;
		this.request = request;
		this.testPlan = testPlan;
	}

	@Override
	public int countTestCases() {
		return (int) testPlan.countTestIdentifiers(i -> i.isTest());
	}

	@Override
	public void sendTree(IVisitsTestTrees notified) {
		TestIdentifier testIdentifier = testPlan.getRoots().iterator().next();
		sendTree(notified, testPlan.getChildren(testIdentifier).iterator().next());
	}

	private void sendTree(IVisitsTestTrees notified, TestIdentifier testIdentifier) {
		if (testIdentifier.isTest()) {
			notified.visitTreeEntry(new JUnit5Identifier(testIdentifier), false, 1);
		} else {
			Set<TestIdentifier> children = testPlan.getChildren(testIdentifier);
			notified.visitTreeEntry(new JUnit5Identifier(testIdentifier), true, children.size());
			for (TestIdentifier child : children) {
				sendTree(notified, child);
			}
		}
	}

	@Override
	public void run(TestExecution execution) {
		JUnit5TestListener listener = new JUnit5TestListener(execution.getListener());
		launcher.registerTestExecutionListeners(listener);
		launcher.execute(request);
	}

	@Override
	public ITestIdentifier getIdentifier() {
		TestIdentifier testIdentifier = testPlan.getRoots().iterator().next();
		return new JUnit5Identifier(testIdentifier);
	}
}
