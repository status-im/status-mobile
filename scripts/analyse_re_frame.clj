(ns analyse-re-frame
  (:require
   [clj-kondo.core :as kondo]
   [clojure.data.json :as json]
   [clojure.set :as set]))

(defn- safe-name [x]
  (when x (name x)))

(defn- analyze-code [paths]
  (kondo/run!
   {:lint paths
    :config
    {:output {:analysis {:keywords true}}
     :hooks  {:analyze-call '{utils.i18n/label hooks.core/i18n-label}}}}))

(defn- filter-on-usage [syms]
  (fn [analysis-keywords]
    (filter (comp syms :reg) analysis-keywords)))

(def get-i18n-label (filter-on-usage #{'i18n/label}))

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
  (-> file slurp json/read-str keys))

(def translation-file "translations/en.json")

(defn -main [& _args]
  (let [result (analyze-code ["src"])
        all-keywords (get-in result [:analysis :keywords])
        used-translations (get-i18n-label all-keywords)
        used-translation-keys (set (map :name used-translations))
        file-translation-keys (set (extract-translation-keys translation-file))
        missing-translations (remove (comp file-translation-keys :name) used-translations)
        unused-translation-keys (set/difference file-translation-keys
                                                used-translation-keys)]

    (report-issues (map #(assoc % :reason "Missing Translation Key") missing-translations))
    (run! #(println "Unused Translation Key:" %) unused-translation-keys)))