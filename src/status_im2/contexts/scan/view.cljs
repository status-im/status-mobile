(ns status-im2.contexts.scan.view
  (:require [react-native.core :as rn]
            [status-im2.contexts.scan.style :as style]
            [status-im2.common.scan-qr-code.view :as scan-qr-code]
            [utils.re-frame :as rf]
            [react-native.safe-area :as safe-area]
            [quo2.core :as quo]
            [utils.i18n :as i18n]))

(defn header
  []
  [:<>
   [quo/button
    {:icon                true
      :type                :blur-bg
      :size                32
      :accessibility-label :close-sign-in-by-syncing
      :override-theme      :dark
      :style               style/header-button
      :on-press            #(rf/dispatch [:navigate-back])}
    :i/close]
   [quo/text
    {:size   :heading-1
     :weight :semi-bold
     :style  style/header-text}
    (i18n/label :t/scan-qr-text)]])

(defn view
  []
  (let [insets         (safe-area/get-insets)]
  [:<>
  [:f> scan-qr-code/view header]]
   ))