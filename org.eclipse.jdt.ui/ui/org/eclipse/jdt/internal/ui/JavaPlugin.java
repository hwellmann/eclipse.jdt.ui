/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IBufferFactory;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.WorkingCopyOwner;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.ConfigurationElementSorter;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;

import org.eclipse.jdt.internal.corext.javadoc.JavaDocLocations;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplates;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.corext.template.java.JavaDocContextType;
import org.eclipse.jdt.internal.corext.template.java.Templates;
import org.eclipse.jdt.internal.corext.util.AllTypesCache;

import org.eclipse.jdt.internal.ui.browsing.LogicalPackage;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.internal.ui.javaeditor.ClassFileDocumentProvider;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider;
import org.eclipse.jdt.internal.ui.javaeditor.CustomBufferFactory;
import org.eclipse.jdt.internal.ui.javaeditor.DocumentAdapter;
import org.eclipse.jdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider;
import org.eclipse.jdt.internal.ui.javaeditor.WorkingCopyManager;
import org.eclipse.jdt.internal.ui.preferences.MembersOrderPreferenceCache;
import org.eclipse.jdt.internal.ui.preferences.MockupPreferenceStore;
import org.eclipse.jdt.internal.ui.text.ChainedPreferenceStore;
import org.eclipse.jdt.internal.ui.text.PreferencesAdapter;
import org.eclipse.jdt.internal.ui.text.java.hover.JavaEditorTextHoverDescriptor;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.internal.ui.viewsupport.ProblemMarkerManager;

import org.eclipse.jdt.ui.IContextMenuConstants;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.JavaTextTools;

import org.eclipse.ltk.core.refactoring.RefactoringCore;

/**
 * Represents the java plug-in. It provides a series of convenience methods such as
 * access to the workbench, keeps track of elements shared by all editors and viewers
 * of the plug-in such as document providers and find-replace-dialogs.
 */
public class JavaPlugin extends AbstractUIPlugin {
	/** @deprecated Will stay true */
	public static final boolean USE_WORKING_COPY_OWNERS= true;
	/**
	 * The key to store customized templates. 
	 * @since 3.0
	 */
	private static final String TEMPLATES_KEY= "org.eclipse.jdt.ui.text.custom_templates"; //$NON-NLS-1$
	/**
	 * The key to store customized code templates. 
	 * @since 3.0
	 */
	private static final String CODE_TEMPLATES_KEY= "org.eclipse.jdt.ui.text.custom_code_templates"; //$NON-NLS-1$
	/**
	 * The key to store whether the legacy templates have been migrated 
	 * @since 3.0
	 */
	private static final String TEMPLATES_MIGRATION_KEY= "org.eclipse.jdt.ui.text.templates_migrated"; //$NON-NLS-1$
	/**
	 * The key to store whether the legacy code templates have been migrated 
	 * @since 3.0
	 */
	private static final String CODE_TEMPLATES_MIGRATION_KEY= "org.eclipse.jdt.ui.text.code_templates_migrated"; //$NON-NLS-1$
	
	private static JavaPlugin fgJavaPlugin;
	
	/** 
	 * The template context type registry for the java editor. 
	 * @since 3.0
	 */
	private ContextTypeRegistry fContextTypeRegistry;
	/** 
	 * The code template context type registry for the java editor. 
	 * @since 3.0
	 */
	private ContextTypeRegistry fCodeTemplateContextTypeRegistry;
	
	/**
	 * The template store for the java editor. 
	 * @since 3.0
	 */
	private TemplateStore fTemplateStore;
	/**
	 * The coded template store for the java editor. 
	 * @since 3.0
	 */
	private TemplateStore fCodeTemplateStore;


	private IWorkingCopyManager fWorkingCopyManager;
	private IBufferFactory fBufferFactory;
	private ICompilationUnitDocumentProvider fCompilationUnitDocumentProvider;
	private ClassFileDocumentProvider fClassFileDocumentProvider;
	private JavaTextTools fJavaTextTools;
	private ProblemMarkerManager fProblemMarkerManager;
	private ImageDescriptorRegistry fImageDescriptorRegistry;
	
