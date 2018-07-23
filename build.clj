(require '[cljs.build.api :as api]
         '[clojure.string :as str])

;; clj build.clj help # Prints details about tasks

;;; Configuration.

(def cljsbuild-config
  {:dev
   {:ios
    {:source-paths     ["components/src" "react-native/src/cljsjs" "react-native/src/mobile" "src"]
     :compiler         {:output-to     "target/ios/app.js"
                        :main          "env.ios.main"
                        :output-dir    "target/ios"
                        :npm-deps false
                        :optimizations :none}
     :warning-handlers '[status-im.utils.build/warning-handler]}
    :android
    {:source-paths     ["components/src" "react-native/src/cljsjs" "react-native/src/mobile" "src"]
     :compiler         {:output-to     "target/android/app.js"
                        :main          "env.android.main"
                        :output-dir    "target/android"
                        :npm-deps false
                        :optimizations :none}
     :warning-handlers '[status-im.utils.build/warning-handler]}}

   :prod
   {:ios
    {:source-paths     ["components/src" "react-native/src/cljsjs" "react-native/src/mobile" "src" "env/prod"]
     :compiler         {:output-to          "index.ios.js"
                        :output-dir         "target/ios-prod"
                        :static-fns         true
                        :optimize-constants true
                        :optimizations      :simple
                        :closure-defines    {"goog.DEBUG" false}
                        :parallel-build     false
                        :elide-asserts      true
                        :language-in        :ecmascript5}
     :warning-handlers '[status-im.utils.build/warning-handler]}
    :android
    {:source-paths     ["components/src" "react-native/src/cljsjs" "react-native/src/mobile" "src" "env/prod"]
     :compiler         {:output-to          "index.android.js"
                        :output-dir         "target/android-prod"
                        :static-fns         true
                        :optimize-constants true
                        :optimizations      :simple
                        :closure-defines    {"goog.DEBUG" false}
                        :parallel-build     false
                        :elide-asserts      true
                        :language-in        :ecmascript5}
     :warning-handlers '[status-im.utils.build/warning-handler]}}

   :test
   {:test
    {:source-paths ["src" "test/cljs"]
     :compiler     {:main          "status-im.test.runner"
                    :output-to     "target/test/test.js"
                    :output-dir    "target/test"
                    :optimizations :none
                    :preamble      ["js/hook-require.js"]
                    :target        :nodejs}}
    :protocol
    {:source-paths ["src" "test/cljs"]
     :compiler     {:main          "status-im.test.protocol.runner"
                    :output-to     "target/test/test.js"
                    :output-dir    "target/test"
                    :optimizations :none
                    :target        :nodejs}}
    :env-dev-utils
    {:source-paths ["env/dev/env/utils.cljs" "test/env/dev"]
     :compiler     {:main          "env.test.runner"
                    :output-to     "target/test/test.js"
                    :output-dir    "target/test"
                    :optimizations :none
                    :target        :nodejs}}}})

(def cli-tasks-info
  {:compile  {:desc  "Compile ClojureScript"
              :usage ["Usage: clj build.clj compile [env] [build-id] [type]"
                      ""
                      "[env] (required): Pre-defined build environment. Allowed values: \"dev\", \"prod\", \"test\""
                      "[build-id] (optional): Build ID. When omitted, this task will compile all builds from the specified [env]."
                      "[type] (optional): Build type - value could be \"once\" or \"watch\". Default: \"once\"."]}
   :figwheel {:desc  "Start figwheel + CLJS REPL / nREPL"
              :usage ["Usage: clj -R:repl build.clj figwheel [options]"
                      ""
                      "[-h|--help] to see all available options"]}
   :test     {:desc  "Run tests"
              :usage ["Usage: clj -R:test build.clj test [build-id]"
                      ""
                      "[build-id] (required): Value could be \"test\", \"protocol\" or \"env-dev-utils\". It will compile then run the tests once."]}
   :help     {:desc "Show this help"}})

;;; Helper functions.

(def reset-color "\u001b[0m")
(def red-color "\u001b[31m")
(def green-color "\u001b[32m")
(def yellow-color "\u001b[33m")

(defn- colorizer [c]
  (fn [& args]
    (str c (apply str args) reset-color)))

(defn- println-colorized [message color]
  (println ((colorizer color) message)))

(defn- elapsed [started-at]
  (let [elapsed-us (- (System/currentTimeMillis) started-at)]
    (with-precision 2
      (str (/ (double elapsed-us) 1000) " seconds"))))

(defn- try-require [ns-sym]
  (try
    (require ns-sym)
    true
    (catch Exception e
      false)))

(defmacro with-namespaces [namespaces & body]
  (if (every? try-require namespaces)
    `(do ~@body)
    `(do (println-colorized "task not available - required dependencies not found" red-color)
         (System/exit 1))))

(defn- get-cljsbuild-config [name-env & [name-build-id]]
  (try
    (let [env (keyword name-env)]
      (when-not (contains? cljsbuild-config env)
        (throw (Exception. (str "ENV " (pr-str name-env) " does not exist"))))
      (let [env-config (get cljsbuild-config env)]
        (if name-build-id
          (let [build-id (keyword name-build-id)]
            (when-not (contains? env-config build-id)
              (throw (Exception. (str "Build ID " (pr-str name-build-id) " does not exist"))))
            (get env-config build-id))
          env-config)))
    (catch Exception e
      (println-colorized (.getMessage e) red-color)
      (System/exit 1))))

(defn- get-output-files [compiler-options]
  (if-let [output-file (:output-to compiler-options)]
    [output-file]
    (into [] (map :output-to (->> compiler-options :modules vals)))))

(defn- compile-cljs-with-build-config [build-config build-fn env build-id]
  (let [{:keys [source-paths compiler]} build-config
        output-files                    (get-output-files compiler)
        started-at                      (System/currentTimeMillis)]
    (println (str "Compiling " (pr-str build-id) " for " (pr-str env) "..."))
    (flush)
    (build-fn (apply api/inputs source-paths) compiler)
    (println-colorized (str "Successfully compiled " (pr-str output-files) " in " (elapsed started-at) ".") green-color)))

(defn- compile-cljs [env & [build-id watch?]]
  (let [build-fn (if watch? api/watch api/build)]
    (if build-id
      (compile-cljs-with-build-config (get-cljsbuild-config env build-id) build-fn env build-id)
      (doseq [[build-id build-config] (get-cljsbuild-config env)]
        (compile-cljs-with-build-config (get-cljsbuild-config env build-id) build-fn env build-id)))))

(defn- show-help []
  (doseq [[task {:keys [desc usage]}] cli-tasks-info]
    (println (format (str yellow-color "%-12s" reset-color green-color "%s" reset-color)
                     (name task) desc))
    (when usage
      (println)
      (->> usage
           (map #(str "  " %))
           (str/join "\n")
           println)
      (println))))

;;; Task dispatching

(defmulti task first)

(defmethod task :default [args]
  (println (format "Unknown or missing task. Choose one of: %s\n"
                   (->> cli-tasks-info
                        keys
                        (map name)
                        (interpose ", ")
                        (apply str))))
  (show-help)
  (System/exit 1))

;;; Compiling task

(defmethod task "compile" [[_ env build-id type]]
  (case type
    (nil "once") (compile-cljs env build-id)
    "watch"      (compile-cljs env build-id true)
    (do (println "Unknown argument to compile task:" type)
        (System/exit 1))))

;;; Testing task

(defmethod task "test" [[_ build-id]]
  (with-namespaces [[doo.core :as doo]]
    (compile-cljs :test build-id)
    (doo/run-script :node (->> build-id (get-cljsbuild-config :test) :compiler))))

;;; Figwheeling task

(def figwheel-cli-opts
  [["-p" "--platform BUILD-IDS" "CLJS Build IDs for platforms <android|ios>"
    :id       :build-ids
    :default  [:android]
    :parse-fn #(->> (.split % ",")
                    (map (comp keyword str/lower-case str/trim))
                    vec)
    :validate [(fn [build-ids] (every? #(some? (#{:android :ios} %)) build-ids)) "Allowed \"android\", and/or \"ios\""]]
   ["-n" "--nrepl-port PORT" "nREPL Port"
    :id       :port
    :parse-fn #(if (string? %) (Integer/parseInt %) %)
    :validate [#(or (true? %) (< 0 % 0x10000)) "Must be a number between 0 and 65536"]]
   ["-a" "--android-device TYPE" "Android Device Type <avd|genymotion|real>"
    :id       :android-device
    :parse-fn #(keyword (str/lower-case %))
    :validate [#(some? (#{:avd :genymotion :real} %)) "Must be \"avd\", \"genymotion\", or \"real\""]]
   ["-i" "--ios-device TYPE" "iOS Device Type <simulator|real>"
    :id       :ios-device
    :parse-fn #(keyword (str/lower-case %))
    :validate [#(some? (#{:simulator :real} %)) "Must be \"simulator\", or \"real\""]]
   ["-h" "--help"]])

(defn print-and-exit [msg]
  (println msg)
  (System/exit 1))

(defn parse-figwheel-cli-opts [args]
  (with-namespaces [[clojure.tools.cli :as cli]]
    (let [{:keys [options errors summary]} (cli/parse-opts args figwheel-cli-opts)]
      (cond
        (:help options)     (print-and-exit summary)
        (not (nil? errors)) (print-and-exit errors)
        :else               options))))

(defmethod task "figwheel" [[_ & args]]
  (with-namespaces [[figwheel-sidecar.repl-api :as ra]
                    [hawk.core :as hawk]
                    [re-frisk-sidecar.core :as rfs]
                    [clj-rn.core :as clj-rn]
                    [clj-rn.main :refer [get-main-config] :rename {get-main-config get-cljrn-config}]]
    (let [{:keys [build-ids
                  port
                  android-device
                  ios-device]} (parse-figwheel-cli-opts args)
          hosts-map            {:android (clj-rn/resolve-dev-host :android android-device)
                                :ios     (clj-rn/resolve-dev-host :ios ios-device)}]
      (clj-rn/enable-source-maps)
      (clj-rn/write-env-dev hosts-map)
      (doseq [build-id build-ids
              :let     [host-ip                 (get hosts-map build-id)
                        platform-name           (if (= build-id :ios) "iOS" "Android")
                        {:keys [js-modules
                                name
                                resource-dirs]} (get-cljrn-config)]]
        (clj-rn/rebuild-index-js build-id {:app-name      name
                                           :host-ip       host-ip
                                           :js-modules    js-modules
                                           :resource-dirs resource-dirs})
        (when (= build-id :ios)
          (clj-rn/update-ios-rct-web-socket-executor host-ip)
          (println-colorized "Host in RCTWebSocketExecutor.m was updated" green-color))
        (println-colorized (format "Dev server host for %s: %s" platform-name host-ip) green-color))
      (ra/start-figwheel!
       {:figwheel-options (cond-> {:builds-to-start build-ids}
                            port (merge {:nrepl-port       port
                                         :nrepl-middleware ["cider.nrepl/cider-middleware"
                                                            "refactor-nrepl.middleware/wrap-refactor"
                                                            "cemerick.piggieback/wrap-cljs-repl"]}))
        :all-builds       (into [] (for [[build-id {:keys [source-paths compiler warning-handlers]}]
                                         (get-cljsbuild-config :dev)]
                                     {:id           build-id
                                      :source-paths (conj source-paths "env/dev")
                                      :compiler     compiler
                                      :figwheel     true}))})
      (rfs/-main)
      (if-not port
        (ra/cljs-repl)
        (spit ".nrepl-port" port)))))

;;; Help

(defmethod task "help" [_]
  (show-help)
  (System/exit 1))

;;; Build script entrypoint.

(task *command-line-args*)
