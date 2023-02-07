package com.github.benjaminasdf.ideaidle.elispinterop

import com.github.benjaminasdf.idlelib.ideaidleconfig.IdeaIdleConfig
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.editor.Editor
import java.io.IOException
import java.util.concurrent.TimeUnit

object ElispUtil {

    fun callEmacs(editor: Editor, onProc: (ProcessBuilder) -> ProcessBuilder, vararg cmd: String): String? {
        try {
            println("Run cmd: " + cmd.joinToString(","))
            var proc =
                ProcessBuilder(*cmd)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE).let {
                        onProc(it).start()
                    }

            if (!proc.waitFor(2, TimeUnit.SECONDS)) {
                HintManager.getInstance().showInformationHint(editor,"emacs timed out")
                proc.destroyForcibly()
            }

            if (proc.errorStream.bufferedReader().readText().let {
                    if ("Lisp error:".toRegex().matches(it)) {
                        println(it)
                    } else {
                        val content = "\\(user-error \"(.*)\"\\)".toRegex().find(it)?.groupValues?.get(1) ?: it
                        HintManager.getInstance().showErrorHint(editor,content)
                }
                it.isNotEmpty() }) {
                return null
            }
            return proc.inputStream.bufferedReader().readText().replace("\r\n","\n")
        } catch(e: IOException) {
            e.printStackTrace()
            return null;
        }
    }

    fun inferiorElispEval(editor: Editor, file: String, fn: String, data: String): String? {

        return callEmacs(
            editor, { it },
            IdeaIdleConfig.inferiorElisp(),
            "-batch", file,
            "--eval", lispData("add-to-list",quote("load-path"), lispString(IdeaIdleConfig.elispFile(""))),
            "--load", IdeaIdleConfig.elispFile("core.el"),
            "--load", IdeaIdleConfig.elispFile("intentions.el"),
            "--eval", data,
            "-f", "idea/main",
            "-f", fn,
            "-f", "idea/print"
        )

    }


    fun lispString(name: String): LispString = LispString(name)

    fun quote(rst: Any): Collection<Any> = arrayListOf("quote",rst)

    fun lispPrint(obj: Any): String {
        return when (obj) {
            is LispString -> lispData("symbol-name", quote(obj.content))
            is Collection<*> -> obj.joinToString(" ", "(", ")") {
                if (it == null) {
                    ""
                } else {
                    lispPrint(it)
                }
            }
            else -> obj.toString()
        }
    }

    fun lispData(vararg stuff: Any): String = lispPrint(listOf(*stuff))


}
