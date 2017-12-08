/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.fusesource.ide.wsdl2rest.ui.wizard.pages;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.fusesource.ide.foundation.core.util.Strings;
import org.fusesource.ide.wsdl2rest.ui.internal.UIMessages;

/**
 * @author brianf
 *
 */
public class Wsdl2RestWizardFirstPage extends Wsdl2RestWizardBasePage {

	private Text urlTextControl;

	/**
	 * Constructor
	 * @param pageName
	 * @param title
	 * @param titleImage
	 */
	public Wsdl2RestWizardFirstPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		setMessage(UIMessages.wsdl2RestWizardFirstPagePageOneDescription);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		WizardPageSupport.create(this, dbc);
		setDescription(UIMessages.wsdl2RestWizardFirstPagePageOneDescription);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.swtDefaults().numColumns(4).create());
		composite.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).create());
		urlTextControl = createLabelAndText(composite, UIMessages.wsdl2RestWizardFirstPageWSDLFileLabel, 2);
		Button urlBrowseBtn = createButton(composite, "..."); //$NON-NLS-1$
		urlBrowseBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				selectWSDL();
				urlTextControl.notifyListeners(SWT.Modify, new Event());
			}
		});

		Text projectTextControl = createLabelAndText(composite, UIMessages.wsdl2RestWizardFirstPageProjectLabel, 2);
		Button outPathBrowseButton = createButton(composite, "..."); //$NON-NLS-1$
		outPathBrowseButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IProject selectedProject = selectProject();
				if (selectedProject != null) {
					getOptionsFromWizard().setProjectName(selectedProject.getName());
					projectTextControl.notifyListeners(SWT.Modify, new Event());				
				}
			}
		});
		
		// define the data bindings
		Binding wsdlBinding = createBinding(urlTextControl, "wsdlURL", new WsdlValidator()); //$NON-NLS-1$
		ControlDecorationSupport.create(wsdlBinding, SWT.LEFT | SWT.TOP);

		Binding projectTextBinding = createBinding(projectTextControl, "projectName", new ProjectNameValidator()); //$NON-NLS-1$
		ControlDecorationSupport.create(projectTextBinding, SWT.LEFT | SWT.TOP);

		// set initial values
		if (!Strings.isEmpty(getOptionsFromWizard().getWsdlURL())) {
			urlTextControl.setText(getOptionsFromWizard().getWsdlURL());
		}
		if (!Strings.isEmpty(getOptionsFromWizard().getProjectName())) {
			projectTextControl.setText(getOptionsFromWizard().getProjectName());
		}

		setControl(composite);
        setPageComplete(isPageComplete());
        setErrorMessage(null); // clear any error messages at first
	}

}
