(ns quo2.components.code.snippet.view
  (:require [quo2.theme :as theme]
            [quo2.components.code.common.view :as code-common]))

(defn- view-internal
  [_]
  (fn [{:keys [language max-lines on-copy-press]} children]
    [code-common/view
     {:language      language
      :max-lines     max-lines
      :on-copy-press on-copy-press
      :preview?      false}
     children]))

(def view (theme/with-theme view-internal))
