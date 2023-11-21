(ns utils.i18n
  (:require [clj-kondo.hooks-api :as hooks]))

(defn label
  "Verify call to `utils.i18n/label` pass the translation keyword qualified with `t`."
  [{:keys [node]}]
  (let [[_ translation-key-node & _] (:children node)]
    (when (and (hooks/keyword-node? translation-key-node)
               (not= "t" (-> translation-key-node hooks/sexpr namespace)))
      (hooks/reg-finding! (assoc (meta translation-key-node)
                                 :message "Translation keyword should be qualified with \"t\""
                                 :type    :status-im.linter/invalid-translation-keyword)))))

(comment
  ;; Valid
  (label {:node     (hooks/parse-string "(i18n/label :t/foo {:var \"hello\"})")
          :cljc     false
          :lang     :cljs
          :filename ""
          :config   {}
          :ns       ""
          :context  nil})

  ;; Invalid
  (label {:node (hooks/parse-string "(i18n/label :foo)")})
)
