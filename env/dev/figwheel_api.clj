(ns figwheel-api
  (:use [figwheel-sidecar.repl-api :as ra])
  (:require [hawk.core :as hawk]
            [re-frisk-sidecar.core :as rfs]
            [status-im.utils.core :as utils]
            [figwheel :as config]))

(defn start-figwheel
  "Start figwheel for one or more builds"
  [build-ids]
  (ra/start-figwheel! (config/system-options build-ids)))

(defn stop-figwheel
  "Stops figwheel"
  []
  (ra/stop-figwheel!))

(defn start
  ([]
   (start (if *command-line-args*
            (map keyword *command-line-args*)
            [:android])))
  ([build-ids]
   (hawk/watch! [{:paths   ["resources"]
                  :handler (fn [ctx e]
                             (let [path "src/status_im/utils/js_resources.cljs"
                                   js-resourced (slurp path)]
                               (spit path (str js-resourced " ;;"))
                               (spit path js-resourced))
                             ctx)}])
   (start-figwheel build-ids)
   (rfs/-main)))

(def stop ra/stop-figwheel!)

(def start-cljs-repl ra/cljs-repl)
