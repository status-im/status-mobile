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
  [quo/page-top {:title            (i18n/label (if pairing-progress?
                                                 :t/sync-devices-title
                                                 :t/sync-devices-error-title))
                 :description      :text
                 :description-text (i18n/label (if pairing-progress?
                                                 :t/sync-devices-sub-title
                                                 :t/sync-devices-error-sub-title))}])

(defn- navigate-to-enter-seed-phrase
  []
  (rf/dispatch [:syncing/set-syncing-fallback-flow])
  (debounce/debounce-and-dispatch
   [:onboarding/navigate-to-sign-in-by-seed-phrase :screen/onboarding.sync-or-recover-profile]
   500))

(defn- try-again
  [logged-in?]
  (rf/dispatch [:syncing/clear-states])
  (if logged-in?
    (rf/dispatch [:navigate-back])
    (rf/dispatch [:navigate-back-to :screen/onboarding.sync-or-recover-profile])))

(defn try-again-button
  [profile-color logged-in?]
  (let [number-of-actions     (if logged-in? :one-action :two-actions)
        try-again-label       (i18n/label :t/try-again)
        try-again-props       {:type                (if logged-in? :primary :grey)
                               :accessibility-label :try-again-later-button
                               :customization-color profile-color
                               :container-style     {:flex 1}
                               :size                40
                               :on-press            #(try-again logged-in?)}
        recovery-phrase-label (i18n/label :t/recovery-phrase)
        recovery-phrase-props {:type                :primary
                               :accessibility-label :try-seed-phrase-button
                               :customization-color profile-color
                               :container-style     {:flex 1}
                               :size                40
                               :on-press            navigate-to-enter-seed-phrase}
        buttons               (if logged-in?
                                {:button-one-label try-again-label
                                 :button-one-props try-again-props}
                                {:button-one-label recovery-phrase-label
                                 :button-one-props recovery-phrase-props
                                 :button-two-label try-again-label
                                 :button-two-props try-again-props})]
    [quo/bottom-actions (assoc buttons
                          :actions number-of-actions
                          :blur? true)]))

(defn- illustration
  [pairing-progress?]
  [rn/image
   {:resize-mode :contain
    :style       (style/page-illustration (:width (rn/get-window)))
    :source      (resources/get-image (if pairing-progress?
                                        :syncing-devices
                                        :syncing-wrong))}])

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
     (when-not pairing-progress?
       [try-again-button profile-color logged-in?])]))

(defn view-onboarding
  []
  [view true])
