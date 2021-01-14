package org.modelio.module.attacktreedesigner.impl;

import java.util.Map;
import org.modelio.api.modelio.model.IModelingSession;
import org.modelio.api.module.lifecycle.DefaultModuleLifeCycleHandler;
import org.modelio.api.module.lifecycle.ModuleException;
import org.modelio.vbasic.version.Version;

public class AttackTreeDesignerLifeCycleHandler extends DefaultModuleLifeCycleHandler {
    private AttackTreeModelChangeHandler modelChangeHandler = null;

    public AttackTreeDesignerLifeCycleHandler(final AttackTreeDesignerModule module) {
        super(module);
    }

    @Override
    public boolean start() throws ModuleException {
        IModelingSession session = this.module.getModuleContext().getModelingSession();
        this.modelChangeHandler = new AttackTreeModelChangeHandler();
        session.addModelHandler(this.modelChangeHandler);
        return super.start();
    }

    @Override
    public void stop() throws ModuleException {
        super.stop();
    }

    /**
     * @return
     */
    public static boolean install(final String modelioPath, final String mdaPath) throws ModuleException {
        return DefaultModuleLifeCycleHandler.install(modelioPath, mdaPath);
    }

    @Override
    public boolean select() throws ModuleException {
        return super.select();
    }

    @Override
    public void upgrade(final Version oldVersion, final Map<String, String> oldParameters) throws ModuleException {
        super.upgrade(oldVersion, oldParameters);
    }

    @Override
    public void unselect() throws ModuleException {
        super.unselect();
    }

}
