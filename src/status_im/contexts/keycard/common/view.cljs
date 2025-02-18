(ns status-im.contexts.keycard.common.view
  (:require [quo.core :as quo]
            [utils.i18n :as i18n]))

(defn tips
  []
  [:<>
   [quo/divider-label (i18n/label :t/tips-scan-keycard)]
   [quo/markdown-list {:description (i18n/label :t/remove-phone-case)}]
   [quo/markdown-list {:description (i18n/label :t/keep-card-steady)}]])
