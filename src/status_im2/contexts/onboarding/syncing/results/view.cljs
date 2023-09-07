(ns status-im2.contexts.onboarding.syncing.results.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [utils.re-frame :as rf]
            [status-im2.contexts.onboarding.syncing.results.style :as style]
            [status-im2.contexts.syncing.device.view :as device]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [react-native.reanimated :as reanimated]
            [status-im2.constants :as constants]))

(defn page-title
  []
  [quo/text-combinations
   {:container-style                 {:margin-horizontal 20}
    :title                           (i18n/label :t/sync-devices-complete-title)
    :title-accessibility-label       :sync-devices-title
    :description                     (i18n/label :t/sync-devices-complete-sub-title)
    :description-accessibility-label :sync-devices-complete-sub-title}])

(defn current-device
  [installation]
  [rn/view {:style style/current-device}
   [device/view
    (merge installation
           {:this-device? true})]])

(defn devices-list
  []
  (let [installations (rf/sub [:pairing/enabled-installations])
        this-device   (first installations)
        other-devices (rest installations)]
    [rn/view {:style style/device-list}
     [current-device this-device]
     [quo/text
      {:accessibility-label :synced-with-sub-title
       :weight              :regular
       :size                :paragraph-2
       :style               {:color colors/white-opa-40}}
      (i18n/label :t/synced-with)]
     [rn/flat-list
      {:data                            other-devices
       :shows-vertical-scroll-indicator false
       :key-fn                          :installation-id
       :render-fn                       device/view}]]))

(defn continue-button
  [on-press]
  (let [profile-color (rf/sub [:profile/customization-color])]
    [quo/button
     {:on-press            (fn []
                             (when on-press
                               (on-press))
                             (rf/dispatch [:open-modal :enable-notifications]))
      :accessibility-label :continue-button
      :customization-color profile-color
      :container-style     style/continue-button}
     (i18n/label :t/continue)]))

(defn- f-view
  []
  (let [top          (safe-area/get-top)
        translate-x  (reanimated/use-shared-value 0)
        window-width (:width (rn/get-window))]
    [rn/view {:style (style/page-container top)}
     [background/view true]
     [reanimated/view
      {:style (style/content translate-x)}
      [page-title]
      [devices-list]
      [continue-button
       #(reanimated/animate-shared-value-with-delay translate-x
                                                    (- window-width)
                                                    constants/onboarding-modal-animation-duration
                                                    :linear
                                                    200)]]]))

(defn view
  []
  [:f> f-view])
