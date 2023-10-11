(ns hooks.core
  (:require [clj-kondo.hooks-api :as api]))

(defn i18n-label
  "Verify call to `i18n/label` pass the translation keyword qualified with `t`."
  [{:keys [node]}]
  (let [[_ translation-key-node & _] (:children node)]
    (when (and (api/keyword-node? translation-key-node)
               (not= "t" (-> translation-key-node api/sexpr namespace)))
      (api/reg-finding! (assoc (meta translation-key-node)
                               :message "Translation keyword should be qualified with \"t\""
                               :type    :status-im.linter/invalid-translation-keyword)))))

(comment
  ;; Valid
  (i18n-label {:node (api/parse-string "(i18n/label :t/foo)")})

  ;; Invalid
  (i18n-label {:node (api/parse-string "(i18n/label :foo)")}))
