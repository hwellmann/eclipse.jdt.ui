/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.ui.text.template;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import org.eclipse.ui.IEditorPart;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.core.Assert;

public class TemplateEngine {

	private String fContextType;
	
	private ArrayList fProposals= new ArrayList();

	/**
	 * Creates the template engine for a particular context type.
	 * See <code>TemplateContext</code> for supported context types.
	 */
	public TemplateEngine(String contextType) {
		Assert.isNotNull(contextType);
		fContextType= new String(contextType);
	}

	/**
	 * Empties the collector.
	 * 
	 * @param viewer the text viewer  
	 * @param unit   the compilation unit (may be <code>null</code>)
	 */
	public void reset() {
		fProposals.clear();
	}

	/**
	 * Returns the array of matching templates.
	 */
	public ICompletionProposal[] getResults() {
		return (ICompletionProposal[]) fProposals.toArray(new ICompletionProposal[fProposals.size()]);
	}

	/**
	 * Inspects the context of the compilation unit around <code>completionPosition</code>
	 * and feeds the collector with proposals.
	 * @param viewer             the text viewer
	 * @param completionPosition the context position in the document of the text viewer
	 * @param unit               the compilation unit (may be <code>null</code>)
	 */
	public void complete(ITextViewer viewer, int completionPosition, ICompilationUnit sourceUnit)
		throws JavaModelException
	{
		Assert.isNotNull(viewer);

		// prohibit recursion
		if (TemplateEditor.isEditorActive(viewer))
			return;
		
		TemplateContext context= new TemplateContext(viewer, completionPosition, sourceUnit, fContextType);
		Template[] templates= TemplateSet.getInstance().getMatchingTemplates(context);

		for (int i= 0; i != templates.length; i++) {
			TemplateProposal proposal= new TemplateProposal(templates[i], context);
			fProposals.add(proposal);
		}
	}
	
}

