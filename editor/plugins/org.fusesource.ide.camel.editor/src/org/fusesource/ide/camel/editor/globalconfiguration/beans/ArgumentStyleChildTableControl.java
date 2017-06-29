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
package org.fusesource.ide.camel.editor.globalconfiguration.beans;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeColumn;
import org.fusesource.ide.camel.editor.internal.UIMessages;
import org.fusesource.ide.camel.model.service.core.model.AbstractCamelModelElement;
import org.fusesource.ide.camel.model.service.core.model.CamelBasicModelElement;
import org.fusesource.ide.camel.model.service.core.model.CamelBean;
import org.fusesource.ide.camel.model.service.core.model.CamelFile;
import org.w3c.dom.Element;

/**
 * @author brianf
 *
 */
public class ArgumentStyleChildTableControl extends Composite {

	private static final String[] TREE_COLUMNS = new String[] { CamelBean.ARG_TYPE, CamelBean.ARG_VALUE };

	private Button addButton;
	private Button removeButton;
	private Button editButton;
	private boolean isReadOnly = false;
	private String warningMsg = null;
	private ListenerList<ChangeListener> changeListeners;
	private TreeViewer propertyTreeTable;
	private AbstractCamelModelElement inputElement;
	private List<AbstractCamelModelElement> argumentList = new ArrayList<>();
	private BeanConfigUtil beanConfigUtil = new BeanConfigUtil();

	public ArgumentStyleChildTableControl(Composite parent, int style) {
		this(parent, style, false);
	}

