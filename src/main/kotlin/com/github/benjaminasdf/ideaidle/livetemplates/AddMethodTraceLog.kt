package com.github.benjaminasdf.ideaidle.livetemplates

import com.intellij.codeInsight.template.*
import com.intellij.codeInsight.template.macro.MacroBase
import com.intellij.openapi.util.text.StringUtil

class AddMethodTraceLog : MacroBase {
    constructor() : super("methodTraceLog", "bestDescr") {}

    /**
     * Strictly to uphold contract for constructors in base class.
     */
    private constructor(name: String, description: String) : super(name, description) {}

    override fun calculateResult(params: Array<Expression>, context: ExpressionContext, quick: Boolean): Result? {
        println("calc result")
        return TextResult("foo")
    }

    override fun isAcceptableInContext(context: TemplateContextType): Boolean {
        println(context)
        return true
    }


}