(ns status-im2.contexts.onboarding.syncing.progress.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [react-native.safe-area :as safe-area]
            [status-im2.contexts.onboarding.syncing.progress.style :as style]
            [status-im2.contexts.onboarding.common.background.view :as background]))

(defn page-title
  [pairing-progress?]
  [rn/view {:style {:padding-horizontal 20}}
   [quo/text
    {:accessibility-label :notifications-screen-title
     :weight              :semi-bold
     :size                :heading-1
     :style               {:color colors/white}}
    (if pairing-progress?
      (i18n/label :t/sync-devices-title)
      (i18n/label :t/sync-devices-error-title))]
   [quo/text
    {:accessibility-label :notifications-screen-sub-title
     :weight              :regular
     :size                :paragraph-1
     :style               {:color colors/white}}
    (if pairing-progress?
      (i18n/label :t/sync-devices-sub-title)
      (i18n/label :t/sync-devices-error-sub-title))]])

(defn navigation-bar
  []
  [rn/view {:style style/navigation-bar}
   [quo/page-nav]])

(defn view
  []
  (let [local-pairing-status (rf/sub [:pairing/pairing-in-progress])
        status               (:pairing-in-progress? local-pairing-status)
        profile-color        (:color (rf/sub [:onboarding-2/profile]))]
    [safe-area/consumer
     (fn [insets]
       (js/console.log (str "pairing in progress " status))
       [rn/view {:style (style/page-container (:top insets))}
        [background/view true]
        [navigation-bar]
        [page-title status]
        (if status
          [rn/view {:style style/page-illustration}
           [quo/text "[Success here]"]]
          [rn/view {:style style/page-illustration}
           [quo/text "[Error here]"]])
        (when-not status
          [quo/button
           {:on-press                  #(rf/dispatch [:navigate-back])
            :accessibility-label       :enable-notifications-later-button
            :override-background-color (colors/custom-color profile-color 60)
            :style                     {:margin-top         20
                                        :padding-horizontal 20}}
           (i18n/label :t/try-again)])])]))