	public ArgumentStyleChildTableControl(Composite parent, int style, boolean isReadOnly) {
		super(parent, style);

		this.isReadOnly = isReadOnly;
		this.changeListeners = new ListenerList<>();

		int additionalStyles;
		if (isReadOnly) {
			additionalStyles = SWT.READ_ONLY;
		} else {
			additionalStyles = SWT.NONE;
		}
		setLayout(GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(2).create());

		propertyTreeTable = new TreeViewer(this,
				SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.FULL_SELECTION | style | additionalStyles);
		this.propertyTreeTable.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
		propertyTreeTable.getTree().setLayoutData(
				GridDataFactory.fillDefaults().span(1, 5).grab(true, true).hint(SWT.DEFAULT, 100).create());
		propertyTreeTable.getTree().setHeaderVisible(true);
		propertyTreeTable.getTree().setLinesVisible(true);
		TreeColumn typeColumn = new TreeColumn(propertyTreeTable.getTree(), SWT.LEFT);
		typeColumn.setText(UIMessages.argumentStyleChildTableControlTypeColumnLabel);
		typeColumn.setWidth(200);
		TreeColumn valueColumn = new TreeColumn(propertyTreeTable.getTree(), SWT.LEFT);
		valueColumn.setText(UIMessages.argumentStyleChildTableControlValueColumnLabel);
		valueColumn.setWidth(200);

		propertyTreeTable.setColumnProperties(TREE_COLUMNS);

		propertyTreeTable.setLabelProvider(new ArgumentTypeTreeLabelProvider());

		propertyTreeTable.setContentProvider(new ArgumentTypeTreeContentProvider());

		propertyTreeTable.setCellEditors(new CellEditor[] { new TextCellEditor(propertyTreeTable.getTree()),
				new TextCellEditor(propertyTreeTable.getTree()), null });

		this.addButton = new Button(this, SWT.NONE);
		this.addButton.setLayoutData(GridDataFactory.fillDefaults().create());
		this.addButton.setText(UIMessages.argumentStyleChildTableControlAddButtonLabel);
		this.addButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				addArgumentTypeToList();
				propertyTreeTable.refresh();
				updateArgumentTypeButtons();
				fireChangedEvent(e.getSource());
			}
		});

		this.addButton.setEnabled(false);

		this.editButton = new Button(this, SWT.NONE);
		this.editButton.setLayoutData(GridDataFactory.fillDefaults().create());
		this.editButton.setText(UIMessages.argumentStyleChildTableControlEditButtonLabel);
		this.editButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				editArgumentType();
				propertyTreeTable.refresh();
				updateArgumentTypeButtons();
				fireChangedEvent(e.getSource());
			}
		});

		this.editButton.setEnabled(false);

		propertyTreeTable.addDoubleClickListener(e -> {
			editArgumentType();
			propertyTreeTable.refresh();
			updateArgumentTypeButtons();
			fireChangedEvent(e.getSource());
		});

		propertyTreeTable.getTree().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateArgumentTypeButtons();
			}
		});

		this.removeButton = new Button(this, SWT.NONE);
		this.removeButton.setLayoutData(GridDataFactory.fillDefaults().create());
		this.removeButton.setText(UIMessages.argumentStyleChildTableControlRemoveButtonLabel);
		this.removeButton.setEnabled(false);
		this.removeButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				removeArgumentFromList();
				propertyTreeTable.refresh();
				updateArgumentTypeButtons();
				fireChangedEvent(e.getSource());
			}
		});

		propertyTreeTable.setInput(argumentList);
		updateArgumentTypeButtons();
	}

	/**
	 * If we changed, fire a changed event.
	 * 
	 * @param source
	 */
	protected void fireChangedEvent(Object source) {
		ChangeEvent e = new ChangeEvent(source);
		// inform any listeners of the resize event
		Object[] listeners = this.changeListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i) {
			((ChangeListener) listeners[i]).stateChanged(e);
		}
	}

	/**
	 * Add a change listener.
	 * 
	 * @param listener
	 *            new listener
	 */
	public void addChangeListener(ChangeListener listener) {
		this.changeListeners.add(listener);
	}

	/**
	 * Remove a change listener.
	 * 
	 * @param listener
	 *            old listener
	 */
	public void removeChangeListener(ChangeListener listener) {
		this.changeListeners.remove(listener);
	}

	/**
	 * Update button state based on what's selected.
	 */
	public void updateArgumentTypeButtons() {
		if (isReadOnly) {
			this.addButton.setEnabled(false);
			this.editButton.setEnabled(false);
			this.removeButton.setEnabled(false);

		} else {
			this.addButton.setEnabled(true);

			// enable if a selection is made
			boolean enable = getStructuredSelection() != null && !getStructuredSelection().isEmpty();
			this.editButton.setEnabled(enable);
			this.removeButton.setEnabled(enable);
		}
	}

	/**
	 * @return the current selection from the table
	 */
	public IStructuredSelection getStructuredSelection() {
		if (propertyTreeTable != null && !propertyTreeTable.getSelection().isEmpty()) {
			return (IStructuredSelection) propertyTreeTable.getSelection();
		}
		return null;
	}

	/**
	 * @return warning string
	 */
	public String getWarning() {
		return this.warningMsg;
	}

	protected void removeArgumentFromList() {
		if (!getStructuredSelection().isEmpty()) {
			AbstractCamelModelElement selectedProperty = (AbstractCamelModelElement) getStructuredSelection()
					.getFirstElement();
			argumentList.remove(selectedProperty);
		}
	}

	protected void addArgumentTypeToList() {
		final ArgumentInputDialog dialog = new ArgumentInputDialog(Display.getCurrent().getActiveShell());
		int rtnValue = dialog.open();
		if (rtnValue == PropertyInputDialog.OK) {
			final String type = dialog.getArgumentType();
			final String value = dialog.getArgumentValue();
			addBeanArgument(type, value);
		}
	}

	protected void editArgumentType() {
		if (!getStructuredSelection().isEmpty()) {
			AbstractCamelModelElement selectedProperty = (AbstractCamelModelElement) getStructuredSelection()
					.getFirstElement();

			final ArgumentInputDialog dialog = new ArgumentInputDialog(Display.getCurrent().getActiveShell());
			final Element xmlElement = (Element) selectedProperty.getXmlNode();
			if (xmlElement.getAttribute(CamelBean.ARG_TYPE) != null) {
				dialog.setArgumentType(xmlElement.getAttribute(CamelBean.ARG_TYPE));
			}
			if (xmlElement.getAttribute(CamelBean.ARG_VALUE) != null) {
				dialog.setArgumentValue(xmlElement.getAttribute(CamelBean.ARG_VALUE));
			}
			int rtnValue = dialog.open();
			if (rtnValue == PropertyInputDialog.OK) {
				final String type = dialog.getArgumentType();
				final String value = dialog.getArgumentValue();
				beanConfigUtil.editBeanArgument(xmlElement, type, value);
			}
		}
	}

	public void setInput(AbstractCamelModelElement input) {
		inputElement = input;
	}

	private void addBeanArgument(String type, String value) {
		final CamelFile camelFile = inputElement.getCamelFile();
		Element propertyNode = beanConfigUtil.createBeanArgument(camelFile, type, value);
		CamelBasicModelElement newProperty = new CamelBasicModelElement(null, propertyNode);
		argumentList.add(newProperty);
	}

	private class ArgumentTypeTreeContentProvider implements ITreeContentProvider {
		private List<AbstractCamelModelElement> properties;

		@SuppressWarnings("unchecked")
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof List<?>) {
				properties = (List<AbstractCamelModelElement>) newInput;
			}
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof List<?>) {
				return properties.toArray();
			}
			return new Object[0];
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof AbstractCamelModelElement) {
				AbstractCamelModelElement parent = (AbstractCamelModelElement) parentElement;
				return new Object[] { parent.getChildElements() };
			}
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof AbstractCamelModelElement) {
				return ((AbstractCamelModelElement) element).getParent();
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof List<?>) {
				return !((List<?>) element).isEmpty();
			}
			return false;
		}
	}

	private class ArgumentTypeTreeLabelProvider implements ITableLabelProvider {
		@Override
		public void addListener(ILabelProviderListener listener) {
			// empty
		}

		@Override
		public void dispose() {
			// empty
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return element instanceof AbstractCamelModelElement
					&& (property.equalsIgnoreCase(CamelBean.ARG_TYPE) || property.equalsIgnoreCase(CamelBean.ARG_VALUE));
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			// empty
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof AbstractCamelModelElement && columnIndex == 0) {
				Element xmlElement = (Element) ((AbstractCamelModelElement) element).getXmlNode();
				return xmlElement.getAttribute(CamelBean.ARG_TYPE);
			} else if (element instanceof AbstractCamelModelElement && columnIndex == 1) {
				Element xmlElement = (Element) ((AbstractCamelModelElement) element).getXmlNode();
				return xmlElement.getAttribute(CamelBean.ARG_VALUE);
			}
			return null;
		}
	}

	public List<AbstractCamelModelElement> getArgumentList() {
		return argumentList;
	}

	public void setArgumentList(List<AbstractCamelModelElement> list) {
		this.argumentList = list;
	}
}
