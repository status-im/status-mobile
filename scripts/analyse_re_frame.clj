(ns analyse-re-frame
  (:require
   [clj-kondo.core :as clj-kondo]
   [clojure.data.json :as json]))

#_(defn- safe-name [x]
  (when x (name x)))

(defn- analyze-code [paths]
  (clj-kondo/run!
   {:lint paths
    :config
    {:output {:analysis {:keywords true}}
     :hooks  {:analyze-call '{utils.i18n/label hooks.core/i18n-label}}}}))

(defn- filter-on-usage [syms]
  (fn [analysis-keywords]
    (filter (comp syms :reg) analysis-keywords)))

(def get-i18n-label (filter-on-usage #{'i18n/label}))

#_(defn- ->keyword [analysis-keyword]
  (keyword (safe-name (:ns analysis-keyword)) (:name analysis-keyword)))

#_(defn- find-incorrect-usages [reason subset superset]
  (->> subset
       (remove (comp (set (map ->keyword superset)) ->keyword))
       (map #(assoc % :reason reason))))

#_(defn- report-issues [incorrect-usages]
  (doseq [incorrect-usage incorrect-usages]
    (println (format "%s:%s %s %s"
                     (:filename incorrect-usage)
                     (:row incorrect-usage)
                     (:reason incorrect-usage)
                     (->keyword incorrect-usage)))))

(defn extract-translation-keys [file]
  (-> file slurp json/read-str keys))

(defn -main [& _args]
  (let [
        result (analyze-code ["src"])
        ;; result (analyze-code ["src/status_im/notifications"])
        all-keywords (get-in result [:analysis :keywords])
        used-translations (get-i18n-label all-keywords)
        used-translation-keys (set (map :name used-translations))
        file-translation-keys (set (extract-translation-keys "translations/en.json"))
        unused-translation-keys (clojure.set/difference file-translation-keys
                                                        used-translations)
        missing-translation-keys (clojure.set/difference used-translation-keys
                                                         file-translation-keys)
        ]
    ;; used-translation-keys
    ;; (count file-translation-keys)
    ;; (first file-translation-keys)
    ;; missing-translation-keys
    ;; unused-translation-keys
    
    (println)
    (println)
    (println "==================================================")
    (println "UNUSED TRANSLATION KEYS")
    (println)
    (run! println unused-translation-keys)
    (println "==================================================")
    (println)
    (println)
    (println "==================================================")
    (println "MISSING TRANSLATION KEYS")
    (println)
    (run! println missing-translation-keys)
    (println "==================================================")
    
    #_(report-issues
       (find-incorrect-usages "Call to unregistered subscription"
                             (get-used-subscription-keys analysis-keywords)
                             (get-registered-subscription-keys analysis-keywords))))
  )

(comment

  (-main)

  (def result (analyze-code ["src/status_im/notifications"]))
;; => #'analyse-re-frame/result

  (def all-keywords (get-in result [:analysis :keywords]))
  ;; => #'analyse-re-frame/analysis-keywords

  (->> all-keywords
       get-i18n-label
       (map :name))


;; => ({:end-row 58, :ns t, :name "push-inbound-transaction", :filename "src/status_im/notifications/wallet.cljs", :from status-im.notifications.wallet, :col 46, :from-var create-transfer-notification, :reg i18n/label, :end-col 73, :row 58} {:end-row 61, :ns t, :name "push-outbound-transaction", :filename "src/status_im/notifications/wallet.cljs", :from status-im.notifications.wallet, :col 46, :from-var create-transfer-notification, :reg i18n/label, :end-col 74, :row 61} {:end-row 64, :ns t, :name "push-failed-transaction", :filename "src/status_im/notifications/wallet.cljs", :from status-im.notifications.wallet, :col 46, :from-var create-transfer-notification, :reg i18n/label, :end-col 72, :row 64} {:end-row 69, :ns t, :name "push-inbound-transaction-bodyz", :filename "src/status_im/notifications/wallet.cljs", :from status-im.notifications.wallet, :col 46, :from-var create-transfer-notification, :reg i18n/label, :end-col 79, :row 69} {:end-row 72, :ns t, :name "push-outbound-transaction-body", :filename "src/status_im/notifications/wallet.cljs", :from status-im.notifications.wallet, :col 46, :from-var create-transfer-notification, :reg i18n/label, :end-col 79, :row 72} {:end-row 75, :ns t, :name "push-failed-transaction-body", :filename "src/status_im/notifications/wallet.cljs", :from status-im.notifications.wallet, :col 46, :from-var create-transfer-notification, :reg i18n/label, :end-col 77, :row 75})

  (->> analysis-keywords
       (drop 4)
       first)
;; => {:row 6, :col 28, :end-row 6, :end-col 31, :name "as", :filename "src/status_im/notifications/wallet.cljs", :from user}
;; => {:row 5, :col 32, :end-row 5, :end-col 35, :name "as", :filename "src/status_im/notifications/wallet.cljs", :from user}
;; => {:row 4, :col 32, :end-row 4, :end-col 35, :name "as", :filename "src/status_im/notifications/wallet.cljs", :from user}
;; => {:row 3, :col 21, :end-row 3, :end-col 24, :name "as", :filename "src/status_im/notifications/wallet.cljs", :from user}
;; => {:row 2, :col 4, :end-row 2, :end-col 12, :name "require", :filename "src/status_im/notifications/wallet.cljs", :from user}
  
  (->> (:var-usages analysis)
       ;; (filter #(= "src/status_im/notifications/wallet.cljs" (:filename %)))
       ;; (drop 30)
       ;; first
       second
       )
;; => {:fixed-arities #{1 2}, :end-row 19, :name-end-col 10, :name-end-row 19, :name-row 19, :name =, :filename "src/status_im/notifications/wallet.cljs", :from status-im.notifications.wallet, :col 8, :name-col 9, :from-var preference=, :end-col 37, :arity 2, :varargs-min-arity 2, :row 19, :to cljs.core}


  
  ;; => {:fixed-arities #{1 3 2}, :end-row 15, :name-end-col 5, :name-end-row 12, :name-row 12, :name def, :filename "src/status_im/notifications/wallet.cljs", :from status-im.notifications.wallet, :macro true, :col 1, :name-col 2, :end-col 23, :arity 2, :row 12, :to cljs.core}

  ;; => {:fixed-arities #{1 2}, :end-row 19, :name-end-col 10, :name-end-row 19, :name-row 19, :name =, :filename "src/status_im/notifications/wallet.cljs", :from status-im.notifications.wallet, :col 8, :name-col 9, :from-var preference=, :end-col 37, :arity 2, :varargs-min-arity 2, :row 19, :to cljs.core}

  (slurp "translations/en.json")
  
  )
