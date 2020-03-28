(defproject illuminepixels "0.1.0-SNAPSHOT"

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.597"]
                 [org.clojure/core.async "1.0.567"]
                 [ring/ring-core "1.8.0"]
                 [ring/ring-devel "1.8.0"]
                 [ring/ring-servlet "1.8.0"]
                 [ring/ring-defaults "0.3.2"]
                 [org.clojars.rutledgepaulv/websocket-layer "0.1.11"]
                 [org.clojars.rutledgepaulv/websocket-fx "0.1.2"]
                 [com.taoensso/timbre "4.10.0"]
                 [org.slf4j/slf4j-simple "1.7.30"]
                 [com.cognitect/transit-cljs "0.8.256"]
                 [com.vodori/missing "0.1.28"]
                 [juji/editscript "0.4.2"]
                 [quil "3.1.0"]
                 [mount "0.1.16"]
                 [re-frame "0.12.0"]
                 [cljsjs/react "16.13.0-0"]
                 [cljsjs/react-dom "16.13.0-0"]
                 [venantius/glow "0.1.6"]
                 [reagent "0.10.0" :exclusions [cljsjs/react cljsjs/react-dom]]
                 [re-frame "0.12.0" :exclusions [cljsjs/react cljsjs/react-dom]]
                 [markdown-clj "1.10.2"]
                 [hickory "0.7.1"]
                 [clj-jgit "0.8.10"]
                 [metosin/reitit-core "0.4.2"]
                 [metosin/reitit-schema "0.4.2"]
                 [metosin/reitit-frontend "0.4.2"]
                 [garden "1.3.9"]
                 [ns-tracker "0.4.0"]
                 [compojure "1.6.1"]
                 [haslett "0.1.6"]]

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
   {:dependencies [[binaryage/devtools "1.0.0"]
                   [day8.re-frame/re-frame-10x "0.6.0"]
                   [cider/piggieback "0.4.2"]
                   [figwheel-sidecar "0.5.19"]]

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
