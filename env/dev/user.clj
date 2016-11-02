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

(def cljs-builds
  (get-in profiles [:dev :cljsbuild :builds]))

(defn start-figwheel
  "Start figwheel for one or more builds"
  [build-ids]
  (ra/start-figwheel!
    {:figwheel-options {:nrepl-port 7888}
     :build-ids        build-ids
     :all-builds       cljs-builds}))

(def start-cljs-repl ra/cljs-repl)

(defn stop-figwheel
  "Stops figwheel"
  []
  (ra/stop-figwheel!))

(hawk/watch! [{:paths   ["resources"]
               :handler (fn [ctx e]
                          (let [path         "src/status_im/utils/js_resources.cljs"
                                js-resourced (slurp path)]
                            (spit path (str js-resourced " ;;"))
                            (spit path js-resourced))
                          ctx)}])

(let [env-build-ids (System/getenv "BUILD_IDS")
      build-ids (if env-build-ids
                  (map keyword (s/split env-build-ids #","))
                  [:android])]
  (start-figwheel build-ids))
