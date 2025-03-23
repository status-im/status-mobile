#!/usr/bin/env bb

(ns lint-translations
  (:require [babashka.pods :as pods]))

(pods/load-pod 'clj-kondo/clj-kondo "2023.09.07")
(require '[pod.borkdude.clj-kondo :as kondo])
(require '[cheshire.core :as json])
(require '[clojure.set :as set])

(def src-paths ["src"])

(def translation-file "translations/en.json")

;; set the following to true when solving https://github.com/status-im/status-mobile/issues/17811
(def flag-show-non-namespaced-translation-keys false)
(def flag-show-non-namespaced-translation-keys-occurrences false) ;; this makes the output super verbose!

;; set the following to true when solving https://github.com/status-im/status-mobile/issues/17813
;; and keep it permanently on after #17811 and #17813 have both been solved
(def flag-show-unused-translation-keys false)

(def flag-show-missing-translation-keys true)

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

(def ^:private probably-unused-warning
  (format "Probably unused translation key in %s:" translation-file))

(defn -main
  [& _args]
  (println "Linting translations...")
  (let [result                           (kondo/run!
                                          {:lint   src-paths
                                           :config {:output {:analysis {:keywords true}}}})
        all-keywords                     (get-in result [:analysis :keywords])
        used-translations                (filter (comp (partial = 't) :ns) all-keywords)
        file-translation-keys            (apply sorted-set (extract-translation-keys translation-file))
        missing-translations             (remove (comp file-translation-keys :name) used-translations)
        used-translation-keys            (set (map :name used-translations))
        possibly-unused-translation-keys (set/difference file-translation-keys used-translation-keys)
        non-namespaced-translations      (filter
                                          (fn [kw]
                                            (and (not (:ns kw))
                                                 (possibly-unused-translation-keys (:name kw))))
                                          all-keywords)
        unused-translation-keys          (set/difference possibly-unused-translation-keys
                                                         (set (map :name non-namespaced-translations)))]

    ;; TODO (2023-11-06 akatov): delete the following once #17811 and #17813 have both been solved
    (doseq [k (apply sorted-set (map :name non-namespaced-translations))]
      (when flag-show-non-namespaced-translation-keys
        (println "Probably non-namespaced key" k))
      (when flag-show-non-namespaced-translation-keys-occurrences
        (->> non-namespaced-translations
             (filter #(= k (:name %)))
             (map #(assoc % :reason "Possibly non-namespaced translation key"))
             report-issues)))

    (when flag-show-unused-translation-keys
      (run! #(println probably-unused-warning %) unused-translation-keys))

    (when flag-show-missing-translation-keys
      (report-issues (map #(assoc % :reason "Undefined Translation Key") missing-translations)))

    (if (and
         (or (not flag-show-missing-translation-keys)
             (empty? missing-translations))
         (or (not flag-show-unused-translation-keys)
             (empty? possibly-unused-translation-keys))
         (or (not flag-show-non-namespaced-translation-keys)
             (not flag-show-non-namespaced-translation-keys-occurrences)
             (empty? unused-translation-keys)))
      0
      1)))

(when (= *file* (System/getProperty "babashka.file"))
  (->> *command-line-args*
       (apply -main)
       System/exit))
