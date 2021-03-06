/*******************************************************************************
 * Copyright (c) 2007-2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth - core API and implementation
 *******************************************************************************/
package net.bioclipse.jmol.editors;

import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JScrollPane;

import net.bioclipse.jmol.Activator;
import net.bioclipse.jmol.views.JmolPanel;
import net.bioclipse.jmol.views.JmolCompMouseListener;
import net.bioclipse.jmol.views.JmolSelection;
import net.bioclipse.jmol.views.outline.JmolContentOutlinePage;
import net.bioclipse.jmol.views.outline.JmolModel;
import net.bioclipse.jmol.views.outline.JmolModelString;
import net.bioclipse.jmol.views.outline.JmolObject;

import org.apache.log4j.Logger;

import net.bioclipse.core.IResourcePathTransformer;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.util.LogUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.ide.IDE;
import org.jmol.modelset.Model;

/**
 * A Multi Page Editor with Jmol embedded on foremost page.
 */
public class JmolEditor extends MultiPageEditorPart 
                        implements IResourceChangeListener, 
                                   IAdaptable, 
                                   ISelectionListener, 
                                   ISelectionProvider {

    public static final String EDITOR_ID 
        = "net.bioclipse.jmol.editors.JmolEditor";

    private static final Logger logger = Logger.getLogger(JmolEditor.class);

    /** The text editor used in page 1. */
//    private TextEditor editor;

    /** The ContentOutlinePage for the Outline View */
    JmolContentOutlinePage fOutlinePage;

    /** 
     * The JmolPanel that we can get the JmolViewer 
     * from to e.g. call scripts 
     */
    JmolPanel jmolPanel;

    /** Registered listeners */
    private volatile List<ISelectionChangedListener> selectionListeners;

    /** Store last selection */
    private volatile JmolSelection selection;

    //Should we hold the model as a string (read from file) or let 
    //it be the actual file?
    //Not yet decided. Will start by having as String.
    //Must check how react on resource changes works.
    //Ola 2007-11-20
    String content;        //Read from EditorInput

    /**
     * Creates a multi-page editor example.
     */
    public JmolEditor() {
        super();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
        selectionListeners = new ArrayList<ISelectionChangedListener>();
    }

    /**
     * Creates page 0 of the multi-page editor,
     * which consists of Jmol.
     */
    void createPage0() {
    	
        /*
         * Set a Windows specific AWT property that prevents heavyweight
         * components from erasing their background. Note that this is a global
         * property and cannot be scoped. It might not be suitable for your
         * application.
         */
        try {
        	System.setProperty("sun.awt.noerasebackground", "true");
        } catch (NoSuchMethodError error) {
        }

        Composite parent = new Composite(getContainer(), SWT.NONE);

        //Set the layout for parent
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.verticalSpacing = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 2;
        parent.setLayout(layout);

        GridData layoutData = new GridData();
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace = true;
        parent.setLayoutData(layoutData);

        //Add the Jmol composite to the top
        Composite composite = new Composite( parent, SWT.NO_BACKGROUND 
                                                   | SWT.EMBEDDED
                                                   | SWT.DOUBLE_BUFFERED );
        layout = new GridLayout();
        composite.setLayout(layout);
        layoutData = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(layoutData);



        java.awt.Frame awtFrame = SWT_AWT.new_Frame(composite);
        java.awt.Panel awtPanel 
            = new java.awt.Panel(new java.awt.BorderLayout());
        awtFrame.add(awtPanel);

        jmolPanel = new JmolPanel(this);
        JScrollPane scrollPane = new JScrollPane(jmolPanel);
        awtPanel.add(scrollPane);

        jmolPanel.addMouseListener((MouseListener) new
                JmolCompMouseListener(composite,this));

        final float scaleFactor = 0.3f;
        jmolPanel.addKeyListener( new KeyAdapter() {
           @Override
            public void keyPressed( KeyEvent e ) {

               if(e.getKeyCode() != KeyEvent.VK_W)
                   return;

               int onmask;
               String vers = System.getProperty( "os.name" ).toLowerCase();
               if( vers.indexOf( "mac" ) != -1)
                   onmask = KeyEvent.META_DOWN_MASK;
               else
                   onmask = KeyEvent.CTRL_DOWN_MASK;
               int offmask = KeyEvent.SHIFT_DOWN_MASK
                            |KeyEvent.ALT_DOWN_MASK;
               if ( (e.getModifiersEx() & (onmask | offmask)) == onmask) {
                   final IEditorPart editor = JmolEditor.this;
                   Display.getDefault().syncExec(new Runnable() {
                       public void run() {
                           editor.getSite().getPage().closeEditor(editor, true);
                       };
                   });
               }
            }
        });
        jmolPanel.requestFocusInWindow();
        jmolPanel.addMouseWheelListener(
            new java.awt.event.MouseWheelListener() {
                public void mouseWheelMoved(MouseWheelEvent e) {
                    int clicks = e.getWheelRotation();
                    float z = jmolPanel.getViewer().getZoomPercentFloat();
                    if (clicks > 0) {
                        float newZ = z * (1 - scaleFactor);
                        if (newZ > 5.0) {
                            runScriptSilently("zoom " + newZ);
                        }
                    } else {
                        float newZ = z * (1 + scaleFactor);
                        if (newZ < 5000.0) {
                            runScriptSilently("zoom " + newZ);
                        }
                    }
                }
            }
        );

        content = getContentsFromEditor();
        if (content == null) {
            logger.error("Could not get FILE in jmol editor");
            content = "";//return;
        }

        jmolPanel.getViewer().openStringInline(content);

        // Initialize jmol
        // ===============
        // Use halos as selection marker
        jmolPanel.getViewer().setSelectionHalos(true);
        
        // display all frames, then use 'display'
        runScript("frame 0.0");
        runScript("display 1.1");

        runScript("select none");
        
        //make clicking on elemtents select those elements
        if (jmolPanel.getViewer().getPolymerCount() == 0 ) {
            runScript("set picking select atoms");
        }
        else {
            runScript("set picking select group");
        }
        // End Initialize jmol
        // ===============

        int index = addPage(parent);
        setPageText(index, "Jmol");

        // Post selections in Jmol to Eclipse
        getSite().setSelectionProvider(this);
        
        // Register help context for this editor
        PlatformUI.getWorkbench().getHelpSystem().setHelp(
                this.getControl( 0 ),
                "net.bioclipse.jmol.jmolEditor"
        );
    }

    /**
     * Creates page 1 of the multi-page editor,
     * which contains a text editor.
     */
    void createPage1() {
//        try {
//            editor = new TextEditor();
//            int index = addPage(editor, getEditorInput());
//            setPageText(index, editor.getTitle());
//        } catch (PartInitException e) {
//            ErrorDialog.openError(
//                    getSite().getShell(),
//                    "Error creating nested text editor",
//                    null,
//                    e.getStatus());
//        }
    }

    /**
     * Creates the pages of the multi-page editor.
     */
    protected void createPages() {

        createPage0();
        //createPage1();

        getSite().getPage().addSelectionListener(this);
    }

    /**
     * The <code>MultiPageEditorPart</code> implementation of this
     * <code>IWorkbenchPart</code> method disposes all nested editors.
     * Subclasses may extend.
     */
    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        super.dispose();
    }
    
    public void load(IFile file) throws CoreException {
        BufferedReader contentStream = 
            new BufferedReader(new InputStreamReader(file.getContents()));
        StringBuffer stringModel = new StringBuffer();
        try {
            String line;
            while ((line = contentStream.readLine()) != null) {
                stringModel.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        jmolPanel.getViewer().loadInline(stringModel.toString());
    }
    
    /**
     * Take a snapshot of the editor contents and save as a png file
     */
    public void snapshot(IFile file) {
        Image image = new BufferedImage(jmolPanel.getWidth(),
                                        jmolPanel.getHeight(),
                                        BufferedImage.TYPE_INT_RGB);
        jmolPanel.paint(image.getGraphics());

        try {
            // this seems like a convoluted way to do things...
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
            ImageIO.write((RenderedImage) image, "PNG", outputStream);
            ByteArrayInputStream input = 
                new ByteArrayInputStream(outputStream.toByteArray());
            if (file.exists()) {
                file.setContents(input, false, true, null);
            } else {
                file.create(input, false, null);
            }
        } catch (IOException ioe) {
            LogUtils.debugTrace( logger, ioe );
        } catch (CoreException e) {
            LogUtils.debugTrace( logger, e );
        }
    }
    
    /**
     * Saves the multi-page editor's document.
     */
    public void doSave(IProgressMonitor monitor) {
        //Not implemented
    }
    
    /**
     * Saves the multi-page editor's document as another file.
     * Also updates the text for page 0's tab, and updates this multi-page 
     * editor's input to correspond to the nested editor's.
     */
    public void doSaveAs() {
        //Not implemented
    }
    
    /* (non-Javadoc)
     * Method declared on IEditorPart
     */
    public void gotoMarker(IMarker marker) {
        setActivePage(0);
        IDE.gotoMarker(getEditor(0), marker);
    }
    
    public void init(IEditorSite site, IEditorInput editorInput)
                                                    throws PartInitException {
        super.init(site, editorInput);
        setPartName(editorInput.getName());

    }

    /* (non-Javadoc)
     * Method declared on IEditorPart.
     */
    public boolean isSaveAsAllowed() {
        return false;
    }
    
    /**
     * Handle resource changes
     */
    public void resourceChanged(final IResourceChangeEvent event){

        final IEditorInput input=getEditorInput();
        if (!( input instanceof IFileEditorInput ))
            return;
        final IFile jmolfile=((IFileEditorInput)input).getFile();
        
        /*
         * Closes editor if resource is deleted
         */
        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            
            IResourceDelta rootDelta = event.getDelta();
            //get the delta, if any, for the documentation directory
            
            final List<IResource> deletedlist = new ArrayList<IResource>();
            
            IResourceDelta docDelta = rootDelta.findMember(jmolfile.getFullPath());
            if (docDelta != null){
                IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
                    public boolean visit(IResourceDelta delta) {
                       //only interested in removal changes
                       if ((delta.getFlags() & IResourceDelta.REMOVED) == 0){
                           deletedlist.add( delta.getResource() );
                       }
                       return true;
                    }
                 };
                 try {
                    docDelta.accept(visitor);
                 } catch (CoreException e) {
                    LogUtils.handleException( e, logger, Activator.PLUGIN_ID );
                 }
            }
                
            if (deletedlist.size()>0 && deletedlist.contains( jmolfile )){
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        if (getSite()==null) 
                            return;
                        if (getSite().getWorkbenchWindow()==null) 
                            return;
                        
                        IWorkbenchPage[] pages = getSite().getWorkbenchWindow()
                                                          .getPages();
                        for (int i = 0; i<pages.length; i++) {
                                IEditorPart editorPart
                                  = pages[i].findEditor(input);
                                pages[i].closeEditor(editorPart,true);
                        }
                    }
                });
            }
            

            
        }

        /*
         * Closes all editors with this editor input on project close.
         */
        if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    if (getSite()==null) 
                        return;
                    if (getSite().getWorkbenchWindow()==null) 
                        return;
                    
                    IWorkbenchPage[] pages = getSite().getWorkbenchWindow()
                                                      .getPages();
                    for (int i = 0; i<pages.length; i++) {
                        if ( jmolfile.getProject()
                                .equals( event.getResource() )) {
                            IEditorPart editorPart
                              = pages[i].findEditor(input);
                            pages[i].closeEditor(editorPart,true);
                        }
                    }
                }
            });
        }
    }

    private String getContentsFromEditor() {

        IEditorInput input=getEditorInput();
        IResourcePathTransformer transformer 
                                    = ResourcePathTransformer.getInstance();

        try {

            String val = (String) input.getAdapter( String.class );
            if(val != null) return val;
            
            if ((input instanceof IFileEditorInput) && 
                    ((IFileEditorInput)input).getFile().exists()) {
                return readFile( ((IFileEditorInput)input)
                                                     .getFile().getContents());
            }
            if ( input instanceof IPathEditorInput) {
                IFile file = transformer
                                    .transform( ((IPathEditorInput)input)
                                    .getPath().toOSString());
                return readFile( file.getContents() );
            }
            if( input instanceof IURIEditorInput) {
                URI uri = ((IURIEditorInput)input).getURI();
                IFile file = transformer.transform( uri.toString() );
                return readFile( file.getContents() );
            }
            
            logger.debug("Can't read input");
            //TODO: Close editor?
            return null;

        } catch (CoreException e) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace(logger, e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace(logger, e);
        }

        return null;
    }

    private String readFile(InputStream instream) throws IOException {        
        StringBuilder builder = new StringBuilder();

        // read bytes until eof
        for(int i = instream.read(); i != -1; i = instream.read())
        {
            builder.append((char)i);
        }
        instream.close();

        return builder.toString();
    }

    /**
     *
     * Provide Adapters for the JmolEditor
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class required) {

        //Adapter for Outline
        if (IContentOutlinePage.class.equals(required)) {
            if (fOutlinePage == null) {
                fOutlinePage
                    = new JmolContentOutlinePage(getEditorInput(), this);
//                if (getEditorInput() != null)
//                fOutlinePage.setInput(getEditorInput());
            }
            return fOutlinePage;
        }
        
        if (required == JmolModelString.class) {
            String jms = (String) jmolPanel.getViewer()
                                           .getProperty( "String", 
                                                         "stateinfo", 
                                                         "" );
            JmolModelString jmso = new JmolModelString(jms);
            return jmso;
        }
        
        return super.getAdapter(required);
    }

    public void runScript(String script){
        logger.debug("Running jmol script: '" + script + "'");
        String res = jmolPanel.getViewer().evalString(script);
        if (res!=null)
            logger.debug("Jmol said: '" + res + "'");
    }
    
    public void runScriptSilently(String script){
        logger.debug("Running jmol script: '" + script + "'");
        jmolPanel.getViewer().evalString(script);
    }

    public JmolPanel getJmolPanel() {
        return jmolPanel;
    }

    public void setJmolPanel(JmolPanel jmolPanel) {
        this.jmolPanel = jmolPanel;
    }

    @SuppressWarnings("unchecked")
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {

        //Check what part it is selected in
        //Only listen for the outline with our created JmolPage (IAdapter)
        if (part instanceof ContentOutline) {
            ContentOutline outline = (ContentOutline) part;
            IPage pg=outline.getCurrentPage();
            if (fOutlinePage==null) return;
            if (!(fOutlinePage.equals(pg)))
                return;
        }

        if (selection instanceof IStructuredSelection) {
            IStructuredSelection selection2 = (IStructuredSelection) selection;

            if (selection2==null){
                runScript("select all; halos off;");
                return;
            }

            List<String> selectedModelsList=new ArrayList<String>();
            List<String> selectedObjects=new ArrayList<String>();

            for (Iterator selIt=selection2.iterator();selIt.hasNext();){
                Object element=selIt.next();

                //Add models separately as they use display instead of select
                if (element instanceof JmolModel) {
                    JmolModel jmodel = (JmolModel) element;
                    Model model=(Model) jmodel.getObject();

                    //Add +1 as jmol uses base 1 and arrays 0
                    selectedModelsList.add(
                        String.valueOf(model.getModelIndex()+1) );
                }

                else if (element instanceof JmolObject) {
                    JmolObject jobj = (JmolObject) element;
                    selectedObjects.add(jobj.getSelectString());
                }
            }

            /*
            //List used to collect monomers/polymers to select
            List<String> selectedMonomersList=new ArrayList<String>();
            List<String> selectedModelsList=new ArrayList<String>();

            for (Iterator selIt=selection2.iterator();selIt.hasNext();){
                Object element=selIt.next();

                //Collect all selected objects
                if (element instanceof Model) {
                    Model model=(Model)element;
                    int currentModelIndex=jmolPanel.getViewer().getDisplayModelIndex();
                    if (model.getModelIndex()!=currentModelIndex){
                        logger.debug("Should change to model: " + model.getModelIndex());

                        selectedModelsList.add(String.valueOf(model.getModelIndex()+1));
                        //Add +1 as jmol uses base 1 and arrays 0
                        runScript("display 1." + (model.getModelIndex()+1));
                    }
                }

                //Replace BioPolymer with Chain: TODO
                else if (element instanceof BioPolymer) {
                    //We should select all entire BioPplymer
                    BioPolymer bpol = (BioPolymer) element;
                    for (int i=0; i<bpol.getMonomerCount();i++){
                        Monomer mono=bpol.getMonomers()[i];
                        String selStr=String.valueOf(mono.getSeqNumber());
                        int c=mono.getChainID();
                        if (c>0)
                            selStr+=":"+mono.getChainID();
                            //Add this monomer to selection
                        selectedMonomersList.add(selStr);
                    }

                }
                else if (element instanceof Monomer) {
                    Monomer mono = (Monomer) element;
                    String selStr=String.valueOf(mono.getSeqNumber());
                    int c=mono.getChainID();
                    if (c>0)
                        selStr+=":"+mono.getChainID();
                        //Add this monomer to selection
                    selectedMonomersList.add(selStr);
                }
                //Handle proteinStructures?
             */

