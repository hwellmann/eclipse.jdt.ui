/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.ui.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.core.resources.IResource;

import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.jdt.core.IClasspathEntry;import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.ui.JavaPlugin;import org.eclipse.jdt.internal.ui.util.JavaModelUtility;


/**
 * This is a dummy PropertyPage for JavaElements.
 * Copied from the ResourceInfoPage
 */
public class JavaElementInfoPage extends PropertyPage {
	protected Control createContents(Composite parent) {

		// ensure the page has no special buttons
		noDefaultAndApplyButton();

		IJavaElement element= (IJavaElement)getElement();
		
		IResource resource= null;
		try {
			resource= element.getUnderlyingResource(); 
		} catch (JavaModelException e) {
			JavaPlugin.getDefault().logErrorStatus("Creating ElementInfoPage", e.getStatus());
		}
		
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		Label nameLabel= new Label(composite, SWT.NONE);
		nameLabel.setText("Name: ");

		Label nameValueLabel= new Label(composite, SWT.NONE);
		nameValueLabel.setText(element.getElementName());

		if (resource != null) {
			// path label
			Label pathLabel= new Label(composite, SWT.NONE);
			pathLabel.setText("Resource path: ");

			// path value label
			Label pathValueLabel= new Label(composite, SWT.NONE);
			pathValueLabel.setText(resource.getFullPath().toString());
		}
		if (element instanceof ICompilationUnit) {
			ICompilationUnit unit= (ICompilationUnit)element;
			Label packageLabel= new Label(composite, SWT.NONE);
			packageLabel.setText("Package: ");
			Label packageName= new Label(composite, SWT.NONE);
			packageName.setText(unit.getParent().getElementName());
			
		}
		if (element instanceof IPackageFragment) {
			IPackageFragment packageFragment= (IPackageFragment)element;
			Label packageContents= new Label(composite, SWT.NONE);
			packageContents.setText("Package contents: ");
			Label packageContentsType= new Label(composite, SWT.NONE);
			try {
				if (packageFragment.getKind() == IPackageFragmentRoot.K_SOURCE) 
					packageContentsType.setText("source");
				else
					packageContentsType.setText("binary");
			} catch (JavaModelException e) {
				packageContentsType.setText("not present");
			}
		}
		if (element instanceof IPackageFragmentRoot) {
			Label rootContents= new Label(composite, SWT.NONE);
			rootContents.setText("Classpath entry kind: ");
			Label rootContentsType= new Label(composite, SWT.NONE);
			try {
				IClasspathEntry entry= JavaModelUtility.getRawClasspathEntry((IPackageFragmentRoot)element);
				if (entry != null) {
					switch (entry.getEntryKind()) {
						case IClasspathEntry.CPE_SOURCE:
							rootContentsType.setText("source"); break;
						case IClasspathEntry.CPE_PROJECT:
							rootContentsType.setText("project"); break;
						case IClasspathEntry.CPE_LIBRARY:
							rootContentsType.setText("library"); break;
						case IClasspathEntry.CPE_VARIABLE:
							rootContentsType.setText("variable");
							Label varPath= new Label(composite, SWT.NONE);
							varPath.setText("Variable path: ");
							Label varPathVar= new Label(composite, SWT.NONE);
							varPathVar.setText(entry.getPath().makeRelative().toString());							
							break;
					}
				} else {
					rootContentsType.setText("not present");
				}
			} catch (JavaModelException e) {
				rootContentsType.setText("not present");
			}
		}		
		
		return composite;
	}

	/**
	 */
	protected boolean doOk() {
		// nothing to do - read-only page
		return true;
	}

}
