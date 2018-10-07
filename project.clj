(defproject illuminepixels "0.1.0-SNAPSHOT"

  :dependencies [[org.clojure/clojure "1.10.0-alpha9"]
                 [org.clojure/clojurescript "1.10.339"]
                 [org.clojure/core.async "0.4.474"]
                 [ring/ring-core "1.7.0"]
                 [ring/ring-devel "1.7.0"]
                 [ring/ring-servlet "1.7.0"]
                 [ring/ring-defaults "0.3.2"]
                 [com.taoensso/timbre "4.10.0"]
                 [info.sunng/ring-jetty9-adapter "0.11.4"]
                 [com.vodori/missing "0.1.0"]
                 [reagent "0.8.1"]
                 [re-frame "0.10.6"]
                 [secretary "1.2.3"]
                 [garden "1.3.6"]
                 [ns-tracker "0.3.1"]
                 [compojure "1.6.1"]
                 [haslett "0.1.2"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-garden "0.2.8"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs"]

  :main illuminepixels.core

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"
                                    "resources/public/css"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :garden {:builds [{:id           "screen"
                     :source-paths ["src/clj"]
                     :stylesheet   illuminepixels.css/screen
                     :compiler     {:output-to     "resources/public/css/screen.css"
                                    :pretty-print? true}}]}

  :profiles
  {:dev     {:dependencies [[binaryage/devtools "0.9.10"]
                            [day8.re-frame/re-frame-10x "0.3.3"]
                            [day8.re-frame/tracing "0.5.1"]]
             :plugins      [[lein-figwheel "0.5.16"] [lein-doo "0.1.8"]]}

   :prod    {:dependencies [[day8.re-frame/tracing-stubs "0.5.1"]]}

   :uberjar {:source-paths ["env/prod/clj"]
             :dependencies [[day8.re-frame/tracing-stubs "0.5.1"]]
             :omit-source  true
             :main         illuminepixels.core
             :aot          [illuminepixels.core]
             :uberjar-name "illuminepixels.jar"
             :prep-tasks   ["compile" ["cljsbuild" "once" "min"] ["garden" "once"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "illuminepixels.core/mount-root"}
     :compiler     {:main                 illuminepixels.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload day8.re-frame-10x.preload]
                    :closure-defines      {"re_frame.trace.trace_enabled_QMARK_"        true
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
