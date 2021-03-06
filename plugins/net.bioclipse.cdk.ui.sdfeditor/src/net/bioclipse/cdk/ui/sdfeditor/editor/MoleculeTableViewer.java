/*******************************************************************************
 * Copyright (c) 2009  Arvid Berg <goglepox@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor;

import java.util.List;

import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.CDKMoleculePropertySource;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.sourceforge.nattable.GridRegionEnum;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.action.SelectCellAction;
import net.sourceforge.nattable.config.DefaultBodyConfig;
import net.sourceforge.nattable.config.DefaultColumnHeaderConfig;
import net.sourceforge.nattable.config.DefaultRowHeaderConfig;
import net.sourceforge.nattable.config.SizeConfig;
import net.sourceforge.nattable.data.IColumnHeaderLabelProvider;
import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.event.matcher.MouseEventMatcher;
import net.sourceforge.nattable.model.DefaultNatTableModel;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.renderer.AbstractCellRenderer;
import net.sourceforge.nattable.support.EventBindingSupport;
import net.sourceforge.nattable.typeconfig.style.DefaultStyleConfig;
import net.sourceforge.nattable.typeconfig.style.DisplayModeEnum;
import net.sourceforge.nattable.typeconfig.style.IStyleConfig;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.views.properties.IPropertySource;


public class MoleculeTableViewer extends ContentViewer {

    public final static int STRUCTURE_COLUMN_WIDTH = 200;

    NatTable table;
    JCPCellPainter cellPainter;

    private int currentSelected;

    private Runnable dblClickHook;

    public MoleculeTableViewer(Composite parent, int style) {

        cellPainter = new JCPCellPainter();

        DefaultNatTableModel model = new DefaultNatTableModel();

        IColumnHeaderLabelProvider columnHeaderLabelProvider = new IColumnHeaderLabelProvider() {

            public String getColumnHeaderLabel( int col ) {
                List<Object> prop = getColumnHandler().getProperties();
                if(col == 0)
                    return "2D-structure";
                if(col<prop.size()+1 )
                    return prop.get(col-1).toString();
                return "";
            }
        };

        DefaultRowHeaderConfig rowHeaderConfig = new DefaultRowHeaderConfig();
        rowHeaderConfig.setRowHeaderColumnCount(1);
        SizeConfig rowHeaderColumnWidthConfig = new SizeConfig();
        rowHeaderColumnWidthConfig.setDefaultSize(STRUCTURE_COLUMN_WIDTH/3);
        //              columnWidthConfig.setDefaultSize(150);
        rowHeaderColumnWidthConfig.setDefaultResizable(true);
        rowHeaderColumnWidthConfig.setIndexResizable( 1, true );
        rowHeaderConfig.setRowHeaderColumnWidthConfig( rowHeaderColumnWidthConfig );

        DefaultBodyConfig bodyConfig = new DefaultBodyConfig(new IDataProvider() {



            public int getColumnCount() {
                if(getDataProvider()==null) return 0;
                return getDataProvider().getColumnCount();
            }

            public int getRowCount() {
                if(getDataProvider()==null) return 0;
                return getDataProvider().getRowCount()+1;
            }

            public Object getValue( int row, int col ) {
                if(getDataProvider()==null) return null;
                return getDataProvider().getValue( row, col );
            }

        });

        bodyConfig.setCellRenderer( new AbstractCellRenderer() {

            ICellPainter textPainter = new TextCellPainter();

            DefaultStyleConfig selectedStyle = new DefaultStyleConfig(ICellPainter.COLOR_LIST_SELECTION, GUIHelper.COLOR_WHITE, null, null);
            @Override
            public IStyleConfig getStyleConfig(String displayMode, int row, int col) {
                if (DisplayModeEnum.SELECT.name().equals(displayMode)) {
                    return selectedStyle;
                }
                return super.getStyleConfig(displayMode, row, col);
            }

            @Override
            public ICellPainter getCellPainter( int row, int col ) {

                if(col == 0)
                    return cellPainter;
                return textPainter;
            }

            public String getDisplayText( int row, int col ) {

                return getDataProvider().getValue( row, col ).toString();
            }

            public Object getValue( int row, int col ) {

                return getDataProvider().getValue( row, col );
            }

        });

        model.setBodyConfig(bodyConfig);
        model.setRowHeaderConfig(rowHeaderConfig);
        model.setColumnHeaderConfig( new DefaultColumnHeaderConfig(columnHeaderLabelProvider));

        model.setSingleCellSelection( false );
        model.setMultipleSelection( true );
//        model.setMultipleSelection( true );
//        model.


        SizeConfig columnWidthConfig = model.getBodyConfig().getColumnWidthConfig();
        columnWidthConfig.setDefaultSize(STRUCTURE_COLUMN_WIDTH/2);
        columnWidthConfig.setInitialSize( 0, STRUCTURE_COLUMN_WIDTH );
        //              columnWidthConfig.setDefaultSize(150);
        columnWidthConfig.setDefaultResizable(true);
        columnWidthConfig.setIndexResizable(1, true);

        // Row heights
        SizeConfig rowHeightConfig = model.getBodyConfig().getRowHeightConfig();
        rowHeightConfig.setDefaultSize(STRUCTURE_COLUMN_WIDTH);
        rowHeightConfig.setDefaultResizable(true);
        //                rowHeightConfig.setIndexResizable(1, false);

        // NatTable
        table = new NatTable(parent,
                     SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE
                     | SWT.DOUBLE_BUFFERED | SWT.V_SCROLL | SWT.H_SCROLL,
                     model
        );

        EventBindingSupport eventBindingSupport =table.getEventBindingSupport();
        eventBindingSupport.registerSingleClickBinding(
                 new MouseEventMatcher( SWT.COMMAND,
                                        GridRegionEnum.BODY.toString(), 1),
                                      new SelectCellAction(table, false, true));

        Listener listener = new Listener() {

            public void handleEvent( Event event ) {

                switch(event.type) {
                    case SWT.MouseDoubleClick:
                        doubleClickHook();
                        break;
                    case SWT.SELECTED:
                    case SWT.MouseUp:
                        updateSelection( getSelection() );
                }
            }
        };
        table.addListener( SWT.SELECTED, listener);
        table.addListener( SWT.MouseUp, listener );
        table.addListener( SWT.MouseDoubleClick, listener );
        ScrollBar vSb = table.getVerticalBar();
        vSb.setIncrement( 1 );
        vSb.setPageIncrement( 1 );
        table.scrollVBarUpdate( vSb );
    }

    @Override
    public Control getControl() {

        return table;
    }

    public static class MolTableElement implements IAdaptable {

        final IMoleculesEditorModel model;
        final int index;

        CDKMoleculePropertySource pSource;
        public MolTableElement(int index, IMoleculesEditorModel model) {
            this.model = model;
            this.index = index;
        }

        @SuppressWarnings("unchecked")
        public Object getAdapter( Class adapter ) {

            if(adapter.isAssignableFrom( ICDKMolecule.class )) {
                return model.getMoleculeAt( index );
            }
            if (adapter.isAssignableFrom(IPropertySource.class)) {
                ICDKMolecule mol = model.getMoleculeAt( index );
                if(mol instanceof CDKMolecule) {
                    if(pSource == null)
                        pSource=  new CDKMoleculePropertySource((CDKMolecule) mol);
                    return pSource;
                } else
                    return null;
            }
            return Platform.getAdapterManager().getAdapter(this, adapter);
        }
    }

    public static class MolTableSelection implements ISelection, IAdaptable{

        IMoleculesEditorModel model;
        int[] selection;

        public MolTableSelection(int[] selection,IMoleculesEditorModel model) {
            this.selection = selection;
            this.model = model;
        }

        public boolean isEmpty() {
            return selection.length==0;
        }

        @SuppressWarnings("unchecked")
        public Object getAdapter( Class adapter ) {

            if(adapter.isAssignableFrom( IMoleculesEditorModel.class ) ) {
                final IMoleculesEditorModel editorModel = new IMoleculesEditorModel() {

                    public ICDKMolecule getMoleculeAt( int index ) {
                        return model.getMoleculeAt( selection[index] );
                    }

                    public int getNumberOfMolecules() {
                        return selection.length;
                    }

                    public void markDirty( int index,
                                           ICDKMolecule moleculeToSave ) {

                        throw new UnsupportedOperationException();

                    }

                    public void save() {
                        throw new UnsupportedOperationException();
                    }

                };
                return editorModel;
            }
            return null;
        }
    }

    @Override
    public ISelection getSelection() {

        if(getContentProvider() instanceof MoleculeTableContentProvider) {

            int[] selected = table.getSelectionModel().getSelectedRows();
            int max = getDataProvider().getRowCount();

            if(selected.length==0) {
                currentSelected = -1;
                return StructuredSelection.EMPTY;
            }
            currentSelected = selected[0];

            IMoleculesEditorModel model;
            if(getInput() instanceof IMoleculesEditorModel) {
                model = (IMoleculesEditorModel) getInput();
                if(selected.length == 1) {
                    return new StructuredSelection(
                               new MolTableElement( selected[0], model));
                }else {

                    return new  StructuredSelection(
                             new MolTableSelection(selected,model));
                }
            }
        }

        return StructuredSelection.EMPTY;
    }

    private IDataProvider getDataProvider() {
        return (IDataProvider)getContentProvider();
    }

    private IMoleculeTableColumnHandler getColumnHandler() {
        return (IMoleculeTableColumnHandler) getContentProvider();
    }

    @Override
    public void refresh() {
        if(!table.isDisposed()) {
            table.reset();
            table.redraw();
            table.updateResize();
            table.update();
        }
    }

    @Override
    public void setSelection( ISelection selection, boolean reveal ) {

        // TODO Auto-generated method stub

    }

     IRenderer2DConfigurator getRenderer2DConfigurator() {
        return cellPainter.getRenderer2DConfigurator();
    }

     void setRenderer2DConfigurator(
                             IRenderer2DConfigurator renderer2DConfigurator ) {
        cellPainter.setRenderer2DConfigurator( renderer2DConfigurator);
    }


    protected void updateSelection(ISelection selection) {
        SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
        fireSelectionChanged(event);
    }


    public int getFirstSelected() {

        return currentSelected;
    }

    public void setDoubleClickHook(Runnable hook) {
        dblClickHook = hook;
    }
    protected void doubleClickHook() {
        if(dblClickHook!=null) {
            dblClickHook.run();
        }
    }
}
