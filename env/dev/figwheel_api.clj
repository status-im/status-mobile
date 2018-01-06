(ns figwheel-api
  (:use [figwheel-sidecar.repl-api :as ra])
  (:require [hawk.core :as hawk]
            [re-frisk-sidecar.core :as rfs]
            [clojure.string :as s]))

(defn get-test-build [build]
  (update build :source-paths
          (fn [paths] (let [paths-set (set paths)]
                        (-> paths-set
                            (disj "env/dev")
                            (conj "env/test" "test/cljs")
                            vec)))))

(defn start-figwheel
  "Start figwheel for one or more builds"
  [build-ids cljs-builds]
  (ra/start-figwheel!
    {:figwheel-options {:nrepl-port 7888}
     :build-ids        build-ids
     :all-builds       cljs-builds}))

(defn stop-figwheel
  "Stops figwheel"
  []
  (ra/stop-figwheel!))

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
      (assoc
        (let [build (get all-builds (get-id id))]
          (if (test-id? id)
            (get-test-build build)
            build))
        :id id))
    ids))

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
   ;; read project.clj to get build configs
   (let [profiles (->> "project.clj"
                       slurp
                       read-string
                       (drop-while #(not= % :profiles))
                       (apply hash-map)
                       :profiles)
         cljs-builds (get-in profiles [:dev :cljsbuild :builds])
         builds (get-builds build-ids cljs-builds)]
     (start-figwheel build-ids builds)
     (rfs/-main))))

(def stop ra/stop-figwheel!)

(def start-cljs-repl ra/cljs-repl)
