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
package org.fusesource.ide.camel.editor.globalconfiguration.beans.wizards.pages;

import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.fusesource.ide.camel.editor.globalconfiguration.beans.ArgumentStyleChildTableControl;
import org.fusesource.ide.camel.editor.globalconfiguration.beans.PropertyStyleChildTableControl;
import org.fusesource.ide.camel.editor.internal.UIMessages;
import org.fusesource.ide.camel.model.service.core.model.AbstractCamelModelElement;

/**
 * @author brianf
 *
 */
public class GlobalBeanWizardPage extends GlobalBeanBaseWizardPage {

	private ArgumentStyleChildTableControl beanArgsTable;
	private PropertyStyleChildTableControl beanPropsTable;

	/**
	 * @param pageName
	 */
	public GlobalBeanWizardPage(DataBindingContext dbc, String title, String description, AbstractCamelModelElement parent) {
		super(UIMessages.globalBeanWizardPageDefaultName);
		setTitle(title);
		setDescription(description);
		this.dbc = dbc;
		this.parent = parent;
		this.project = parent.getCamelFile().getResource().getProject();
	}

	public List<AbstractCamelModelElement> getPropertyList() {
		return beanPropsTable.getPropertyList();
	}

	public List<AbstractCamelModelElement> getArgumentsList() {
		return beanArgsTable.getArgumentList();
	}

	@Override
	protected void createArgumentsControls(Composite parent, int cols) {
		beanArgsTable = new ArgumentStyleChildTableControl(parent, SWT.NULL);
		beanArgsTable.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(cols, 2).create());
		beanArgsTable.setInput(this.parent);
	}

	@Override
	protected void createPropsControls(Composite parent, int cols) {
		beanPropsTable = new PropertyStyleChildTableControl(parent, SWT.NULL);
		beanPropsTable.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(cols, 2).create());
		beanPropsTable.setInput(this.parent);
	}

	@Override
	protected void createClassBinding(UpdateValueStrategy strategy) {
		classObservable = PojoProperties.value(GlobalBeanBaseWizardPage.class, "classname", String.class).observe(this); //$NON-NLS-1$
		Binding binding = dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(classText), classObservable, strategy, null);
		ControlDecorationSupport.create(binding, SWT.LEFT | SWT.TOP);
	}

	@Override
	protected void createIdBinding(UpdateValueStrategy strategy) {
		final IObservableValue<?> idObservable = PojoProperties.value(GlobalBeanBaseWizardPage.class, "id", String.class).observe(this); //$NON-NLS-1$
		Binding binding = dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(idText), idObservable, strategy, null);
		ControlDecorationSupport.create(binding, SWT.LEFT | SWT.TOP);
	}

}
