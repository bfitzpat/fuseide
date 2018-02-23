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
package org.jboss.tools.fuse.ui.bot.tests;

import static org.jboss.tools.fuse.reddeer.CamelCatalogUtils.CatalogType.LANGUAGE;
import static org.jboss.tools.fuse.reddeer.ProjectTemplate.EMPTY_SPRING;
import static org.jboss.tools.fuse.reddeer.wizard.NewFuseIntegrationProjectWizardDeploymentType.STANDALONE;
import static org.jboss.tools.fuse.reddeer.wizard.NewFuseIntegrationProjectWizardRuntimeType.KARAF;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.eclipse.ui.perspectives.JavaEEPerspective;
import org.eclipse.reddeer.junit.internal.runner.ParameterizedRequirementsRunnerFactory;
import org.eclipse.reddeer.junit.requirement.inject.InjectRequirement;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.requirements.autobuilding.AutoBuildingRequirement.AutoBuilding;
import org.eclipse.reddeer.requirements.cleanworkspace.CleanWorkspaceRequirement.CleanWorkspace;
import org.eclipse.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
import org.eclipse.reddeer.swt.api.CCombo;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.workbench.handler.EditorHandler;
import org.eclipse.reddeer.workbench.impl.shell.WorkbenchShell;
import org.jboss.tools.fuse.reddeer.CamelCatalogUtils;
import org.jboss.tools.fuse.reddeer.MavenDependency;
import org.jboss.tools.fuse.reddeer.ProjectType;
import org.jboss.tools.fuse.reddeer.component.Transform;
import org.jboss.tools.fuse.reddeer.editor.CamelEditor;
import org.jboss.tools.fuse.reddeer.projectexplorer.CamelProject;
import org.jboss.tools.fuse.reddeer.requirement.CamelCatalogRequirement;
import org.jboss.tools.fuse.reddeer.requirement.CamelCatalogRequirement.CamelCatalog;
import org.jboss.tools.fuse.reddeer.utils.ProjectFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.UseParametersRunnerFactory;
import org.xml.sax.SAXException;

/**
 * This test verifies if it is possible to set all supported languages and if all required dependencies were added to
 * the pom.xml file.
 * 
 * For this purpose the test uses the 'transform' camel component.
 * 
 * @author Andrej Podhradsky (apodhrad@redhat.com)
 */

@CamelCatalog
@CleanWorkspace
@AutoBuilding(false)
@RunWith(RedDeerSuite.class)
@OpenPerspective(JavaEEPerspective.class)
@UseParametersRunnerFactory(ParameterizedRequirementsRunnerFactory.class)
public class CamelEditorLanguageTest {

	public static final String PROJECT_NAME = "languages";
	public static final ProjectType PROJECT_TYPE = ProjectType.SPRING;
	public static final String DEPENDENCY_PATTERN = "$groupId/$artifactId";
	public static final String TESTED_COMPONENT_LABEL = "Transform _transform1";

	private static String initialPomContent;

	protected Logger log = Logger.getLogger(CamelEditorLanguageTest.class);

	private CamelProject camelProject;
	private CamelEditor camelEditor;
	private String language;

	@InjectRequirement
	public static CamelCatalogRequirement camelCatalogRequirement;
	private static CamelCatalogUtils camelCatalogUtils;

	public CamelEditorLanguageTest(String language) {
		this.language = language;
	}

	@Parameters(name = "{0}")
	public static Collection<String> getCamelLanguages() {
		List<String> camelLanguages = new ArrayList<>();
		// there is a bug in RedDeer, see https://github.com/eclipse/reddeer/issues/1914
		// that's why we use hard-coded languages - the languages were taken from
		// cat org/apache/camel/catalog/models/transform.json | jq -r '.properties.expression.oneOf | .[]'
		camelLanguages.add("constant");
		camelLanguages.add("el");
		camelLanguages.add("exchangeProperty");
		camelLanguages.add("groovy");
		camelLanguages.add("header");
		camelLanguages.add("javaScript");
		camelLanguages.add("jsonpath");
		camelLanguages.add("jxpath");
		camelLanguages.add("language");
		camelLanguages.add("method");
		camelLanguages.add("mvel");
		camelLanguages.add("ognl");
		camelLanguages.add("php");
		camelLanguages.add("python");
		camelLanguages.add("ref");
		camelLanguages.add("ruby");
		camelLanguages.add("simple");
		camelLanguages.add("spel");
		camelLanguages.add("sql");
		camelLanguages.add("terser");
		camelLanguages.add("tokenize");
		camelLanguages.add("vtdxml");
		camelLanguages.add("xpath");
		camelLanguages.add("xquery");
		camelLanguages.add("xtokenize");
		return camelLanguages;
	}

	/**
	 * Prepares test environment
	 */
	@BeforeClass
	public static void createTestProject() throws Exception {
		new WorkbenchShell().maximize();
		ProjectFactory.newProject(PROJECT_NAME).deploymentType(STANDALONE).runtimeType(KARAF)
				.version(camelCatalogRequirement.getConfiguration().getVersion()).template(EMPTY_SPRING).create();

		CamelProject camelProject = new CamelProject(PROJECT_NAME);
		camelProject.openCamelContext(PROJECT_TYPE.getCamelContext());
		initialPomContent = camelProject.getPomContent();
	}

	@BeforeClass
	public static void initializeCamelCatalogUtils() {
		camelCatalogUtils = new CamelCatalogUtils(camelCatalogRequirement.getConfiguration().getHome());
	}

	/**
	 * Cleans up test environment
	 */
	@AfterClass
	public static void setupDeleteProjects() {
		new WorkbenchShell();
		EditorHandler.getInstance().closeAll(true);
		ProjectFactory.deleteAllProjects();
	}

	@Before
	public void initProjectAndOpenEditor() throws Exception {
		camelProject = new CamelProject(PROJECT_NAME);
		camelProject.setPomContent(initialPomContent);
		camelProject.openCamelContext(PROJECT_TYPE.getCamelContext());

		camelEditor = new CamelEditor(PROJECT_TYPE.getCamelContext());
		camelEditor.addCamelComponent(new Transform(), "Route _route1");
	}

	@After
	public void closeEditor() {
		camelEditor.deleteCamelComponent(TESTED_COMPONENT_LABEL);
		camelEditor.close(true);
	}

	@Test
	public void testLanguage() throws FileNotFoundException, XPathExpressionException, ParserConfigurationException,
			SAXException, IOException {
		camelEditor.selectEditPart(TESTED_COMPONENT_LABEL);
		camelEditor.setProperty(CCombo.class, "Expression", language);
		new WaitUntil(new ShellIsAvailable("Progress Information"), false);
		new WaitWhile(new ShellIsAvailable("Progress Information"), TimePeriod.VERY_LONG);
		camelEditor.save();

		List<MavenDependency> actualDeps = camelProject.getMavenDependencies();
		MavenDependency expectedDep = camelCatalogUtils.getMavenDependency(LANGUAGE, language);
		assertTrue("Cannot find " + expectedDep + ".\nAvailable dependencies are " + actualDeps, expectedDep == null
				|| actualDeps.stream().filter(dep -> dep.equalsIgnoreVersion(expectedDep)).findAny().isPresent());
	}

}
