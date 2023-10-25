(ns hooks.core
  (:require [clj-kondo.hooks-api :as api]))

(defn i18n-label
  "Verify call to `i18n/label` pass the translation keyword qualified with `t`."
  [{:keys [node
           ;; cljc
           ;; lang
           ;; filename
           ;; config
           ;; ns
           ;; context
           ]}]
  (let [[f translation-key-node & _remainder] (:children node)
        {fsym :value} f]
    #_(println "reg-finding!" translation-key-node)
    (when (and (api/keyword-node? translation-key-node)
               (not= "t" (-> translation-key-node api/sexpr namespace)))
      (api/reg-finding! (assoc (meta translation-key-node)
                               :message "Translation keyword should be qualified with \"t\""
                               :type    :status-im.linter/invalid-translation-keyword)))

    {:node (api/list-node
            (list* (api/reg-keyword! translation-key-node fsym)
                   _remainder))}

    #_(let [[_ event-vec] (:children node)
          [event-key & event-args] (:children event-vec)]
      {:node (hooks-api/list-node
              (list* (hooks-api/reg-keyword! event-key register-as)
                     event-args))})

    ))

(comment

  (i18n-label {:node (api/parse-string
                      "(i18n/label :t/push-failed-transaction-body
                                   {:value amount
                                    :currency (:symbol token)
                                    :to to})")})
;; => {:node <list: (:t/push-failed-transaction-body {:value amount :currency (:symbol token) :to to})>}
;; => {:node <list: (:t/push-failed-transaction-body {:value amount :currency (:symbol token) :to to})>}
;; => {:node <list: (:t/push-failed-transaction-body {:value amount :currency (:symbol token) :to to})>}

;; => [<token: :t/push-failed-transaction-body> (<map: {:value amount :currency (:symbol token) :to to}>)]
;; => (<token: i18n/label> <token: :t/push-failed-transaction-body> <map: {:value amount :currency (:symbol token) :to to}>)

  
  ;; Valid
  (i18n-label {:node (api/parse-string "(i18n/label :t/foo )")
               :cljc false
               :lang :cljs
               :filename ""
               :config {}
               :ns ""
               :context nil
               })

  ;; Invalid
  (i18n-label {:node (api/parse-string "(i18n/label :foo)")}))
