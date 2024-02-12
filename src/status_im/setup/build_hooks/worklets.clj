(ns status-im.setup.build-hooks.worklets
  (:require [clojure.set])
  (:import (java.io BufferedReader InputStreamReader PrintWriter)))

(defn- update-js-output
  [build-state code-processed]
  (reduce-kv (fn [prev-build-state ns-key new-code]
               (assoc-in prev-build-state [:output ns-key :js] new-code))
             build-state
             code-processed))

(defn- get-workletized-code!
  [code-seq store-atom]
  (doall
   (pmap (fn [[[_ filepath :as ns-key] js-code]]
           (println "Workletizing:" filepath) ;; TODO: debug remove
           (try
             (let [command       "node workletize-code.js"
                   process       (.exec (Runtime/getRuntime) command)
                   output-stream (.getOutputStream process)
                   writer        (PrintWriter. output-stream)
                   reader        (BufferedReader. (InputStreamReader. (.getInputStream process)))]
               (.println writer js-code)
               (.flush writer)
               (.close writer)

               (let [output     (apply str (interleave (line-seq reader) (repeat "\n")))
                     _exit-code (.waitFor process)]
                 (swap! store-atom assoc ns-key output)))

             (catch Exception e
               (println (str "Exception while workletizing: " filepath "\n")
                        (.getMessage e)))))
         code-seq)))

(defn- get-js-code-with-ns
  [build-state-output file-path]
  (let [ns-key  [:shadow.build.classpath/resource file-path]
        js-code (get-in build-state-output [ns-key :js])]
    [ns-key js-code]))

(defn- get-files-to-workletize
  [{:keys [shadow.build/build-info compiler-env] :as _build-state}]
  (let [files-compiled      (->> build-info
                                 :compiled
                                 (map second)
                                 (set))
        namespaces-metadata (->> compiler-env
                                 :cljs.analyzer/namespaces
                                 vals
                                 (map :meta))
        files-marked        (->> namespaces-metadata
                                 (filter :workletize)
                                 (map :file)
                                 (set))]
    (clojure.set/intersection files-compiled files-marked)))

(defn transform-output
  {:shadow.build/stage :compile-finish}
  [{:keys [output] :as build-state}]
  (let [files-to-workletize  (get-files-to-workletize build-state)
        js-code-with-ns      (map #(get-js-code-with-ns output %) files-to-workletize)
        code-processed-store (atom {})]
    (get-workletized-code! js-code-with-ns code-processed-store)
    (update-js-output build-state @code-processed-store)))
