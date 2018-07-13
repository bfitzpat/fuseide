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
 * @author bfitzpat
 */
public class DeleteRestElementAction extends RestEditorAction {
	
	public DeleteRestElementAction(RestConfigEditor restConfigEditor, ImageRegistry imageReg) {
		super(restConfigEditor, imageReg);
	}
	
	@Override
	public void run() {
		restConfigEditor.removeRestElement();
	}
	
	@Override
	public String getToolTipText() {
		return UIMessages.deleteRestElementActionTooltip;
	}
	
	@Override
	public String getImageName() {
		return RestConfigConstants.IMG_DESC_DELETE;
	}
}
