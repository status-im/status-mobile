(ns user
    (:use [figwheel-sidecar.repl-api :as ra]))
;; This namespace is loaded automatically by nREPL

;; read project.clj to get build configs
(def profiles (->> "project.clj"
                   slurp
                   read-string
                   (drop-while #(not= % :profiles))
                   (apply hash-map)
                   :profiles))

(def cljs-builds (get-in profiles [:dev :cljsbuild :builds]))

(defn start-figwheel
      "Start figwheel for one or more builds"
      [& build-ids]
      (ra/start-figwheel!
        {:build-ids  build-ids
         :all-builds cljs-builds})
      (ra/cljs-repl))

(defn stop-figwheel
      "Stops figwheel"
      []
      (ra/stop-figwheel!))