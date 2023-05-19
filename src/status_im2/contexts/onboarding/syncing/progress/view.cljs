(ns status-im2.contexts.onboarding.syncing.progress.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [status-im2.contexts.onboarding.syncing.progress.style :as style]
            [status-im2.contexts.onboarding.common.background.view :as background]))

(defn pairing-progress
  [status]
  (cond
    (= status :error)
    false

    :else
    true))

(defn page-title
  [pairing-progress?]
  [quo/title
   {:title                        (i18n/label (if pairing-progress?
                                                :t/sync-devices-title
                                                :t/sync-devices-error-title))
    :subtitle                     (i18n/label (if pairing-progress?
                                                :t/sync-devices-sub-title
                                                :t/sync-devices-error-sub-title))
    :title-accessibility-label    :progress-screen-title
    :subtitle-accessibility-label :progress-screen-sub-title}])

(defn try-again-button
  [profile-color]
  [quo/button
   {:on-press                  #(rf/dispatch [:navigate-back])
    :accessibility-label       :try-again-later-button
    :override-background-color (colors/custom-color profile-color 60)
    :style                     style/try-again-button}
   (i18n/label :t/try-again)])

(defn view
  []
  (let [pairing-status (rf/sub [:pairing/pairing-in-progress])
        profile-color  (:color (rf/sub [:onboarding-2/profile]))]
    [rn/view {:style style/page-container}
     [background/view true]
     [quo/page-nav]
     [page-title (pairing-progress pairing-status)]
     (if (pairing-progress pairing-status)
       [rn/view {:style style/page-illustration}
        [quo/text "[Success here]"]]
       [rn/view {:style style/page-illustration}
        [quo/text "[Error here]"]])
     (when-not (pairing-progress pairing-status)
       [try-again-button profile-color])]))
