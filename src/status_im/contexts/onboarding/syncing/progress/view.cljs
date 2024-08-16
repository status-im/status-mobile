(ns status-im.contexts.onboarding.syncing.progress.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.resources :as resources]
    [status-im.contexts.onboarding.common.background.view :as background]
    [status-im.contexts.onboarding.syncing.progress.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn pairing-progress
  [status]
  (not= status :error))

(defn page-title
  [pairing-progress?]
  [quo/text-combinations
   {:container-style                 {:margin-top 56 :margin-horizontal 20}
    :title                           (i18n/label (if pairing-progress?
                                                   :t/sync-devices-title
                                                   :t/sync-devices-error-title))
    :description                     (i18n/label (if pairing-progress?
                                                   :t/sync-devices-sub-title
                                                   :t/sync-devices-error-sub-title))
    :title-accessibility-label       :progress-screen-title
    :description-accessibility-label :progress-screen-sub-title}])

(defn try-again-button
  [profile-color]
  [quo/button
   {:on-press            (fn []
                           (rf/dispatch [:syncing/clear-states])
                           (rf/dispatch [:navigate-back]))
    :accessibility-label :try-again-later-button
    :customization-color profile-color
    :size                40
    :container-style     style/try-again-button}
   (i18n/label :t/try-again)])

(defn- illustration
  [pairing-progress?]
  [rn/image
   {:resize-mode :contain
    :style       (style/page-illustration (:width (rn/get-window)))
    :source      (resources/get-image (if pairing-progress? :syncing-devices :syncing-wrong))}])

(defn view
  [in-onboarding?]
  (let [pairing-status    (rf/sub [:pairing/pairing-status])
        pairing-progress? (pairing-progress pairing-status)
        profile-color     (or (:color (rf/sub [:onboarding/profile]))
                              (rf/sub [:profile/customization-color]))]
    [rn/view {:style (style/page-container in-onboarding?)}
     (when-not in-onboarding?
       [rn/view {:style style/absolute-fill}
        [background/view true]])
     [quo/page-nav {:type :no-title :background :blur}]
     [page-title pairing-progress?]
     [illustration pairing-progress?]
     (when-not (pairing-progress pairing-status)
       [try-again-button profile-color])]))

(defn view-onboarding
  []
  [view true])
