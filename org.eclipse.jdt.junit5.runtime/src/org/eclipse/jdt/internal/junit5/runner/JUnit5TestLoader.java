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

import static org.junit.gen5.engine.discovery.ClassSelector.forClass;
import static org.junit.gen5.engine.discovery.MethodSelector.forMethod;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import org.eclipse.jdt.internal.junit.runner.ITestLoader;
import org.eclipse.jdt.internal.junit.runner.ITestReference;
import org.eclipse.jdt.internal.junit.runner.RemoteTestRunner;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestDiscoveryRequest;
import org.junit.gen5.launcher.TestPlan;
import org.junit.gen5.launcher.main.LauncherFactory;

public class JUnit5TestLoader implements ITestLoader {
	
	private Launcher launcher;

	public JUnit5TestLoader() {
		this.launcher = LauncherFactory.create();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public ITestReference[] loadTests(Class[] testClasses, String testName, String[] failureNames,
			RemoteTestRunner listener) {

		ITestReference[] refs = new ITestReference[testClasses.length];
		for (int i = 0; i < testClasses.length; i++) {
			Class<?> clazz = testClasses[i];
			ITestReference ref = createTest(clazz, testName, failureNames, listener);
			refs[i] = ref;
		}
		return refs;
	}

	private ITestReference createTest(Class<?> clazz, String testName, String[] failureNames, RemoteTestRunner listener) {
		if (clazz == null) {
			return null;
		}
		if (testName != null) {
			return createFilteredTest(clazz, testName, failureNames);
		}
		return createUnfilteredTest(clazz, failureNames);
	}

	private ITestReference createFilteredTest(Class<?> clazz, String testName, String[] failureNames) {
		TestDiscoveryRequest request = request().select(forMethod(clazz, testName)).build();
		TestPlan testPlan = launcher.discover(request);
		return new JUnit5TestReference(launcher, request, testPlan);		
	}
	
	private ITestReference createUnfilteredTest(Class<?> clazz, String[] failureNames) {
		TestDiscoveryRequest request = request().select(forClass(clazz)).build();
		TestPlan testPlan = launcher.discover(request);
		return new JUnit5TestReference(launcher, request, testPlan);		
	}
}
