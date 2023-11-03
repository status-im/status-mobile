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


(def ^:private probably-unused-warning
  (format "Probably unused translation key in %s:" translation-file))

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
        possibly-unused-translation-keys (set/difference file-translation-keys used-translation-keys)
        non-namespaced-translations      (filter
                                          (fn [kw]
                                            (and (not (:ns kw))
                                                 (possibly-unused-translation-keys (:name kw))))
                                          all-keywords)
        unused-translation-keys          (set/difference possibly-unused-translation-keys
                                                         (set (map :name non-namespaced-translations)))]

    ;; uncomment to print a list of all non-namespaced translation keys
    #_(run! #(println "Probably non-namespaced key" %)
            (apply sorted-set (map :name non-namespaced-translations)))

    ;; uncomment to print all potential usages of non-namespaced translation keys
    ;; note that this will list a lot of false positives, e.g. for `:default` or `:bold`
    #_(report-issues (map #(assoc % :reason "Probably Non-namespaced Translation Key")
                          non-namespaced-translations))

    ;; uncomment to print all occurrences of probably unused translation keys
    #_(run! #(println probably-unused-warning %) unused-translation-keys)

    ;; remove this line once the above one is uncommented
    ;; this line is merely kept to prevent unused variable warnings
    (run! (constantly probably-unused-warning) unused-translation-keys)

    (report-issues (map #(assoc % :reason "Undefined Translation Key") missing-translations))

    ;; uncomment more individual tests once we are ready for the script to actually fail on finding
    ;; corresponding occurrences
    (if (and
         (empty? missing-translations)
         #_(empty? possibly-unused-translation-keys)
         #_(empty? unused-translation-keys))
      0
      1)))

(when (= *file* (System/getProperty "babashka.file"))
  (->> *command-line-args*
       (apply -main)
       System/exit))
