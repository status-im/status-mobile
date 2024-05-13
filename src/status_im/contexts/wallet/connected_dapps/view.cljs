(ns status-im.contexts.wallet.connected-dapps.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.connected-dapps.style :as style]
    [utils.re-frame :as rf]))

(defn- header
  [{:keys [title subtitle]}]
  [:<>
   [rn/view {:style style/header-container}
    [quo/button
     {:icon-only?          true
      :type                :grey
      :background          :blur
      :size                32
      :accessibility-label :close-scan-qr-code
      :on-press            #(rf/dispatch [:navigate-back])}
     :i/close]]
   [quo/text
    {:size   :heading-1
     :weight :semi-bold
     :style  (style/header-text (when subtitle true))}
    title]])

(defn view
  []
  [rn/view {:style {:flex 1}}
   [header {:title "Connected dApps"}]])
