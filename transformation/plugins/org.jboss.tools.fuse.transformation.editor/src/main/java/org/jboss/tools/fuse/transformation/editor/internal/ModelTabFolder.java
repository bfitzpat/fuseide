/******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: JBoss by Red Hat - Initial implementation.
 *****************************************************************************/
package org.jboss.tools.fuse.transformation.editor.internal;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.jboss.tools.fuse.transformation.editor.internal.util.TransformationManager;
import org.jboss.tools.fuse.transformation.editor.wizards.ReloadTransformationWizard;
import org.jboss.tools.fuse.transformation.model.Model;

abstract class ModelTabFolder extends CTabFolder {

    final TransformationManager manager;
    private final Model model;
    private final CTabItem modelTab;
    private final ModelViewer modelViewer;

    /**
     * @param manager
     * @param parent
     * @param title
     * @param model
     * @param potentialDropTargets
     */
    ModelTabFolder(final TransformationManager manager,
                   final Composite parent,
                   final String title,
                   final Model model,
                   final List<PotentialDropTarget> potentialDropTargets) {
        super(parent, SWT.BORDER);
        this.manager = manager;
        this.model = model;

        setBackground(parent.getDisplay()
                            .getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));

        final ToolBar toolBar = new ToolBar(this, SWT.RIGHT);
        setTopRight(toolBar);

        modelTab = new CTabItem(this, SWT.NONE);
        modelTab.setText(title + (model == null ? "" : ": " + model.getName()));
        modelViewer = constructModelViewer(potentialDropTargets, title);
        modelTab.setControl(modelViewer);
        modelViewer.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        modelViewer.layout();
        setSelection(modelTab);
        addMenuManager();
    }

    ModelViewer constructModelViewer(List<PotentialDropTarget> potentialDropTargets,
                                     String preferenceId) {
        return new ModelViewer(manager, this, model, potentialDropTargets, preferenceId);
    }

    /**
     * @param object
     */
    public void select(final Object object) {
        if (object instanceof Model) {
            setSelection(modelTab);
            modelViewer.select((Model)object);
        }
    }

    private void addMenuManager() {
    	final TreeViewer viewer = modelViewer.treeViewer;
    	Control control = viewer.getControl();
    	MenuManager menuMgr = new MenuManager();
    	Menu menu = menuMgr.createContextMenu(control);
    	menuMgr.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                // IWorkbench wb = PlatformUI.getWorkbench();
                // IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
                if (viewer.getSelection().isEmpty()) {
                    return;
                }

                if (viewer.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = 
                    		(IStructuredSelection) viewer.getSelection();
                    Model object = (Model)selection.getFirstElement();

                    if (object.getParent() == null) {
                    	manager.add(new MyAction());
                    }
                }
            }
        });
        menuMgr.setRemoveAllWhenShown(true);
        control.setMenu(menu);
    }
    
    class MyAction extends Action {

		@Override
		public void run() {
			Shell shell = Display.getCurrent().getActiveShell();
			boolean answer = 
					MessageDialog.openQuestion(shell, "Rebuild Model?", "Are you sure you want to rebuild this model? It may invalidate the transformation.");
			if (answer) {
				// open wizard
			    ReloadTransformationWizard wizard = new ReloadTransformationWizard(manager);
			    wizard.setModel(model);
				WizardDialog dialog = new WizardDialog(shell, wizard);
				int value = dialog.open();
			}
		}

		@Override
		public String getText() {
			return "Refresh Menu";
		}
    	
    }
}
