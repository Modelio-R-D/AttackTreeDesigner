package org.modelio.module.attacktreedesigner.impl;

import org.modelio.api.module.AbstractJavaModule;
import org.modelio.api.module.context.IModuleContext;
import org.modelio.api.module.lifecycle.IModuleLifeCycleHandler;
import org.modelio.api.module.parameter.IParameterEditionModel;

public class AttackTreeDesignerModule extends AbstractJavaModule {
    private static final String MODULE_IMAGE = "/res/icon/module.png";

    private AttackTreeDesignerPeerModule peerModule = null;

    private AttackTreeDesignerLifeCycleHandler lifeCycleHandler = null;

    private static AttackTreeDesignerModule instance;

    public AttackTreeDesignerModule(final IModuleContext moduleContext) {
        super(moduleContext);
        
        AttackTreeDesignerModule.instance = this;
        
        this.lifeCycleHandler  = new AttackTreeDesignerLifeCycleHandler(this);
        this.peerModule = new AttackTreeDesignerPeerModule(this, moduleContext.getPeerConfiguration());
        init();
    }

    @Override
    public AttackTreeDesignerPeerModule getPeerModule() {
        return this.peerModule;
    }

    /**
     * Return the LifeCycleHandler  attached to the current module. This handler is used to manage the module lifecycle by declaring the desired implementation for the start, select... methods.
     */
    @Override
    public IModuleLifeCycleHandler getLifeCycleHandler() {
        return this.lifeCycleHandler;
    }

    /**
     * Method automatically called just after the creation of the module. The module is automatically instanciated at the beginning
     * of the MDA lifecycle and constructor implementation is not accessible to the module developer. The <code>init</code> method
     * allows the developer to execute the desired initialization.
     */
    @Override
    public IParameterEditionModel getParametersEditionModel() {
        return super.getParametersEditionModel();
    }

    @Override
    public String getModuleImagePath() {
        return AttackTreeDesignerModule.MODULE_IMAGE;
    }

    public static AttackTreeDesignerModule getInstance() {
        // Automatically generated method. Please delete this comment before entering specific code.
        return instance;
    }

}
