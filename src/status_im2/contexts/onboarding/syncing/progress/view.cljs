(ns status-im2.contexts.onboarding.syncing.progress.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [status-im2.contexts.onboarding.syncing.progress.style :as style]
            [status-im2.contexts.onboarding.common.background.view :as background]))

(defn pairing-progress
  [status]
  (not= status :error))

(defn page-title
  [pairing-progress?]
  [quo/title
   {:container-style              {:margin-top 56}
    :title                        (i18n/label (if pairing-progress?
                                                :t/sync-devices-title
                                                :t/sync-devices-error-title))
    :subtitle                     (i18n/label (if pairing-progress?
                                                :t/sync-devices-sub-title
                                                :t/sync-devices-error-sub-title))
    :title-accessibility-label    :progress-screen-title
    :subtitle-accessibility-label :progress-screen-sub-title}])

(defn try-again-button
  [profile-color in-onboarding?]
  [quo/button
   {:on-press            (fn []
                           (rf/dispatch [:syncing/clear-states])
                           (rf/dispatch [:navigate-back-to
                                         (if in-onboarding? :sign-in-intro :sign-in)]))
    :accessibility-label :try-again-later-button
    :customization-color profile-color
    :container-style     style/try-again-button}
   (i18n/label :t/try-again)])

(defn view
  [in-onboarding?]
  (let [pairing-status (rf/sub [:pairing/pairing-status])
        profile-color  (:color (rf/sub [:onboarding-2/profile]))]
    [rn/view {:style (style/page-container in-onboarding?)}
     (when-not in-onboarding? [background/view true])
     [quo/page-nav]
     [page-title (pairing-progress pairing-status)]
     (if (pairing-progress pairing-status)
       [rn/view {:style style/page-illustration}
        [quo/text "[Success here]"]]
       [rn/view {:style style/page-illustration}
        [quo/text "[Error here]"]])
     (when-not (pairing-progress pairing-status)
       [try-again-button profile-color in-onboarding?])]))

(defn view-onboarding
  []
  [view true])