	private JavaElementAdapterFactory fJavaElementAdapterFactory;
	private MarkerAdapterFactory fMarkerAdapterFactory;
	private EditorInputAdapterFactory fEditorInputAdapterFactory;
	private ResourceAdapterFactory fResourceAdapterFactory; 
	private LogicalPackageAdapterFactory fLogicalPackageAdapterFactory;
	
	private MembersOrderPreferenceCache fMembersOrderPreferenceCache;
	private IPropertyChangeListener fFontPropertyChangeListener;
	
	/**
	 * Property change listener on this plugin's preference store.
	 * 
	 * @since 3.0
	 */
	private IPropertyChangeListener fPropertyChangeListener;
	
	private JavaEditorTextHoverDescriptor[] fJavaEditorTextHoverDescriptors;
	
	/**
	 * Mockup preference store for firing events and registering listeners on project setting changes.
	 * FIXME: Temporary solution.
	 * 
	 * @since 3.0
	 */
	private MockupPreferenceStore fMockupPreferenceStore;
	
	/**
	 * The AST provider.
	 * @since 3.0
	 */
	private ASTProvider fASTProvider;
	
	/**
	 * The combined preference store.
	 * @since 3.0
	 */
	private IPreferenceStore fCombinedPreferenceStore;

	
	public static JavaPlugin getDefault() {
		return fgJavaPlugin;
	}
	
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	public static IWorkbenchPage getActivePage() {
		return getDefault().internalGetActivePage();
	}
	
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}
	
	public static Shell getActiveWorkbenchShell() {
		 IWorkbenchWindow window= getActiveWorkbenchWindow();
		 if (window != null) {
		 	return window.getShell();
		 }
		 return null;
	}
	
	/**
	 * Returns an array of all editors that have an unsaved content. If the identical content is 
	 * presented in more than one editor, only one of those editor parts is part of the result.
	 * 
	 * @return an array of all dirty editor parts.
	 */
	public static IEditorPart[] getDirtyEditors() {
		Set inputs= new HashSet();
		List result= new ArrayList(0);
		IWorkbench workbench= getDefault().getWorkbench();
		IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
		for (int i= 0; i < windows.length; i++) {
			IWorkbenchPage[] pages= windows[i].getPages();
			for (int x= 0; x < pages.length; x++) {
				IEditorPart[] editors= pages[x].getDirtyEditors();
				for (int z= 0; z < editors.length; z++) {
					IEditorPart ep= editors[z];
					IEditorInput input= ep.getEditorInput();
					if (!inputs.contains(input)) {
						inputs.add(input);
						result.add(ep);
					}
				}
			}
		}
		return (IEditorPart[])result.toArray(new IEditorPart[result.size()]);
	}
	
	/**
	 * Returns an array of all instanciated editors. 
	 */
	public static IEditorPart[] getInstanciatedEditors() {
		List result= new ArrayList(0);
		IWorkbench workbench= getDefault().getWorkbench();
		IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
		for (int windowIndex= 0; windowIndex < windows.length; windowIndex++) {
			IWorkbenchPage[] pages= windows[windowIndex].getPages();
			for (int pageIndex= 0; pageIndex < pages.length; pageIndex++) {
				IEditorReference[] references= pages[pageIndex].getEditorReferences();
				for (int refIndex= 0; refIndex < references.length; refIndex++) {
					IEditorPart editor= references[refIndex].getEditor(false);
					if (editor != null)
						result.add(editor);
				}
			}
		}
		return (IEditorPart[])result.toArray(new IEditorPart[result.size()]);
	}
	
	public static String getPluginId() {
		return getDefault().getDescriptor().getUniqueIdentifier();
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getPluginId(), IJavaStatusConstants.INTERNAL_ERROR, message, null));
	}

	public static void logErrorStatus(String message, IStatus status) {
		if (status == null) {
			logErrorMessage(message);
			return;
		}
		MultiStatus multi= new MultiStatus(getPluginId(), IJavaStatusConstants.INTERNAL_ERROR, message, null);
		multi.add(status);
		log(multi);
	}
	
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getPluginId(), IJavaStatusConstants.INTERNAL_ERROR, JavaUIMessages.getString("JavaPlugin.internal_error"), e)); //$NON-NLS-1$
	}
	
	public static boolean isDebug() {
		return getDefault().isDebugging();
	}
		
	public static ImageDescriptorRegistry getImageDescriptorRegistry() {
		return getDefault().internalGetImageDescriptorRegistry();
	}
	
	public JavaPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgJavaPlugin= this;
	}

	/* (non - Javadoc)
	 * Method declared in Plugin
	 */
	public void startup() throws CoreException {
		super.startup();
		registerAdapters();
		
		if (USE_WORKING_COPY_OWNERS) {
			WorkingCopyOwner.setPrimaryBufferProvider(new WorkingCopyOwner() {
				public IBuffer createBuffer(ICompilationUnit workingCopy) {
					ICompilationUnit original= workingCopy.getPrimary();
					IResource resource= original.getResource();
					if (resource instanceof IFile)
						return new DocumentAdapter(workingCopy, (IFile) resource);
					return DocumentAdapter.NULL;
				}
			});
		}
	
		installPreferenceStoreBackwardsCompatibility();
		
		AllTypesCache.initialize();
		
		// Initialize AST provider
		getASTProvider();
	}
	
	/**
	 * Installs backwards compatibility for the preference store.
	 */
	private void installPreferenceStoreBackwardsCompatibility() {

		/*
		 * Installs backwards compatibility: propagate the Java editor font from a
		 * pre-2.1 plug-in to the Platform UI's preference store to preserve
		 * the Java editor font from a pre-2.1 workspace. This is done only
		 * once.
		 */
		String fontPropagatedKey= "fontPropagated"; //$NON-NLS-1$
		if (getPreferenceStore().contains(JFaceResources.TEXT_FONT) && !getPreferenceStore().isDefault(JFaceResources.TEXT_FONT)) {
			if (!getPreferenceStore().getBoolean(fontPropagatedKey))
				PreferenceConverter.setValue(PlatformUI.getWorkbench().getPreferenceStore(), PreferenceConstants.EDITOR_TEXT_FONT, PreferenceConverter.getFontDataArray(getPreferenceStore(), JFaceResources.TEXT_FONT));
		}
		getPreferenceStore().setValue(fontPropagatedKey, true);

		/*
		 * Backwards compatibility: set the Java editor font in this plug-in's
		 * preference store to let older versions access it. Since 2.1 the
		 * Java editor font is managed by the workbench font preference page.
		 */
		PreferenceConverter.putValue(getPreferenceStore(), JFaceResources.TEXT_FONT, JFaceResources.getFontRegistry().getFontData(PreferenceConstants.EDITOR_TEXT_FONT));

		fFontPropertyChangeListener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (PreferenceConstants.EDITOR_TEXT_FONT.equals(event.getProperty()))
					PreferenceConverter.putValue(getPreferenceStore(), JFaceResources.TEXT_FONT, JFaceResources.getFontRegistry().getFontData(PreferenceConstants.EDITOR_TEXT_FONT));
			}
		};
		JFaceResources.getFontRegistry().addListener(fFontPropertyChangeListener);
		
		/*
		 * Backwards compatibility: propagate the Java editor tab width from a
		 * pre-3.0 plug-in to the new preference key. This is done only once.
		 */
		final String oldTabWidthKey= PreferenceConstants.EDITOR_TAB_WIDTH;
		final String newTabWidthKey= AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH;
		String tabWidthPropagatedKey= "tabWidthPropagated"; //$NON-NLS-1$
		if (getPreferenceStore().contains(oldTabWidthKey) && !getPreferenceStore().isDefault(oldTabWidthKey)) {
			if (!getPreferenceStore().getBoolean(tabWidthPropagatedKey))
				getPreferenceStore().setValue(newTabWidthKey, getPreferenceStore().getInt(oldTabWidthKey));
		}
		getPreferenceStore().setValue(tabWidthPropagatedKey, true);

		/*
		 * Backwards compatibility: set the Java editor tab width in this plug-in's
		 * preference store with the old key to let older versions access it.
		 * Since 3.0 the tab width is managed by the extended texteditor and
		 * uses a new key.
		 */
		getPreferenceStore().putValue(oldTabWidthKey, getPreferenceStore().getString(newTabWidthKey));

		fPropertyChangeListener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (newTabWidthKey.equals(event.getProperty()))
					getPreferenceStore().putValue(oldTabWidthKey, getPreferenceStore().getString(newTabWidthKey));
			}
		};
		getPreferenceStore().addPropertyChangeListener(fPropertyChangeListener);
		
		/*
		 * Backward compatibility for the refactoring preference key. 
		 */
		getPreferenceStore().setValue(
			PreferenceConstants.REFACTOR_ERROR_PAGE_SEVERITY_THRESHOLD, 
			RefactoringCore.getConditionCheckingFailedSeverity());
	}
	
	/**
	 * Uninstalls backwards compatibility for the preference store.
	 */
	private void uninstallPreferenceStoreBackwardsCompatibility() {
		JFaceResources.getFontRegistry().removeListener(fFontPropertyChangeListener);
		getPreferenceStore().removePropertyChangeListener(fPropertyChangeListener);
	}
	
	/*
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#createImageRegistry()
	 */
	protected ImageRegistry createImageRegistry() {
		return JavaPluginImages.getImageRegistry();
	}

	/*
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		
		AllTypesCache.terminate();

		if (fImageDescriptorRegistry != null)
			fImageDescriptorRegistry.dispose();
		
		unregisterAdapters();

		if (fASTProvider != null) {
			fASTProvider.dispose();
			fASTProvider= null;
		}
		
		if (fWorkingCopyManager != null) {
			fWorkingCopyManager.shutdown();
			fWorkingCopyManager= null;
		}
		
		if (fCompilationUnitDocumentProvider != null) {
			fCompilationUnitDocumentProvider.shutdown();
			fCompilationUnitDocumentProvider= null;
		}
				
		if (fJavaTextTools != null) {
			fJavaTextTools.dispose();
			fJavaTextTools= null;
		}
		
		JavaDocLocations.shutdownJavadocLocations();
		
		uninstallPreferenceStoreBackwardsCompatibility();
		
		RefactoringCore.getUndoManager().shutdown();
		
		super.shutdown();
	}
		
	private IWorkbenchPage internalGetActivePage() {
		IWorkbenchWindow window= getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return null;
		return getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}
	
	public synchronized IBufferFactory getBufferFactory() {
		if (fBufferFactory == null)
			fBufferFactory= new CustomBufferFactory();
		return fBufferFactory;
	}
	
	public synchronized ICompilationUnitDocumentProvider getCompilationUnitDocumentProvider() {
		if (fCompilationUnitDocumentProvider == null)
			fCompilationUnitDocumentProvider= new CompilationUnitDocumentProvider();
		return fCompilationUnitDocumentProvider;
	}
	
	public synchronized ClassFileDocumentProvider getClassFileDocumentProvider() {
		if (fClassFileDocumentProvider == null)
			fClassFileDocumentProvider= new ClassFileDocumentProvider();
		return fClassFileDocumentProvider;
	}

	public synchronized IWorkingCopyManager getWorkingCopyManager() {
		if (fWorkingCopyManager == null) {
			ICompilationUnitDocumentProvider provider= getCompilationUnitDocumentProvider();
			fWorkingCopyManager= new WorkingCopyManager(provider);
		}
		return fWorkingCopyManager;
	}
	
	/** 
	 * Returns the mockup preference store for firing events and registering listeners on project setting changes. Temporary solution.
	 */
	public MockupPreferenceStore getMockupPreferenceStore() {
		if (fMockupPreferenceStore == null)
			fMockupPreferenceStore= new MockupPreferenceStore();
		
		return fMockupPreferenceStore;
	}
	
	public synchronized ProblemMarkerManager getProblemMarkerManager() {
		if (fProblemMarkerManager == null)
			fProblemMarkerManager= new ProblemMarkerManager();
		return fProblemMarkerManager;
	}	
	
	public synchronized JavaTextTools getJavaTextTools() {
		if (fJavaTextTools == null)
			fJavaTextTools= new JavaTextTools(getPreferenceStore(), JavaCore.getPlugin().getPluginPreferences());
		return fJavaTextTools;
	}
	
	/**
	 * Returns the AST provider.
	 * 
	 * @since 3.0
	 */
	public synchronized ASTProvider getASTProvider() {
		if (fASTProvider == null)
			fASTProvider= new ASTProvider();
		
		return fASTProvider;
	}
		
	public synchronized MembersOrderPreferenceCache getMemberOrderPreferenceCache() {
		if (fMembersOrderPreferenceCache == null)
			fMembersOrderPreferenceCache= new MembersOrderPreferenceCache();
		return fMembersOrderPreferenceCache;
	}	

	/**
	 * Returns all Java editor text hovers contributed to the workbench.
	 * 
	 * @return an array of JavaEditorTextHoverDescriptor
	 * @since 2.1
	 */
	public JavaEditorTextHoverDescriptor[] getJavaEditorTextHoverDescriptors() {
		if (fJavaEditorTextHoverDescriptors == null) {
			fJavaEditorTextHoverDescriptors= JavaEditorTextHoverDescriptor.getContributedHovers();
			ConfigurationElementSorter sorter= new ConfigurationElementSorter() {
				/**
				 * {@inheritDoc}
				 */
				public IConfigurationElement getConfigurationElement(Object object) {
					return ((JavaEditorTextHoverDescriptor)object).getConfigurationElement();
				}
			};
			sorter.sort(fJavaEditorTextHoverDescriptors);
		}
		
		return fJavaEditorTextHoverDescriptors;
	} 

	/**
	 * Resets the Java editor text hovers contributed to the workbench.
	 * <p>
	 * This will force a rebuild of the descriptors the next time
	 * a client asks for them.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public void resetJavaEditorTextHoverDescriptors() {
		fJavaEditorTextHoverDescriptors= null;
	}

	/**
	 * Creates the Java plugin standard groups in a context menu.
	 */
	public static void createStandardGroups(IMenuManager menu) {
		if (!menu.isEmpty())
			return;
			
		menu.add(new Separator(IContextMenuConstants.GROUP_NEW));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GOTO));
		menu.add(new Separator(IContextMenuConstants.GROUP_OPEN));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_SHOW));
		menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
		menu.add(new Separator(IContextMenuConstants.GROUP_GENERATE));
		menu.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
		menu.add(new Separator(IContextMenuConstants.GROUP_BUILD));
		menu.add(new Separator(IContextMenuConstants.GROUP_ADDITIONS));
		menu.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
		menu.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));
	}

	/**
	 * Returns the template context type registry for the java plugin.
	 * 
	 * @return the template context type registry for the java plugin
	 * @since 3.0
	 */
	public ContextTypeRegistry getTemplateContextRegistry() {
		if (fContextTypeRegistry == null) {
			fContextTypeRegistry= new ContextTypeRegistry();
			
			fContextTypeRegistry.addContextType(new JavaContextType());
			fContextTypeRegistry.addContextType(new JavaDocContextType());
		}

		return fContextTypeRegistry;
	}
	
	/**
	 * Returns the template store for the java editor templates.
	 * 
	 * @return the template store for the java editor templates
	 * @since 3.0
	 */
	public TemplateStore getTemplateStore() {
		if (fTemplateStore == null) {
			boolean alreadyMigrated= getPreferenceStore().getBoolean(TEMPLATES_MIGRATION_KEY);
			if (alreadyMigrated)
				fTemplateStore= new TemplateStore(getTemplateContextRegistry(), getPreferenceStore(), TEMPLATES_KEY);
			else {
				fTemplateStore= new CompatibilityTemplateStore(getTemplateContextRegistry(), getPreferenceStore(), TEMPLATES_KEY, Templates.getInstance());
				getPreferenceStore().setValue(TEMPLATES_MIGRATION_KEY, true);
			}

			try {
				fTemplateStore.load();
			} catch (IOException e) {
				log(e);
			}
		}
		
		return fTemplateStore;
	}

	/**
	 * Returns the template context type registry for the code generation
	 * templates.
	 * 
	 * @return the template context type registry for the code generation
	 *         templates
	 * @since 3.0
	 */
	public ContextTypeRegistry getCodeTemplateContextRegistry() {
		if (fCodeTemplateContextTypeRegistry == null) {
			fCodeTemplateContextTypeRegistry= new ContextTypeRegistry();
			
			CodeTemplateContextType.registerContextTypes(fCodeTemplateContextTypeRegistry);
		}

		return fCodeTemplateContextTypeRegistry;
	}
	
	/**
	 * Returns the template store for the code generation templates.
	 * 
	 * @return the template store for the code generation templates
	 * @since 3.0
	 */
	public TemplateStore getCodeTemplateStore() {
		if (fCodeTemplateStore == null) {
			boolean alreadyMigrated= getPreferenceStore().getBoolean(CODE_TEMPLATES_MIGRATION_KEY);
			if (alreadyMigrated)
				fCodeTemplateStore= new TemplateStore(getCodeTemplateContextRegistry(), getPreferenceStore(), CODE_TEMPLATES_KEY);
			else {
				fCodeTemplateStore= new CompatibilityTemplateStore(getCodeTemplateContextRegistry(), getPreferenceStore(), CODE_TEMPLATES_KEY, CodeTemplates.getInstance());
				getPreferenceStore().setValue(CODE_TEMPLATES_MIGRATION_KEY, true);
			}

			try {
				fCodeTemplateStore.load();
			} catch (IOException e) {
				log(e);
			}
			
			// compatibility / bug fixing code for duplicated templates
			// TODO remove for 3.0
			CompatibilityTemplateStore.pruneDuplicates(fCodeTemplateStore, true);
		}
		
		return fCodeTemplateStore;
	}
	
	/**
	 * @see AbstractUIPlugin#initializeDefaultPreferences(org.eclipse.jface.preference.IPreferenceStore)
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		super.initializeDefaultPreferences(store);
		MarkerAnnotationPreferences.initializeDefaultValues(store);		
		PreferenceConstants.initializeDefaultValues(store);
	}
	
	private synchronized ImageDescriptorRegistry internalGetImageDescriptorRegistry() {
		if (fImageDescriptorRegistry == null)
			fImageDescriptorRegistry= new ImageDescriptorRegistry();
		return fImageDescriptorRegistry;
	}

	private void registerAdapters() {
		fJavaElementAdapterFactory= new JavaElementAdapterFactory();
		fMarkerAdapterFactory= new MarkerAdapterFactory();
		fEditorInputAdapterFactory= new EditorInputAdapterFactory();
		fResourceAdapterFactory= new ResourceAdapterFactory();
		fLogicalPackageAdapterFactory= new LogicalPackageAdapterFactory();

		IAdapterManager manager= Platform.getAdapterManager();		
		manager.registerAdapters(fJavaElementAdapterFactory, IJavaElement.class);
		manager.registerAdapters(fMarkerAdapterFactory, IMarker.class);
		manager.registerAdapters(fEditorInputAdapterFactory, IEditorInput.class);
		manager.registerAdapters(fResourceAdapterFactory, IResource.class);
		manager.registerAdapters(fLogicalPackageAdapterFactory, LogicalPackage.class);
	}
	
	private void unregisterAdapters() {
		IAdapterManager manager= Platform.getAdapterManager();
		manager.unregisterAdapters(fJavaElementAdapterFactory);
		manager.unregisterAdapters(fMarkerAdapterFactory);
		manager.unregisterAdapters(fEditorInputAdapterFactory);
		manager.unregisterAdapters(fResourceAdapterFactory);
		manager.unregisterAdapters(fLogicalPackageAdapterFactory);
	}
	
	/**
	 * Returns a combined preference store, this store is read-only.
	 * 
	 * @return the combined preference store
	 * 
	 * @since 3.0
	 */
	public IPreferenceStore getCombinedPreferenceStore() {
		if (fCombinedPreferenceStore == null)
			fCombinedPreferenceStore= new ChainedPreferenceStore(new IPreferenceStore[] { getPreferenceStore(), new PreferencesAdapter(JavaCore.getPlugin().getPluginPreferences()) });
		return fCombinedPreferenceStore;
	}
}
