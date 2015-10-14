/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.fuse.transformation.editor.wizards;

import java.net.URLClassLoader;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.jboss.tools.fuse.transformation.editor.internal.util.JavaUtil;
import org.jboss.tools.fuse.transformation.editor.internal.util.TransformationManager;
import org.jboss.tools.fuse.transformation.editor.internal.wizards.CompareModelPage;
import org.jboss.tools.fuse.transformation.editor.internal.wizards.JSONPage;
import org.jboss.tools.fuse.transformation.editor.internal.wizards.Model;
import org.jboss.tools.fuse.transformation.editor.internal.wizards.ModelPage;
import org.jboss.tools.fuse.transformation.editor.internal.wizards.ReloadTypePage;
import org.jboss.tools.fuse.transformation.editor.internal.wizards.XMLPage;
import org.jboss.tools.fuse.transformation.editor.internal.wizards.XformWizardPage;

/**
 * @author brianf
 *
 */
public class ReloadTransformationWizard extends Wizard {

    private Model uiModel = new Model();
    private org.jboss.tools.fuse.transformation.model.Model genModel = null;
    public ModelPage javaModelPage;
    public URLClassLoader loader;
    private final TransformationManager manager;
    private ReloadTypePage reloadTypePage;
    public XMLPage xmlSource;
    public JSONPage jsonSource;
    private CompareModelPage reloadFinalPage;

    public ReloadTransformationWizard(final TransformationManager manager) {
    	this.manager = manager;
    }
    
    public void setModel(org.jboss.tools.fuse.transformation.model.Model inModel) {
    	genModel = inModel;
        uiModel.setProject(manager.project());
    }
    
    @Override
    public boolean performFinish() {
    	// empty for now
    	return true;
    }

    @Override
    public void addPages() {
    	if (genModel != null && manager != null) {
    		boolean isSource = manager.source(genModel);
    		
    		String modelType = null;
    		if (isSource) {
    		    modelType = manager.rootSourceModel().getType();
    		} else {
    		    modelType = manager.rootTargetModel().getType();
    		}
    		System.out.println("modelType = " + modelType);
    		
            if (javaModelPage == null) {
                javaModelPage = new ModelPage("ModelPage", uiModel, true, manager);
                javaModelPage.setJavaModel(genModel);
            }
            addPage(javaModelPage);
            
            if (reloadTypePage == null) {
                reloadTypePage = new ReloadTypePage(uiModel);
            }
            addPage(reloadTypePage);
            
            if (xmlSource == null) {
                xmlSource = new XMLPage("SourceXml", uiModel, true);
            }
            addPage(xmlSource);
            if (jsonSource == null) {
                jsonSource = new JSONPage("Sourcejson", uiModel, true);
            }
            addPage(jsonSource);

            if (reloadFinalPage == null) {
                reloadFinalPage = new CompareModelPage("FinalModelPage", uiModel, true, manager);
                reloadFinalPage.setTitle("Updated Model");
                reloadFinalPage.setDescription("Below is the modified model.");
            }
            addPage(reloadFinalPage);
    	
    	}
    }

    @Override
    public String getWindowTitle() {
        return "Regenerate Model";
    }

    public void setSelectedProject(IProject project) {
        uiModel.setProject(project);
        final IJavaProject javaProject = JavaCore.create(project);
        try {
            loader = (URLClassLoader) JavaUtil.getProjectClassLoader(javaProject, getClass().getClassLoader());
        } catch (final Exception e) {
            // eat exception
            e.printStackTrace();
        }
    }

    public URLClassLoader getLoader() {
        if (uiModel.getProject() == null && manager.project() != null) {
            uiModel.setProject(manager.project());
        }
        if (this.loader == null && uiModel.getProject() != null) {
            setSelectedProject(uiModel.getProject());
        }
        return this.loader;
    }

    public Model getModel() {
        return uiModel;
    }
    
    public WizardPage getFinalPage() {
        return reloadFinalPage;
    }

    @Override
    public boolean canFinish() {
        if (reloadTypePage != null && reloadTypePage.getSourcePage() != null) {
            ((XformWizardPage) reloadTypePage.getSourcePage()).notifyListeners();
            if (reloadTypePage.isPageComplete() && reloadTypePage.getSourcePage().isPageComplete()
                    && reloadFinalPage.isPageComplete()) {
                return true;
            }
        } else {
            return false;
        }
        return super.canFinish();
    }

    private void resetPage(IWizardPage page) {
        if (page != null && page instanceof XformWizardPage) {
            ((XformWizardPage) page).resetFinish();
        }
    }

    public void resetSourceAndTargetPages() {
        resetPage(xmlSource);
        resetPage(jsonSource);
    }
}
