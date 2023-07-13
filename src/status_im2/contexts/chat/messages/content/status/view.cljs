(ns status-im2.contexts.chat.messages.content.status.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [utils.i18n :as i18n]
            [status-im2.contexts.chat.messages.content.status.style :as style]
            [quo2.core :as quo]))

(defn status
  [outgoing-status]
  [rn/view
   {:accessibility-label :message-status
    :style               style/status-container}
   [quo/icon
    (if (= outgoing-status :delivered)
      :i/delivered
      :i/sent)
    {:size  12
     :color (colors/theme-colors colors/neutral-40 colors/neutral-50)}]
   [quo/text
    {:size  :label
     :style (style/message-status-text)}
    (if (= outgoing-status :delivered)
      (i18n/label :t/delivered)
      (i18n/label :t/sent))]])
