package com.github.benjaminasdf.ideaidle.listeners
import clojure.java.api.Clojure

object LoadClojure {

    fun LoadClojure(f: () -> Unit) {
        val current = Thread.currentThread().contextClassLoader
        try {
            Thread.currentThread().contextClassLoader = this.javaClass.classLoader
            f()
        } catch (e: Exception)  {
            println("ideaidle failed to load with exception:")
            println(e)
        } finally {
            Thread.currentThread().contextClassLoader = current
        }
    }

    fun LoadClojureNs(ns: String) {
        LoadClojure {
            val require = Clojure.`var`("clojure.core", "require")
            require.invoke(Clojure.read(ns));
        }
    }

}
