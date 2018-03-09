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
package org.fusesource.ide.camel.editor.restconfiguration;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class RestEditorColorManager {
	
	private static Map<String, Color> colorMap = new HashMap<>();
	
	static {
		Display display = Display.getDefault();
		colorMap.put(RestConfigConstants.REST_COLOR_LIGHT_BLUE, new Color(display, 235, 242, 250));
		colorMap.put(RestConfigConstants.REST_COLOR_LIGHT_ORANGE, new Color(display, 250, 241, 230));
		colorMap.put(RestConfigConstants.REST_COLOR_LIGHT_GREEN, new Color(display, 232, 245, 239));
		colorMap.put(RestConfigConstants.REST_COLOR_LIGHT_GREY, new Color(display, 240, 248, 255));
		colorMap.put(RestConfigConstants.REST_COLOR_LIGHT_RED, new Color(display, 250, 231, 231));
		colorMap.put(RestConfigConstants.REST_COLOR_DARK_BLUE, new Color(display, 93, 173, 255));
		colorMap.put(RestConfigConstants.REST_COLOR_DARK_ORANGE, new Color(display, 254, 162, 24));
		colorMap.put(RestConfigConstants.REST_COLOR_DARK_GREEN, new Color(display, 65, 205, 142));
		colorMap.put(RestConfigConstants.REST_COLOR_DARK_RED, new Color(display, 252, 60, 55));
	}
	
	Color getBackgroundColorForType(String tag) {
		if (RestConfigConstants.GET_VERB.equals(tag)) {
			return colorMap.get(RestConfigConstants.REST_COLOR_LIGHT_BLUE);
		}
		if (RestConfigConstants.PUT_VERB.equals(tag)) {
			return colorMap.get(RestConfigConstants.REST_COLOR_LIGHT_GREEN);
		}
		if (RestConfigConstants.POST_VERB.equals(tag)) {
			return colorMap.get(RestConfigConstants.REST_COLOR_LIGHT_ORANGE);
		}
		if (RestConfigConstants.DELETE_VERB.equals(tag)) {
			return colorMap.get(RestConfigConstants.REST_COLOR_LIGHT_RED);
		}
		return colorMap.get(RestConfigConstants.REST_COLOR_LIGHT_GREY);
	}		

	Color getForegroundColorForType(String tag) {
		Color foregroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
		if (RestConfigConstants.PUT_VERB.equals(tag)) {
			foregroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		}
		return foregroundColor;
	}		

	Color getImageColorForType(String tag) {
		if (RestConfigConstants.GET_VERB.equals(tag)) {
			return colorMap.get(RestConfigConstants.REST_COLOR_DARK_BLUE);
		}
		if (RestConfigConstants.PUT_VERB.equals(tag)) {
			return colorMap.get(RestConfigConstants.REST_COLOR_DARK_GREEN);
		}
		if (RestConfigConstants.POST_VERB.equals(tag)) {
			return colorMap.get(RestConfigConstants.REST_COLOR_DARK_ORANGE);
		}
		if (RestConfigConstants.DELETE_VERB.equals(tag)) {
			return colorMap.get(RestConfigConstants.REST_COLOR_DARK_RED);
		}
		return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
	}

	/**
	 * @param colorConstant from RestConfigConstants.REST_COLOR_XXX
	 * @return
	 */
	public Color get(String colorConstant) {
		return colorMap.get(colorConstant);
	}

}
