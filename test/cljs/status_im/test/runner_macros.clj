(ns status-im.test.runner-macros
  (:require [cljs.analyzer.api :as ana-api]))

(def cljs-tests-ordered-ns (->> ['status-im.test.utils.async]
                                 reverse
                                 (map-indexed #(vector %2 %))
                                 (into {})))
(def cljs-tests-excluded-ns #{'status-im.test.contacts.events})

(defn cljs-test-ns []
  (->> (ana-api/all-ns)
       (filter #(re-matches #"^status-im\.test\.(?!protocol).*" (name %)))
       (remove cljs-tests-excluded-ns)
       (sort (fn [ns1 ns2]
               (>= (get cljs-tests-ordered-ns ns1 0)
                   (get cljs-tests-ordered-ns ns2 0))))))

(defmacro run-cljs-tests []
  `(cljs.test/run-tests ~@(map (fn [n] `(quote ~n)) (cljs-test-ns))))

(defmacro doo-cljs-tests []
  `(doo.runner/doo-tests ~@(map (fn [n] `(quote ~n)) (cljs-test-ns))))
