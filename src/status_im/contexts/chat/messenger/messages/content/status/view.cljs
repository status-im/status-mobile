(ns status-im.contexts.chat.messenger.messages.content.status.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.contexts.chat.messenger.messages.content.status.style :as style]
    [utils.i18n :as i18n]))

(defn status
  [outgoing-status]
  (let [theme (quo.theme/use-theme)]
    [rn/view
     {:accessibility-label :message-status
      :style               style/status-container}
     [quo/icon
      (if (= outgoing-status :delivered)
        :i/delivered
        :i/sent)
      {:size  12
       :color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)}]
     [quo/text
      {:size  :label
       :style (style/message-status-text theme)}
      (if (= outgoing-status :delivered)
        (i18n/label :t/delivered)
        (i18n/label :t/sent))]]))
