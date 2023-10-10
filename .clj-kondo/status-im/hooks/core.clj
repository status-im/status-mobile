(ns hooks.core
  (:require [clj-kondo.hooks-api :as api]))

(defn i18n-label
  "Verify call to `i18n/label` pass the translation keyword qualified with `:t`."
  [{:keys [node]}]
  (when (api/list-node? node)
    (let [[symbol-node translation-key-node & _] (:children node)]
      (when (and (api/token-node? symbol-node)
                 (= "i18n/label" (str symbol-node))
                 (api/keyword-node? translation-key-node)
                 (not= "t" (-> translation-key-node api/sexpr namespace)))
        (let [{:keys [row col end-col]} (meta translation-key-node)]
          (api/reg-finding! {:message "Translation keyword should be qualified with :t"
                             :row     row
                             :col     col
                             :end-col end-col
                             :type    :status-im.linter/invalid-translation-keyword}))))))

(comment
  ;; Valid
  (i18n-label {:node (api/parse-string "(i18n/label :t/foo)")})

  ;; Invalid
  (i18n-label {:node (api/parse-string "(i18n/label :foo)")}))
