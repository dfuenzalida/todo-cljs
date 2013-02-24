(defproject todo-cljs "0.1.0-SNAPSHOT"
  :description "A port of the Todos JavaScript app to ClojureScript"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :plugins [[lein-cljsbuild "0.3.0"]]

  ;; cljsbuild configuration based on https://github.com/magomimmo/modern-cljs/
  :cljsbuild
  {:builds
   [{:source-paths ["src/cljs"],
     :id "dev",
     :compiler
     {:pretty-print true,
      :output-to "resources/public/js/todos.js",
      :optimizations :whitespace}}
    {:source-paths ["src/cljs"],
     :id "prod",
     :compiler
     {:output-to "resources/public/js/todos_prod.js",
      :optimizations :advanced}}]}
  )
