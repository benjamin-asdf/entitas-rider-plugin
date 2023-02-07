package com.github.benjaminasdf.ideaidle.services
import clojure.java.api.Clojure
import com.intellij.openapi.project.Project
import com.github.benjaminasdf.ideaidle.listeners.LoadClojure

class MyProjectService(project: Project) {

    var initialized = false
    init {
        LoadClojure.LoadClojure {
            val require = Clojure.`var`("clojure.core", "require")
            val ns = "com.github.benjaminasdf.idlelib.core";
            require.invoke(Clojure.read(ns));
            val initP = Clojure.`var`(ns,Clojure.read("initProj"))
            initP.invoke(project)

            System.getenv("IDEAIDLE_REPL_PORT")?.toInt()?.let { port ->
                require.invoke(Clojure.read("nrepl.server"))
                //require.invoke(Clojure.read("cider.nrepl"))
                val start = Clojure.`var`("nrepl.server", "start-server")
                // val ciderMiddleware = Clojure.`var`("cider.nrepl", "cider-middleware")
                // val ciderHandler = Clojure.`var`("cider.nrepl", "cider-nrepl-handler")
                //require.invoke(Clojure.read("cider.nrepl"))

                start.invoke(Clojure.read(":port"),
                    Clojure.read(Integer.toString(port))
                    // Clojure.read(":handler"), ciderHandler,
                    // Clojure.read(":middleware"), ciderMiddleware,
                )
                println("nrepl server started on port $port")
                // println(System.getProperty("orchard.use-dynapath"))
            }

            //val middleware = Clojure.`var`("cider.nrepl", "cider")
            initialized = true
        }
    }

}
