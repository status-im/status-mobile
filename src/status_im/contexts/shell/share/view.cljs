(ns status-im.contexts.shell.share.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.shell.share.profile.view :as profile-view]
    [status-im.contexts.shell.share.style :as style]
    [status-im.contexts.shell.share.wallet.view :as wallet-view]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- header
  []
  (let [{:keys [status]} (quo.theme/use-screen-params)]
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
      (if (= :receive status)
        (i18n/label :t/receive)
        (i18n/label :t/share))]]))

(defn- tab-content
  []
  (let [{:keys [initial-tab hide-tab-selector?]
         :or   {initial-tab        :profile
                hide-tab-selector? false}} (quo.theme/use-screen-params)
        [selected-tab set-selected-tab]    (rn/use-state initial-tab)]
    [rn/view {:style {:padding-top (safe-area/get-top)}}
     [header]
     (when-not hide-tab-selector?
       [rn/view {:style style/tabs-container}
        [quo/segmented-control
         {:size           28
          :blur?          true
          :on-change      set-selected-tab
          :default-active selected-tab
          :data           [{:id    :profile
                            :label (i18n/label :t/profile)}
                           {:id    :wallet
                            :label (i18n/label :t/wallet)}]}]])
     (if (= selected-tab :wallet)
       [wallet-view/wallet-tab]
       [profile-view/profile-tab])]))

(defn view
  []
  [quo/overlay {:type :shell}
   [rn/view {:key :share}
    [tab-content]]])
