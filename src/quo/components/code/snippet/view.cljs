(ns quo.components.code.snippet.view
  (:require
    [quo.components.code.common.view :as code-common]))

(defn view
  [{:keys [language max-lines on-copy-press]} children]
  [code-common/view
   {:language      language
    :max-lines     max-lines
    :on-copy-press on-copy-press
    :preview?      false}
   children])
