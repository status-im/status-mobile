(ns status-im.contexts.onboarding.syncing.progress.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.resources :as resources]
    [status-im.contexts.onboarding.common.background.view :as background]
    [status-im.contexts.onboarding.syncing.progress.style :as style]
    [utils.debounce :as debounce]
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

(defn- navigate-to-enter-seed-phrase
  []
  (rf/dispatch [:navigate-back-to :screen/onboarding.sync-or-recover-profile])
  (debounce/debounce-and-dispatch
   [:onboarding/navigate-to-sign-in-by-seed-phrase :screen/onboarding.sync-or-recover-profile]
   500))

(defn try-again-button
  [profile-color logged-in?]
  [:<>
   (when-not logged-in?
     [quo/button
      {:on-press            navigate-to-enter-seed-phrase
       :accessibility-label :try-seed-phrase-button
       :customization-color profile-color
       :container-style     style/try-again-button}
      (i18n/label :t/enter-seed-phrase)])
   [quo/button
    {:on-press            (fn []
                            (rf/dispatch [:syncing/clear-states])
                            (rf/dispatch [:navigate-back]))
     :accessibility-label :try-again-later-button
     :customization-color profile-color
     :size                40
     :container-style     style/try-again-button}
    (i18n/label :t/try-again)]])

(defn- illustration
  [pairing-progress?]
  [rn/image
   {:resize-mode :contain
    :style       (style/page-illustration (:width (rn/get-window)))
    :source      (resources/get-image (if pairing-progress? :syncing-devices :syncing-wrong))}])

(defn view
  [in-onboarding?]
  (let [pairing-status    (rf/sub [:pairing/pairing-status])
        logged-in?        (rf/sub [:multiaccount/logged-in?])
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
       [try-again-button profile-color logged-in?])]))

(defn view-onboarding
  []
  [view true])
