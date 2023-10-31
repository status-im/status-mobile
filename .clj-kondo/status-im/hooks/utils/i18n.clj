(ns hooks.utils.i18n
  (:require [clj-kondo.hooks-api :as api]))

(defn label
  "Verify call to `utils.i18n/label` pass the translation keyword qualified with `t`.
   Returns the node with the keyword marked as a definition and `utils.i18n/label` as
   the function that registered it."
  [{:keys [node] :as expr}]
  (let [[f translation-key-node & remainder] (:children node)]
    (when (and (api/keyword-node? translation-key-node)
               (not= "t" (-> translation-key-node api/sexpr namespace)))
      (api/reg-finding! (assoc (meta translation-key-node)
                               :message "Translation keyword should be qualified with \"t\""
                               :type    :status-im.linter/invalid-translation-keyword)))
    (assoc expr :node
           (api/list-node
            (list* f (api/reg-keyword! translation-key-node 'utils.i18n/label)
                   remainder)))))

(comment
  ;; Valid
  (label {:node (api/parse-string "(i18n/label :t/foo
                                                    {:var \"hello\"}) )")
          :cljc false
          :lang :cljs
          :filename ""
          :config {}
          :ns ""
          :context nil})

  ;; Invalid
  (label {:node (api/parse-string "(i18n/label :foo)")})
  )
