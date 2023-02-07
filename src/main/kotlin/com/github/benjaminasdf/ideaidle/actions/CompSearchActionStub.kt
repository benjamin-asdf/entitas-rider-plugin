package com.github.benjaminasdf.ideaidle.actions
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.github.benjaminasdf.idlelib.compsearchActions.CompsearchActions
import javax.swing.JComponent

class CompSearchActionStub : ComboBoxAction() {

    override fun createPopupActionGroup(button: JComponent?): DefaultActionGroup {
        return CompsearchActions.createPopupActionGroup(button);
    }

}

