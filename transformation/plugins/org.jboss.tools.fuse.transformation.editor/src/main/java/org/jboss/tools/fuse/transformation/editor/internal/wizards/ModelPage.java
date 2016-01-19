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
package org.jboss.tools.fuse.transformation.editor.internal.wizards;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.fuse.transformation.editor.Activator;
import org.jboss.tools.fuse.transformation.editor.internal.ModelViewer;
import org.jboss.tools.fuse.transformation.editor.internal.util.TransformationManager;

/**
 * @author brianf
 *
 */
public class ModelPage extends XformWizardPage {

    private Composite _page;
    private Text _javaClassText;
    private org.jboss.tools.fuse.transformation.core.model.Model _javaModel = null;
    private ModelViewer _modelViewer;
    private final TransformationManager manager;

    /**
     * @param pageName
     * @param model
     * @param isSource
     */
    public ModelPage(String pageName, final Model model, boolean isSource, TransformationManager manager) {
        super(pageName, model);
        this.manager = manager;
        setTitle("Model Page");
        setDescription("Below is the current model you want to reload.");
        setImageDescriptor(Activator.imageDescriptor("transform.png"));
        observablesManager.addObservablesFromContext(context, true, true);
    }

    @Override
    public void createControl(final Composite parent) {
        observablesManager.runAndCollect(new Runnable() {

            @Override
            public void run() {
                createPage(parent);
            }
        });
    }

    private void createPage(Composite parent) {
        _page = new Composite(parent, SWT.NONE);
        setControl(_page);
        GridLayout layout = new GridLayout(3, false);
        layout.marginRight = 5;
        layout.horizontalSpacing = 10;
        _page.setLayout(layout);

        // Create file path widgets
        Label label = 
                createLabel(_page, "Class:", "The source Java class for the model being regenerated.");

        _javaClassText = new Text(_page, SWT.BORDER);
        _javaClassText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        _javaClassText.setToolTipText(label.getToolTipText());
        _javaClassText.setEnabled(false);

        Group group = new Group(_page, SWT.SHADOW_ETCHED_IN);
        group.setText("Class Structure");
        group.setLayout(new GridLayout(3, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 3));

        _modelViewer = new ModelViewer(manager, group, _javaModel);
        _modelViewer.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        _modelViewer.layout();

        if (_javaModel != null) {
            updateViewer(_javaModel.getType());
        }
    }
    
    public void setJavaModel(org.jboss.tools.fuse.transformation.core.model.Model jModel) {
        _javaModel = jModel;
    }
    
    public void updateViewer(String className) {
        _javaClassText.setText(className);
        _modelViewer.setModel(_javaModel);
    }

}
