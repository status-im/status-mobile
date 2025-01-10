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
                                                   :t/sync-profile-title
                                                   :t/sync-devices-error-title))
    :description                     (when-not pairing-progress?
                                       (i18n/label :t/sync-devices-error-sub-title))
    :title-accessibility-label       :progress-screen-title
    :description-accessibility-label :progress-screen-sub-title}])

(defn- navigate-to-enter-seed-phrase
  []
  (rf/dispatch [:syncing/set-syncing-fallback-flow])
  (debounce/debounce-and-dispatch
   [:onboarding/navigate-to-sign-in-by-seed-phrase :screen/onboarding.log-in]
   500))

(defn- try-again
  [logged-in?]
  (rf/dispatch [:syncing/clear-states])
  (if logged-in?
    (rf/dispatch [:navigate-back])
    (rf/dispatch [:navigate-back-to :screen/onboarding.log-in])))

(defn try-again-button
  [profile-color logged-in?]
  (let [two-vertical-actions-height 116]
    [quo/bottom-actions
     {:actions (if logged-in? :one-action :two-vertical-actions)
      :blur? true
      :container-style {:height (when-not logged-in? two-vertical-actions-height)}
      :button-two-label (i18n/label :t/use-recovery-phrase)
      :button-two-props {:type                :primary
                         :accessibility-label :try-seed-phrase-button
                         :customization-color profile-color
                         :size                40
                         :on-press            navigate-to-enter-seed-phrase}
      :button-one-label
      (i18n/label :t/try-again)
      :button-one-props
      {:type                (if logged-in? :primary :grey)
       :accessibility-label :try-again-later-button
       :customization-color profile-color
       :size                40
       :on-press            #(try-again logged-in?)}}]))

(defn- illustration
  [pairing-progress?]
  [rn/image
   {:resize-mode :contain
    :style       (style/page-illustration (:width (rn/get-window)) pairing-progress?)
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
     (if pairing-progress?
       [quo/information-box
        {:type  :default
         :blur? true
         :style {:margin-vertical 11 :margin-horizontal 12}}
        (i18n/label :t/sync-devices-sub-title)]
       [try-again-button profile-color logged-in?])]))

(defn view-onboarding
  []
  [view true])
