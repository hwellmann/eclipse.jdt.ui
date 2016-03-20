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

import org.eclipse.jdt.internal.junit.runner.IListensToTestExecutions;
import org.eclipse.jdt.internal.junit.runner.MessageIds;
import org.eclipse.jdt.internal.junit.runner.TestReferenceFailure;
import org.junit.gen5.commons.util.ExceptionUtils;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.TestExecutionResult.Status;
import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestIdentifier;

public class JUnit5TestListener implements TestExecutionListener {

	private final IListensToTestExecutions notified;

	public JUnit5TestListener(IListensToTestExecutions notified) {
		this.notified = notified;
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		if (testIdentifier.isTest()) {
			notified.notifyTestStarted(new JUnit5Identifier(testIdentifier));
		}
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (testIdentifier.isTest()) {
			JUnit5Identifier identifier = new JUnit5Identifier(testIdentifier);
			String status = MessageIds.TEST_ERROR;
			String trace = null;
			if (testExecutionResult.getThrowable().isPresent()) {
				if (testExecutionResult.getThrowable().get() instanceof AssertionError) {
					status = MessageIds.TEST_FAILED;
				}
				trace = ExceptionUtils.readStackTrace(testExecutionResult.getThrowable().get());
			}
			
			if (testExecutionResult.getStatus() == Status.SUCCESSFUL) {
				notified.notifyTestEnded(identifier);
			}
			else {
				TestReferenceFailure failure = new TestReferenceFailure(identifier, status, trace);
				notified.notifyTestFailed(failure);
			}
		}
	}
}
