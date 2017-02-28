(ns user
  (:use [figwheel-sidecar.repl-api :as ra])
  (:require [hawk.core :as hawk]
            [clojure.string :as s]))
;; This namespace is loaded automatically by nREPL

;; read project.clj to get build configs
(def profiles (->> "project.clj"
                   slurp
                   read-string
                   (drop-while #(not= % :profiles))
                   (apply hash-map)
                   :profiles))

(defn get-test-build [build]
  (update build :source-paths
          (fn [paths] (let [paths-set (set paths)]
                        (-> paths-set
                            (disj "env/dev")
                            (conj "env/test" "test/cljs")
                            vec)))))

(def cljs-builds
  (get-in profiles [:dev :cljsbuild :builds]))

(def buids-by-id
  (into {} (map (fn [{:keys [id] :as build}] [id build]) cljs-builds)))

(defn start-figwheel
  "Start figwheel for one or more builds"
  [build-ids cljs-builds]
  (ra/start-figwheel!
    {:figwheel-options {:nrepl-port 7888}
     :build-ids        build-ids
     :all-builds       cljs-builds}))

(def start-cljs-repl ra/cljs-repl)

(defn stop-figwheel
  "Stops figwheel"
  []
  (ra/stop-figwheel!))

(hawk/watch! [{:paths   ["resources" "bots"]
               :handler (fn [ctx e]
                          (let [path         "src/status_im/utils/js_resources.cljs"
                                js-resourced (slurp path)]
                            (spit path (str js-resourced " ;;"))
                            (spit path js-resourced))
                          ctx)}])

(defn test-id? [id]
  (s/includes? (name id) "-test"))

(defn get-id [id]
  (-> id
      name
      (s/replace #"-test" "")
      keyword))

(defn get-builds [ids all-builds]
  (keep
    (fn [id]
      (let [build (get all-builds (get-id id))]
        (if (test-id? id)
          (get-test-build build)
          build)))
    ids))

(let [env-build-ids (System/getenv "BUILD_IDS")
      build-ids     (if env-build-ids
                      (map keyword (s/split env-build-ids #","))
                      [:android])
      builds        (get-builds build-ids buids-by-id)]
  (start-figwheel build-ids builds))
