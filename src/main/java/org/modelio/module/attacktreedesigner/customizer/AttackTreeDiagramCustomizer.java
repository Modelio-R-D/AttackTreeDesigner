package org.modelio.module.attacktreedesigner.customizer;

import java.util.List;
import java.util.Map;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.modelio.api.modelio.diagram.IDiagramCustomizer;
import org.modelio.api.modelio.diagram.IDiagramService;
import org.modelio.api.module.IModule;
import org.modelio.module.attacktreedesigner.i18n.Messages;
import org.modelio.module.attacktreedesigner.impl.AttackTreeDesignerModule;
import org.modelio.module.attacktreedesigner.utils.IAttackTreeCustomizerPredefinedField;
import org.modelio.module.attacktreedesigner.utils.IDiagramCustomizerPredefinedField;

/**
 * This class handles the palette configuration of Attack Tree block diagram
 * @author ebrosse
 */
public class AttackTreeDiagramCustomizer implements IDiagramCustomizer {
    @Override
    public void fillPalette(PaletteRoot paletteRoot) {
        IDiagramService toolRegistry = AttackTreeDesignerModule.getInstance().getModuleContext().getModelioServices().getDiagramService();
        
        final PaletteDrawer commonGroup = new PaletteDrawer("Default", null);
        commonGroup.setInitialState(PaletteDrawer.INITIAL_STATE_OPEN);
        commonGroup.add(new SelectionToolEntry());
        commonGroup.add(new MarqueeToolEntry());
        paletteRoot.add(commonGroup);
        
        paletteRoot.add(this.createAttackTreeGroup(toolRegistry));
        paletteRoot.add(this.createDefaultNotesGroup(toolRegistry));
        paletteRoot.add(this.createDefaultFreeDrawingGroup(toolRegistry));
    }

    protected PaletteEntry createDefaultFreeDrawingGroup(final IDiagramService toolRegistry) {
        final PaletteDrawer group = new PaletteDrawer(Messages.getString("AttackTreePaletteGroup.Freedrawing"), null);
        
        group.add(toolRegistry.getRegisteredTool(IDiagramCustomizerPredefinedField.DrawingRectangle));
        group.add(toolRegistry.getRegisteredTool(IDiagramCustomizerPredefinedField.DrawingEllipse));
        group.add(toolRegistry.getRegisteredTool(IDiagramCustomizerPredefinedField.DrawingText));
        group.add(toolRegistry.getRegisteredTool(IDiagramCustomizerPredefinedField.DrawingLine));
        
        group.setInitialState(PaletteDrawer.INITIAL_STATE_OPEN);
        return group;
    }

    protected PaletteEntry createDefaultNotesGroup(final IDiagramService toolRegistry) {
        final PaletteDrawer group = new PaletteDrawer(Messages.getString("AttackTreePaletteGroup.NotesAndConstraints"), null);
        
        group.add(toolRegistry.getRegisteredTool(IDiagramCustomizerPredefinedField.Note));
        group.add(toolRegistry.getRegisteredTool(IDiagramCustomizerPredefinedField.Constraint));
        group.add(toolRegistry.getRegisteredTool(IDiagramCustomizerPredefinedField.ExternDocument));
        group.setInitialState(PaletteDrawer.INITIAL_STATE_OPEN);
        return group;
    }

    private PaletteEntry createAttackTreeGroup(final IDiagramService toolRegistry) {
        final PaletteDrawer group = new PaletteDrawer(Messages.getString("AttackTreePaletteGroup.AttackTree"), null);
        group.add(toolRegistry.getRegisteredTool(IAttackTreeCustomizerPredefinedField.ATTACK));
        group.add(toolRegistry.getRegisteredTool(IAttackTreeCustomizerPredefinedField.AND));
        group.add(toolRegistry.getRegisteredTool(IAttackTreeCustomizerPredefinedField.OR));
        group.add(toolRegistry.getRegisteredTool(IAttackTreeCustomizerPredefinedField.CONNECTION));
        group.add(toolRegistry.getRegisteredTool(IAttackTreeCustomizerPredefinedField.COUNTER_MEASURE));
        group.add(toolRegistry.getRegisteredTool(IAttackTreeCustomizerPredefinedField.TREE_REFERENCE));
        group.add(toolRegistry.getRegisteredTool(IAttackTreeCustomizerPredefinedField.ANALYSE_THREAT));
        
        group.setInitialState(PaletteDrawer.INITIAL_STATE_OPEN);
        return group;
    }

    @Override
    public boolean keepBasePalette() {
        return false;
    }

    @Override
    public void initialize(IModule module, List<org.modelio.api.modelio.diagram.tools.PaletteEntry> tools, Map<String, String> hParameters, boolean keepBasePalette) {
    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }

}
