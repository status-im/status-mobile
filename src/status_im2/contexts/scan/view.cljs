(ns status-im2.contexts.scan.view
  (:require [react-native.core :as rn]
            [status-im2.contexts.scan.style :as style]
            [status-im2.common.scan-qr-code.view :as scan-qr-code]
            [utils.re-frame :as rf]
            [react-native.safe-area :as safe-area]
            [quo2.core :as quo]
            [utils.debounce :refer [dispatch-and-chill]]
            [utils.i18n :as i18n]))

(defn header
  []
  [:<>

   [rn/view {:style style/header-container}    
   [quo/button
    {:icon                true
      :type                :blur-bg
      :size                32
      :accessibility-label :close-shell-scan-tab
      :override-theme      :dark
      :on-press            #(rf/dispatch [:navigate-back])}
    :i/close]
    [quo/button
     {:icon                true
      :type                :blur-bg
      :size                32
      :accessibility-label :show-qr-button
      :override-theme      :dark
      :on-press            #(dispatch-and-chill [:open-modal :share-shell] 1000)}
     :i/qr-code]
     ]
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