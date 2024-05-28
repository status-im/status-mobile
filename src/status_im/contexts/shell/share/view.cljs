(ns status-im.contexts.shell.share.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.contexts.shell.share.profile.view :as profile-view]
    [status-im.contexts.shell.share.style :as style]
    [status-im.contexts.shell.share.wallet.view :as wallet-view]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn header
  []
  [:<>
   [rn/view {:style style/header-row}
    [quo/button
     {:icon-only?          true
      :type                :grey
      :background          :blur
      :size                32
      :accessibility-label :close-shell-share-tab
      :container-style     style/header-button
      :on-press            #(rf/dispatch [:navigate-back])}
     :i/close]
    [quo/button
     {:icon-only?          true
      :type                :grey
      :background          :blur
      :size                32
      :accessibility-label :shell-scan-button
      :on-press            (fn []
                             (rf/dispatch [:navigate-back])
                             (rf/dispatch [:open-modal :shell-qr-reader]))}
     :i/scan]]
   [quo/text
    {:size   :heading-1
     :weight :semi-bold
     :style  style/header-heading}
    (i18n/label :t/share)]])

(defn tab-content
  []
  (let [{:keys [initial-tab] :or {initial-tab :profile}} (rf/sub [:get-screen-params])
        selected-tab                                     (reagent/atom initial-tab)]
    (fn []
      [rn/view {:style {:padding-top (safe-area/get-top)}}
       [header]
       [rn/view {:style style/tabs-container}
        [quo/segmented-control
         {:size           28
          :blur?          true
          :on-change      #(reset! selected-tab %)
          :default-active initial-tab
          :data           [{:id    :profile
                            :label (i18n/label :t/profile)}
                           {:id    :wallet
                            :label (i18n/label :t/wallet)}]}]]
       (if (= @selected-tab :profile)
         [profile-view/profile-tab]
         [wallet-view/wallet-tab])])))

(defn view
  []
  [quo/overlay {:type :shell}
   [rn/view
    {:key :share}
    [tab-content]]])
