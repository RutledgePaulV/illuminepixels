(defproject illuminepixels "0.1.0-SNAPSHOT"

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.439"]
                 [org.clojure/core.async "0.4.490"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-devel "1.7.1"]
                 [ring/ring-servlet "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [org.clojars.rutledgepaulv/websocket-layer "0.1.1"]
                 [org.clojars.rutledgepaulv/websocket-fx "0.1.0-SNAPSHOT"]
                 [com.taoensso/timbre "4.10.0"]
                 [org.slf4j/slf4j-simple "1.7.26"]
                 [com.cognitect/transit-cljs "0.8.256"]
                 [com.vodori/missing "0.1.9"]
                 [juji/editscript "0.3.3"]
                 [quil "2.8.0"]
                 [mount "0.1.16"]
                 [re-frame "0.10.6"]
                 [cljsjs/react "16.8.3-0"]
                 [cljsjs/react-dom "16.8.3-0"]
                 [venantius/glow "0.1.5"]
                 [reagent "0.8.1" :exclusions [cljsjs/react cljsjs/react-dom]]
                 [re-frame "0.10.6" :exclusions [cljsjs/react cljsjs/react-dom]]
                 [markdown-clj "1.0.7"]
                 [hickory "0.7.1"]
                 [clj-jgit "0.8.10"]
                 [metosin/reitit-core "0.3.1"]
                 [metosin/reitit-schema "0.3.1"]
                 [metosin/reitit-frontend "0.3.1"]
                 [garden "1.3.9"]
                 [ns-tracker "0.3.1"]
                 [compojure "1.6.1"]
                 [haslett "0.1.3"]]

  :plugins [[lein-cljsbuild "1.1.7"] [lein-garden "0.3.0"]]

  :source-paths ["src/clj" "src/cljc" "src/cljs"]

  :main illuminepixels.core

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "resources/public/css/compiled"
                                    "target"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :garden
  {:builds
   [{:id           "screen"
     :source-paths ["src/clj"]
     :stylesheet   illuminepixels.css/screen
     :compiler     {:output-to     "resources/public/css/compiled/screen.css"
                    :pretty-print? true}}]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.10"]
                   [day8.re-frame/re-frame-10x "0.4.0"]
                   [cider/piggieback "0.4.0"]
                   [figwheel-sidecar "0.5.18"]]

    :source-paths ["script"]

    :repl-options {:init-ns          user
                   :nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}

   :build
   {:prep-tasks ["compile" ["cljsbuild" "once" "min"] ["garden" "once"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "illuminepixels.core/mount-root"}
     :compiler     {:main                 illuminepixels.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "/js/compiled/out"
                    :source-map-timestamp true
                    :infer-externs        true
                    :preloads             [devtools.preload day8.re-frame-10x.preload]
                    :closure-defines      {goog.DEBUG                                   true
                                           "re_frame.trace.trace_enabled_QMARK_"        true
                                           "day8.re_frame.tracing.trace_enabled_QMARK_" true}
                    :external-config      {:devtools/config {:features-to-install :all}}}}

    {:id           "min"
     :source-paths ["src/cljs"]
     :jar          true
     :compiler     {:main            illuminepixels.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}

    {:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:main          illuminepixels.runner
                    :output-to     "resources/public/js/compiled/test.js"
                    :output-dir    "resources/public/js/compiled/test/out"
                    :optimizations :none}}]})
