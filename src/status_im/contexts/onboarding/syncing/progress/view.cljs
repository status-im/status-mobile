(ns status-im.contexts.onboarding.syncing.progress.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.config :as config]
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

(defn navigate-to-enter-seed-phrase
  [view-id]
  (if (= view-id :screen/onboarding.syncing-progress-intro)
    (do
      (rf/dispatch [:navigate-back-to :screen/onboarding.sync-or-recover-profile])
      (debounce/debounce-and-dispatch
       [:onboarding/navigate-to-sign-in-by-seed-phrase :screen/onboarding.sync-or-recover-profile]
       300))
    (do
      (rf/dispatch [:navigate-back])
      (debounce/debounce-and-dispatch [:onboarding/overlay-show] 100)
      (debounce/debounce-and-dispatch [:open-modal :screen/onboarding.new-to-status] 200)
      (debounce/debounce-and-dispatch
       [:onboarding/navigate-to-sign-in-by-seed-phrase :screen/onboarding.new-to-status]
       300))))

(defn try-again-button
  [profile-color in-onboarding? logged-in? view-id]
  [rn/view
   (when-not logged-in?
     [quo/button
      {:on-press            #(navigate-to-enter-seed-phrase view-id)
       :accessibility-label :try-seed-phrase-button
       :customization-color profile-color
       :container-style     style/try-again-button}
      (i18n/label :t/enter-seed-phrase)])


   [quo/button
    {:on-press            (fn []
                            (rf/dispatch [:syncing/clear-states])
                            (cond
                              logged-in?     (rf/dispatch [:navigate-back])
                              in-onboarding? (rf/dispatch [:navigate-back-to
                                                           :screen/onboarding.sign-in-intro])
                              :else          (do
                                               (rf/dispatch [:navigate-back])
                                               (debounce/throttle-and-dispatch
                                                [:open-modal
                                                 :screen/onboarding.sign-in]
                                                1000))))
     :accessibility-label :try-again-later-button
     :customization-color profile-color
     :container-style     style/try-again-button}
    (i18n/label :t/try-again)]])

(defn view
  [in-onboarding?]
  (let [pairing-status (rf/sub [:pairing/pairing-status])
        profile-color  (:color (rf/sub [:onboarding/profile]))
        logged-in?     (rf/sub [:multiaccount/logged-in?])
        view-id        (rf/sub [:view-id])]
    [rn/view {:style (style/page-container in-onboarding?)}
     (when-not in-onboarding?
       [rn/view {:style style/absolute-fill}
        [background/view true]])
     [quo/page-nav {:type :no-title :background :blur}]
     [page-title (pairing-progress pairing-status)]
     (if config/show-not-implemented-features?
       (if (pairing-progress pairing-status)
         [rn/view {:style style/page-illustration}
          [quo/text "[Success here]"]]
         [rn/view {:style style/page-illustration}
          [quo/text "[Error here]"]])
       [rn/view {:flex 1}])
     (when-not (pairing-progress pairing-status)
       [try-again-button profile-color in-onboarding? logged-in? view-id])]))

(defn view-onboarding
  []
  [view true])
