package com.github.benjaminasdf.ideaidle.intention

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

class InsertComponentIntention : PsiElementBaseIntentionAction(), IntentionAction  {

    override fun getText(): String {
        return "Get<Component>"
    }

    override fun getFamilyName(): String {
        return "IdleIntentions"
    }

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
     return true
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        TODO("Not yet implemented")
    }

}