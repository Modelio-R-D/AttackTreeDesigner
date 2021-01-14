package org.modelio.module.attacktreedesigner.ui;

import java.util.List;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.modelio.api.modelio.model.IModelingSession;
import org.modelio.api.modelio.model.ITransaction;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.infrastructure.TagParameter;
import org.modelio.metamodel.uml.infrastructure.TaggedValue;
import org.modelio.module.attacktreedesigner.api.AttackTreeTagTypes;
import org.modelio.module.attacktreedesigner.i18n.Messages;
import org.modelio.module.attacktreedesigner.impl.AttackTreeDesignerModule;
import org.modelio.module.attacktreedesigner.utils.elementmanager.tags.TagsManager;

public class AddCustomTagDialog extends Dialog {
    private static final int DEFAULT_MARGIN = 20;

    private Text propertyNameText;

    private Text propertyValueText;

    private Button cancelButton;

    private Button okButton;

    private ModelElement selectedElement = null;

    private Shell shell;

    public AddCustomTagDialog(Shell parent, int style) {
        super (parent, style);
    }

    public AddCustomTagDialog(Shell activeShell) {
        this (activeShell, SWT.NONE);
    }

    public void setSelectedElement(ModelElement selectedElement) {
        this.selectedElement = selectedElement;
    }

    public void open() {
        Shell parent = getParent();
        Display display = parent.getDisplay();
        
        this.shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        
        
        this.shell.setText(Messages.getString ("Ui.Dialog.AddCustomTag.Label"));
        this.shell.setLocation(parent.getBounds().x + (parent.getBounds().width/2), parent.getBounds().y + (parent.getBounds().height/2));
        // Layout with 2 columns
        GridLayout gridLayout = new GridLayout(2, false);
        this.shell.setLayout(gridLayout);
        
        gridLayout.marginTop = DEFAULT_MARGIN/2;
        gridLayout.marginRight = DEFAULT_MARGIN;
        gridLayout.marginLeft = DEFAULT_MARGIN;
        gridLayout.marginBottom = DEFAULT_MARGIN/2;
        gridLayout.verticalSpacing = DEFAULT_MARGIN/2;
        
        
        // First row : label 
        Label label = new Label(this.shell, SWT.NONE);
        label.setText(Messages.getString ("Ui.Dialog.CustomTagName.Label"));
        GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
        gridData.horizontalSpan = 2;
        label.setLayoutData(gridData);
        
        // Second row : label used as a separator
        label = new Label(this.shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
        gridData.horizontalSpan = 2;
        label.setLayoutData(gridData);
        
        // 3rd row
        label = new Label(this.shell, SWT.NULL);
        label.setText(Messages.getString ("Ui.Field.CustomTagName.Label"));
        this.propertyNameText = new Text(this.shell, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.widthHint = 200;
        this.propertyNameText.setLayoutData(gridData);
        
        
        // 4th row
        label = new Label(this.shell, SWT.NULL);
        label.setText(Messages.getString ("Ui.Field.CustomTagValue.Label"));
        this.propertyValueText = new Text(this.shell, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.widthHint = 200;
        this.propertyValueText.setLayoutData(gridData);
        
        // 5th row
        //  --> Cancel Button
        this.cancelButton = new Button(this.shell, SWT.PUSH);
        this.cancelButton.setText(Messages.getString ("Ui.Button.Cancel.Label"));
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.BEGINNING;
        gridData.widthHint = 100;
        this.cancelButton.setLayoutData(gridData);
        this.cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AddCustomTagDialog.this.shell.close();
            }
        });
        
        //  --> OK Button
        this.okButton = new Button(this.shell, SWT.PUSH);
        this.okButton.setText(Messages.getString ("Ui.Button.OK.Label"));
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.END;
        gridData.widthHint = 100;
        this.okButton.setLayoutData(gridData);
        this.okButton.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                if(AddCustomTagDialog.this.selectedElement == null) {
                    MessageDialog.openInformation(Display.getDefault().getActiveShell(), 
                            Messages.getString ("Ui.Dialog.NoSelectedElement.Label"), 
                            Messages.getString ("Ui.Dialog.NoSelectedElementAdd.Message"));
                    AddCustomTagDialog.this.shell.close();
                } else if (AddCustomTagDialog.this.propertyNameText.getText().isEmpty()) {
                    MessageDialog.openInformation(Display.getDefault().getActiveShell(), 
                            Messages.getString ("Ui.Dialog.EmptyName.Label"), 
                            Messages.getString ("Ui.Dialog.EmptyName.Message"));
                    
                } else if (AddCustomTagDialog.this.propertyValueText.getText().isEmpty()) {
                    MessageDialog.openInformation(Display.getDefault().getActiveShell(), 
                            Messages.getString ("Ui.Dialog.EmptyValue.Label"), 
                            Messages.getString ("Ui.Dialog.EmptyValue.Message"));
                } else  {
                    addNewCustomTag();
                }
            }
        
        });
        
        this.shell.pack();
        
        this.shell.open();
        
        while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    private void addNewCustomTag() {
        IModelingSession modelingSession = AttackTreeDesignerModule.getInstance().getModuleContext().getModelingSession ();
        
        boolean alreadyExistingTag = false;
        List<TaggedValue> attackTags = AddCustomTagDialog.this.selectedElement.getTag();
        for(TaggedValue attackTag:attackTags) {
            String tagDefinitionName = attackTag.getDefinition().getName();
            if(tagDefinitionName.equals(AttackTreeTagTypes.CUSTOM_TAG)) {
                List<TagParameter> tagParameters = attackTag.getActual();
                if(tagParameters.get(0).getValue().equals(AddCustomTagDialog.this.propertyNameText.getText())) {
                    alreadyExistingTag = true;
                    break;
                }
            }
        }
        if(alreadyExistingTag) {
            MessageDialog.openInformation(Display.getDefault().getActiveShell(), 
                    Messages.getString ("Ui.Dialog.existingName.Label"), 
                    Messages.getString ("Ui.Dialog.existingName.Message"));
        } else {
            try( ITransaction transaction = modelingSession.createTransaction(Messages.getString ("Info.Session.Create", TaggedValue.MNAME))){
        
                TagsManager.createCustomTag(modelingSession, AddCustomTagDialog.this.selectedElement, 
                        AddCustomTagDialog.this.propertyNameText.getText(), 
                        AddCustomTagDialog.this.propertyValueText.getText());
        
                transaction.commit ();
            }
            AddCustomTagDialog.this.shell.close();
        }
    }

}
