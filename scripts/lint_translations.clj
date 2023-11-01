#!/usr/bin/env bb

(ns lint-translations
  (:require [babashka.pods :as pods]))

(pods/load-pod 'clj-kondo/clj-kondo "2023.09.07")
(require '[pod.borkdude.clj-kondo :as kondo])
(require '[cheshire.core :as json])
(require '[clojure.set :as set])

(defn- safe-name [x]
  (when x (name x)))

(defn- analyze-code [paths]
  (kondo/run!
   {:lint paths
    :config {:output {:analysis {:keywords true}}}}))

(defn- filter-on-usage [syms]
  (fn [analysis-keywords]
    (filter (comp syms :reg) analysis-keywords)))

(def get-i18n-label (filter-on-usage #{'utils.i18n/label}))

(defn- ->keyword [analysis-keyword]
  (keyword (safe-name (:ns analysis-keyword)) (:name analysis-keyword)))

(defn- report-issues [incorrect-usages]
  (doseq [incorrect-usage incorrect-usages]
    (println (format "%s:%s %s %s"
                     (:filename incorrect-usage)
                     (:row incorrect-usage)
                     (:reason incorrect-usage)
                     (->keyword incorrect-usage)))))

(defn extract-translation-keys [file]
  (-> file slurp json/parse-string keys))

(def translation-file "translations/en.json")

;; (def unused-warning (format "Unused Translation Key in %s:" translation-file))

(def possibly-unused-warning (format "Possibly Unused Translation Key in %s:" translation-file))

(defn -main [& _args]
  (let [result (analyze-code ["src"])
        all-keywords (get-in result [:analysis :keywords])
        used-translations (filter (comp (partial = 't) :ns) all-keywords)
        file-translation-keys (set (extract-translation-keys translation-file))
        missing-translations (remove (comp file-translation-keys :name) used-translations)
        used-translation-keys (set (map :name used-translations))
        possibly-unused-translation-keys (set/difference file-translation-keys used-translation-keys)

        ;; 
        ;; non-namespaced-translations (filter
        ;;                              (fn [kw] (and (not (:ns kw))
        ;;                                           (possibly-unused-translation-keys (:name kw))))
        ;;                              all-keywords)
        ;; unused-translation-keys (set/difference possibly-unused-translation-keys
        ;;                                         (set (map :name non-namespaced-translations)))
        ]

    (report-issues (map #(assoc % :reason "Undefined Translation Key") missing-translations))
    ;; (report-issues (map #(assoc % :reason "Non-namespaced Translation Key") non-namespaced-translations))
    ;; (run! #(println unused-warning %) unused-translation-keys)
    (run! #(println possibly-unused-warning %) possibly-unused-translation-keys)
    (if (and (empty? missing-translations)
             #_(empty? possibly-unused-translation-keys))
      0
      1)))

(when (= *file* (System/getProperty "babashka.file"))
  (->> *command-line-args*
       (apply -main)
       System/exit))
