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

import java.io.File;
import java.text.StringCharacterIterator;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.ISharedDocumentAdapter;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.compare.structuremergeviewer.StructureCreator;
import org.eclipse.compare.structuremergeviewer.StructureDiffViewer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.jboss.tools.fuse.transformation.editor.Activator;
import org.jboss.tools.fuse.transformation.editor.internal.ModelViewer;
import org.jboss.tools.fuse.transformation.editor.internal.util.TransformationManager;
import org.jboss.tools.fuse.transformation.editor.internal.util.Util;
import org.jboss.tools.fuse.transformation.editor.wizards.ReloadTransformationWizard;
import org.jboss.tools.fuse.transformation.core.model.json.JsonModelGenerator;
import org.jboss.tools.fuse.transformation.core.model.xml.XmlModelGenerator;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.compare.*;

/**
 * @author brianf
 *
 */
public class CompareModelPage extends XformWizardPage {

    private static final String OBJECT_FACTORY_NAME = "ObjectFactory";

    private Composite _page;
    private Text _javaClassText;
    private org.jboss.tools.fuse.transformation.core.model.Model _javaModel = null;
    private ModelViewer _modelViewer;
    private final TransformationManager manager;
    private StructureDiffViewer _viewer;

    /**
     * @param pageName
     * @param model
     * @param isSource
     */
    public CompareModelPage(String pageName, final Model model, boolean isSource, TransformationManager manager) {
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

    private Viewer createCompareViewer(final Composite parent,
            final CompareConfiguration config) {
        final Tree localTree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        JavaStructureDiffViewerCreator creator = new JavaStructureDiffViewerCreator();
        _viewer = (StructureDiffViewer) creator.createViewer(parent, config);
//        viewer.setStructureCreator(new ModelStructureCreator()); 
//        viewer.setComparator(new ModelViewerComparator());
        return _viewer; 
        
    }
    
    class ModelStructureCreator extends StructureCreator {

        @Override
        public String getName() {
            return "Model Structure Compare";
        }

        @Override
        public String getContents(Object node, boolean ignoreWhitespace) {
            return null;
        }

        @Override
        protected IStructureComparator createStructureComparator(Object element, IDocument document,
                ISharedDocumentAdapter sharedDocumentAdapter, IProgressMonitor monitor) throws CoreException {
            return null;
        }
        
    }

    private static class ModelViewerComparator extends ViewerComparator { 
        @Override 
        public int compare(final Viewer viewer, final Object e1, final Object e2) { 
            final org.jboss.tools.fuse.transformation.core.model.Model en1 = (org.jboss.tools.fuse.transformation.core.model.Model)e1; 
            final org.jboss.tools.fuse.transformation.core.model.Model en2 = (org.jboss.tools.fuse.transformation.core.model.Model)e2; 
            return super.compare(viewer, en1, en2); 
        } 
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
        
        CompareConfiguration fCompareConfiguration= new CompareConfiguration();
        fCompareConfiguration.setLeftEditable(false);
        fCompareConfiguration.setRightEditable(false);

        Viewer viewer = createCompareViewer(group, fCompareConfiguration);
        viewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

    }
    
    public void setJavaModel(org.jboss.tools.fuse.transformation.core.model.Model jModel) {
        _javaModel = jModel;
    }
    
    public void updateViewer(String className, String compareClass) {
        _javaClassText.setText(compareClass);
        if (_modelViewer != null) {
            _modelViewer.setModel(_javaModel);
        }
        if (_viewer != null) {
            try {
                model.getProject().refreshLocal(IProject.DEPTH_INFINITE, new NullProgressMonitor());
            } catch (CoreException e1) {
                e1.printStackTrace();
            }
            IJavaProject javaProject = JavaCore.create(model.getProject());
            IType originalType;
            IType newType;
            try {
                originalType = javaProject.findType(className);
                newType = javaProject.findType(compareClass);
                _viewer.setInput(newType);
                _viewer.refresh();
            } catch (JavaModelException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private String reloadModel() {
        
        if (getWizard() instanceof ReloadTransformationWizard) {
            ReloadTransformationWizard reloadWiz = (ReloadTransformationWizard) getWizard();
            try {
                String sourceClassName = generateModel(
                        reloadWiz.getModel(),
                        reloadWiz.getModel().getSourceFilePath(),
                        reloadWiz.getModel().getSourceType(), true);
                return sourceClassName;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        String className = reloadModel();
        updateViewer(manager.rootSourceModel().getType(), className);
    }

    private String generateModel(final Model uiModel, 
            final String filePath, final ModelType type, boolean isSource) throws Exception {
        // Build class name from file name
        final StringBuilder className = new StringBuilder();
        final StringCharacterIterator iter = new StringCharacterIterator(filePath.substring(
                filePath.lastIndexOf('/') + 1, filePath.lastIndexOf('.')));
        boolean wordStart = true;
        for (char chr = iter.first(); chr != StringCharacterIterator.DONE; chr = iter.next()) {
            if (className.length() == 0) {
                if (Character.isJavaIdentifierStart(chr)) {
                    className.append(wordStart ? Character.toUpperCase(chr) : chr);
                    wordStart = false;
                }
            } else if (Character.isJavaIdentifierPart(chr)) {
                className.append(wordStart ? Character.toUpperCase(chr) : chr);
                wordStart = false;
            } else {
                wordStart = true;
            }
        }
        // Build package name from class name
        int sequencer = 1;
        String pkgName = className.toString();
        pkgName = "test." + pkgName;
        while (uiModel.getProject().exists(new Path(Util.JAVA_PATH + pkgName))) {
            pkgName = className.toString() + sequencer++;
        }
        pkgName = pkgName.toLowerCase();
        // Generate model
        final File targetClassesFolder = new File(uiModel.getProject().getFolder(Util.JAVA_PATH).getLocationURI());
        switch (type) {
        case OTHER:
        case CLASS: {
            final IJavaProject javaProject = JavaCore.create(uiModel.getProject());
            IType pkg = javaProject.findType(filePath, new NullProgressMonitor());
            if (pkg != null) {
                return pkg.getFullyQualifiedName();
            }
            return null;
        }
        case JAVA: {
            final IJavaProject javaProject = JavaCore.create(uiModel.getProject());
            IType pkg = javaProject.findType(filePath, new NullProgressMonitor());
            if (pkg != null) {
                return pkg.getFullyQualifiedName();
            }
            return null;
        }
        case JSON: {
            final JsonModelGenerator generator = new JsonModelGenerator();
            generator.generateFromInstance(className.toString(), pkgName, uiModel.getProject().findMember(filePath)
                    .getLocationURI().toURL(), targetClassesFolder);
            return pkgName + "." + className;
        }
        case JSON_SCHEMA: {
            final JsonModelGenerator generator = new JsonModelGenerator();
            generator.generateFromSchema(className.toString(), pkgName, uiModel.getProject().findMember(filePath)
                    .getLocationURI().toURL(), targetClassesFolder);
            return pkgName + "." + className;
        }
        case XSD: {
            final XmlModelGenerator generator = new XmlModelGenerator();
            final File schemaFile = new File(uiModel.getProject().findMember(filePath).getLocationURI());
            final JCodeModel model = generator.generateFromSchema(schemaFile, null, targetClassesFolder);
            String elementName = null;
            if (isSource) {
                elementName = uiModel.getSourceClassName();
            } else {
                elementName = uiModel.getTargetClassName();
            }
            String modelClass = null;
            Map<String, String> mappings = generator.elementToClassMapping(model);
            if (mappings != null && !mappings.isEmpty()) {
                modelClass = mappings.get(elementName);
            } else {
                modelClass = selectModelClass(model);
            }
            if (modelClass != null) {
                return modelClass;
            }
            return null;
        }
        case XML: {
            final XmlModelGenerator generator = new XmlModelGenerator();
            final File schemaPath = new File(uiModel.getProject().getFile(filePath + ".xsd").getLocationURI());
            final JCodeModel model = generator.generateFromInstance(new File(uiModel.getProject().findMember(filePath)
                    .getLocationURI()), schemaPath, null, targetClassesFolder);
            String elementName = null;
            if (isSource) {
                elementName = uiModel.getSourceClassName();
            } else {
                elementName = uiModel.getTargetClassName();
            }
            String modelClass = null;
            Map<String, String> mappings = generator.elementToClassMapping(model);
            if (mappings != null && !mappings.isEmpty()) {
                modelClass = mappings.get(elementName);
            } else {
                modelClass = selectModelClass(model);
            }
            if (modelClass != null) {
                return modelClass;
            }
            return null;
        }
        default:
            return null;
        }
    }

    private String selectModelClass(final JCodeModel model) {
        for (final Iterator<JPackage> pkgIter = model.packages(); pkgIter.hasNext();) {
            final JPackage pkg = pkgIter.next();
            for (final Iterator<JDefinedClass> classIter = pkg.classes(); classIter.hasNext();) {
                // TODO this only works when a single top-level class exists;
                // fix after issue #33 is fixed
                final JDefinedClass definedClass = classIter.next();
                if (OBJECT_FACTORY_NAME.equals(definedClass.name())) {
                    continue;
                }
                return definedClass.fullName();
            }
        }
        return null;
    }
    
}