//            Polymers are collections of Monomers, which extend Group. Each Group
//            is a member of a chain.

//            Not all groups are monomers, because not all groups are DNA, RNA,
//            carbohydrate, or protein (for example, H2O).

//            Chains are very specifically the sets defined in PDB and mmCIF
//            files based on the character in the chain field. If this field
//            is blank, it's still a chain -- the blank chain. For a file that
//            is not PDB or mmCIF, there is one chain,
//            and it's designation is blank.

            //A polymer is smaller than a chain. "Chain" refers to PDB chain.
            //water molecules in a PDB file are part of a chain, not a polymer.
            //polymers are only DNA, RNA, protein, carbohydrate


//            else if (element instanceof Strands) {
//            PDBStrand strand = (PDBStrand) chobj;
//            selectionList.add("*:" + strand.getStrandName());
//            }



            //Handle selection of Frames by DISPLAY in Jmol
            if (selectedModelsList.size() > 0) {
                if (selectedModelsList.get(0) != null) {

                    selectedModelsList=removeDuplicates(selectedModelsList);

                    //Collect all Select commands into one string
                    String collectedSelects="Display ";
                    for ( Iterator<String> it = selectedModelsList.iterator(); 
                          it.hasNext(); ) {
                        String sel = it.next();
                        collectedSelects+="1." +sel+",";
                    }

                    //Remove last comma
                    collectedSelects = 
                        collectedSelects
                            .substring( 0, collectedSelects.length() - 1 );
                    logger.debug("Collected display string: '" 
                                 + collectedSelects + "'");

                    runScript(collectedSelects);
                }
            }

            //Handle selection of JmolObjects by SELECT in Jmol
            if (selectedObjects.size() > 0) {
                if (selectedObjects.get(0) !=null) {

                    //Sort list of monomers and remove duplicates
                    selectedObjects=removeDuplicates(selectedObjects);

                    //Collect all Select commands into one string
                    String collectedSelects="Select ";
                    for (Iterator<String> it = selectedObjects.iterator(); 
                          it.hasNext();) {
                        String sel = it.next();
                        collectedSelects+=sel+",";
                    }

                    //Remove last comma
                    collectedSelects 
                        = collectedSelects
                          .substring( 0, collectedSelects.length()-1);
                    logger.debug("Collected select string: '" 
                                 + collectedSelects + "'");

                    runScript(collectedSelects);
                }
            } // else: nothing to select
        }
    }

    /**
     * Convenience method to remove duplicates in a list
     * @param items
     * @return
     */
    @SuppressWarnings("unchecked")
    public List removeDuplicates(List items) {
        Set set = new LinkedHashSet();
        set.addAll(items);
        return new ArrayList(set);
    }

    /* Below are for setting selections in Bioclipse from Jmol, e.g when
     clicked on an Atom*/

    public void addSelectionChangedListener(
                    ISelectionChangedListener listener) {
        if ( !selectionListeners.contains(listener) ) {
            selectionListeners.add(listener);
        }
    }

    public ISelection getSelection() {
        return selection;
    }

    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (selectionListeners.contains(listener))
            selectionListeners.remove(listener);
    }

    public void setSelection(ISelection selection) {
        if (!(selection instanceof JmolSelection)) 
            return;

        this.selection = (JmolSelection)selection;

        if ( this.selection.updateJmolSelection() ) {
            String selStr;
            if ( selection.isEmpty() ) {
                selStr = "Select none";
            }
            else {
                StringBuilder builder = new StringBuilder();
                builder.append( "Select " );
                for ( String s : this.selection) {
                    builder.append( s );
                    builder.append( ", " );
                }
                // -2 to get rid of last ", " part
                selStr = builder.substring( 0, builder.length() - 2 );
            }
            runScript(selStr);
        }

        if (selectionListeners==null) 
            return;
        
        java.util.Iterator<ISelectionChangedListener> iter 
            = selectionListeners.iterator();
        while ( iter.hasNext() ) {
            final ISelectionChangedListener listener = iter.next();
            final SelectionChangedEvent e 
                = new SelectionChangedEvent(this, this.selection);
            //Does SWT stuff so this has to be called on SWT's thread
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    listener.selectionChanged(e);
                }
            });

        }
    }

    public IEditorPart getPart(){
        return this;
    }
    
    public void setDataInput(IEditorInput input){  
        String data = (String) input.getAdapter( String.class );
        if(jmolPanel != null && data != null) {
            jmolPanel.getViewer().openStringInline( data );
        }
    }

    public void runScript( String script, boolean repportErrorToJSConsole ) {
        if ( repportErrorToJSConsole ) {
            jmolPanel.getJmolListener().toggleReportErrorToJSConsole();
        }
        runScript(script);
    }
}