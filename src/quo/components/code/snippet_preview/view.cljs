(ns quo.components.code.snippet-preview.view
  (:require
    [quo.components.code.common.view :as code-common]))

(defn view
  [{:keys [language]} children]
  [code-common/view
   {:preview?  true
    :language  language
    :max-lines 0
    :theme     :dark}
   children])
