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

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.progress.UIJob;
import org.jboss.tools.fuse.transformation.editor.Activator;
import org.jboss.tools.fuse.transformation.editor.wizards.ReloadTransformationWizard;

/**
 * @author brianf
 *
 */
public class ReloadTypePage extends XformWizardPage {

    private Composite _page;
    private ComboViewer _sourceCV;
    
    /**
     * @param model
     */
    public ReloadTypePage(final Model model) {
        super("Reload Type", "Reload Type", Activator.imageDescriptor("transform.png"), model);
        observablesManager.addObservablesFromContext(context, true, true);
    }

    @Override
    public void createControl(final Composite parent) {
        setDescription("Supply the type for the model to reload.");
        observablesManager.runAndCollect(new Runnable() {

            @Override
            public void run() {
                createPage(parent);
            }
        });

        WizardPageSupport.create(this, context);
        setErrorMessage(null); // clear any error messages at first
    }

    private void createPage(Composite parent) {
        _page = new Composite(parent, SWT.NONE);
        setControl(_page);
        GridLayout layout = new GridLayout(3, false);
        layout.marginRight = 5;
        layout.horizontalSpacing = 10;
        _page.setLayout(layout);

        Group group = new Group(_page, SWT.SHADOW_ETCHED_IN);
        group.setText("Type Transformed");
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 2));

        createLabel(group, "Source Type", "Data type for the source being transformed.");

        _sourceCV = new ComboViewer(new Combo(group, SWT.READ_ONLY));
        GridData sourceGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        sourceGD.horizontalIndent = 5;
        _sourceCV.getCombo().setLayoutData(sourceGD);

        bindControls();
        initialize();
        validatePage();
    }

    private void initialize() {

        for (final Object observable : context.getValidationStatusProviders()) {
            ((Binding) observable).getTarget().addChangeListener(new IChangeListener() {

                @Override
                public void handleChange(final ChangeEvent event) {
                    validatePage();
                }
            });
        }
    }

    private void bindControls() {

        // bind the source type string dropdown
        _sourceCV.setContentProvider(new ObservableListContentProvider());
        IObservableValue widgetValue = ViewerProperties.singleSelection().observe(_sourceCV);
        IObservableValue modelValue = BeanProperties.value(Model.class, "sourceTypeStr").observe(model);
        UpdateValueStrategy strategy = new UpdateValueStrategy();
        strategy.setBeforeSetValidator(new IValidator() {

            @Override
            public IStatus validate(final Object value) {
                getModel().setSourceFilePath(new String());
                ((ReloadTransformationWizard) getWizard()).resetSourceAndTargetPages();
                if (ReloadTypePage.this.getSourcePage() != null) {
                    ((XformWizardPage) ReloadTypePage.this.getSourcePage()).clearControls();
                }
                UIJob uiJob = new UIJob("open error") {
                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        if (ReloadTypePage.this.getTargetPage() != null) {
                            ((XformWizardPage) ReloadTypePage.this.getTargetPage()).pingBinding();
                        }
                        return Status.OK_STATUS;
                    }
                };
                uiJob.setSystem(true);
                uiJob.schedule();

                if (value == null || ((String) value).trim().isEmpty()) {
                    resetFinish();
                    return ValidationStatus.error("A source type must be selected");
                }
                return ValidationStatus.ok();
            }
        });

        WritableList sourceList = new WritableList();
        sourceList.add("XML");
        sourceList.add("JSON");
        sourceList.add("");
        _sourceCV.setInput(sourceList);
        ControlDecorationSupport.create(context.bindValue(widgetValue, modelValue, strategy, null), decoratorPosition,
                null);

        listenForValidationChanges();
    }

    @Override
    public void notifyListeners() {
        // empty
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            notifyListeners();
        }
    }

    public IWizardPage getSourcePage() {
        if (model.getSourceTypeStr() != null && !model.getSourceTypeStr().trim().isEmpty()) {
            if (model.getSourceTypeStr().equalsIgnoreCase("xml")) {
                ReloadTransformationWizard wizard = (ReloadTransformationWizard) getWizard();
                return wizard.xmlSource;
            } else if (model.getSourceTypeStr().equalsIgnoreCase("json")) {
                ReloadTransformationWizard wizard = (ReloadTransformationWizard) getWizard();
                return wizard.jsonSource;
            }

        }
        return null;
    }
}
