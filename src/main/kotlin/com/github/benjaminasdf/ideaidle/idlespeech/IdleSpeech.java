package com.github.benjaminasdf.ideaidle.idlespeech;

import com.intellij.lang.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * WARNINGS:
 * - This file MUST NOT be converted to Kotlin!
 * - DO NOT replace <code>String NAME = "Julia"</code> with
 * <code>String NAME = JuliaBundle.message("julia.name")</code>
 * but static import JULIA_LANGUAGE_NAME.
 * <p>
 * ERRORS:
 * - Tests will be failed.
 * - LanguageType `language="Julia"` in plugin.xml will become red.
 *
 * @author zxj5470
 */

public class IdleSpeech extends  Language {

    public static final @NotNull IdleSpeech INSTANCE = new IdleSpeech();

    private IdleSpeech() {
        super("IdleSpeec", "text/" + ".bestidl");
    }

    @Override @Contract(pure = true) public boolean isCaseSensitive() {
        return false;
    }


}


