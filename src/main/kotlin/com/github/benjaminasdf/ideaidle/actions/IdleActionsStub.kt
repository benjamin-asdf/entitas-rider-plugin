package com.github.benjaminasdf.ideaidle.actions
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.github.benjaminasdf.idlelib.idleactions.IdleActions
import javax.swing.JComponent

class IdleActionsStub : ComboBoxAction() {

    override fun createPopupActionGroup(button: JComponent?): DefaultActionGroup {
        return IdleActions.createPopupActionGroup(button);
    }


}

