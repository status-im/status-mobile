#!/usr/bin/env bb

(ns lint-translations
  (:require [babashka.pods :as pods]))

(pods/load-pod 'clj-kondo/clj-kondo "2023.09.07")
(require '[pod.borkdude.clj-kondo :as kondo])
(require '[cheshire.core :as json])
(require '[clojure.set :as set])

(def src-paths ["src"])

(def translation-file "translations/en.json")

(defn- safe-name
  [x]
  (when x (name x)))

(defn- ->keyword
  [analysis-keyword]
  (keyword (safe-name (:ns analysis-keyword)) (:name analysis-keyword)))

(defn- report-issues
  [incorrect-usages]
  (doseq [incorrect-usage incorrect-usages]
    (->> incorrect-usage
         ((juxt :filename :row :reason ->keyword))
         (apply format "%s:%s %s %s")
         println)))

(defn- extract-translation-keys
  [file]
  (-> file slurp json/parse-string keys))


(def ^:private possibly-unused-warning
  (format "Possibly Unused Translation Key in %s:" translation-file))

(defn -main
  [& _args]
  (let [result                           (kondo/run!
                                          {:lint   src-paths
                                           :config {:output {:analysis {:keywords true}}}})
        all-keywords                     (get-in result [:analysis :keywords])
        used-translations                (filter (comp (partial = 't) :ns) all-keywords)
        file-translation-keys            (apply sorted-set (extract-translation-keys translation-file))
        missing-translations             (remove (comp file-translation-keys :name) used-translations)
        used-translation-keys            (set (map :name used-translations))
        possibly-unused-translation-keys (set/difference file-translation-keys used-translation-keys)]
    (run! #(println possibly-unused-warning %) possibly-unused-translation-keys)
    (report-issues (map #(assoc % :reason "Undefined Translation Key") missing-translations))
    (if (empty? missing-translations)
      0
      1)))

(when (= *file* (System/getProperty "babashka.file"))
  (->> *command-line-args*
       (apply -main)
       System/exit))
