/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.fusesource.ide.camel.editor.restconfiguration.actions;

import org.eclipse.jface.resource.ImageRegistry;
import org.fusesource.ide.camel.editor.internal.UIMessages;
import org.fusesource.ide.camel.editor.restconfiguration.RestConfigConstants;
import org.fusesource.ide.camel.editor.restconfiguration.RestConfigEditor;

/**
 * @author lheinema
 */
public class AddRestConfigurationAction extends RestEditorAction {
	
	public AddRestConfigurationAction(RestConfigEditor restConfigEditor, ImageRegistry imageReg) {
		super(restConfigEditor, imageReg);
	}
	
	@Override
	public void run() {
		restConfigEditor.addRestConfigurationElement();
	}
	
	@Override
	public String getToolTipText() {
		return UIMessages.restEditorAddRestConfigurationActionButtonTooltip;
	}
	
	@Override
	public String getImageName() {
		return RestConfigConstants.IMG_DESC_ADD;
	}
}
