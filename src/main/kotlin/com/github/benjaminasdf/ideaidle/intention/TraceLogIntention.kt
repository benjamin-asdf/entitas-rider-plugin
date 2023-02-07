package com.github.benjaminasdf.ideaidle.intention

import com.github.benjaminasdf.ideaidle.elispinterop.ElispUtil.inferiorElispEval
import com.github.benjaminasdf.ideaidle.elispinterop.ElispUtil.lispData
import com.github.benjaminasdf.ideaidle.elispinterop.ElispUtil.quote
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.lang.ASTNode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PlainTextTokenTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil
import com.intellij.psi.impl.source.tree.PlainTextASTFactory

class TraceLogIntention : PsiElementBaseIntentionAction(), IntentionAction  {
    override fun getText(): String {
        return "Add -> logs"
    }

    override fun getFamilyName(): String {
        return "IdleIntentions"
    }

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return true
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null) return
        var elm = element
        while (elm.parent !is PsiFile && elm.parent != null) {
            elm = elm.parent
        }


        ApplicationManager.getApplication().runWriteAction {
            val factory = PlainTextASTFactory()

            val s = inferiorElispEval(
                editor,
                element.containingFile.virtualFile.path,
                "idea/trace-log-method",
                lispData("setf",
                    "idea/arg-data",
                    quote(listOf(":idea-point", editor.caretModel.offset))))

            if (s != null)  {

                if (s.isBlank()) {
                    println("emacs return is blank")
                    return@runWriteAction
                }

                val n = factory.createLeaf(PlainTextTokenTypes.PLAIN_TEXT,s)
                val node = n as ASTNode
                CodeEditUtil.setNodeGenerated(node,true)

                if (elm.node == null) {
                    println("elm node null")
                }

                elm.node.treeParent.replaceChild(elm.node,node)
            }
        }

    }

}