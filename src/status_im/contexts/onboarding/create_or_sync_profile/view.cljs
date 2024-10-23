(ns status-im.contexts.onboarding.create-or-sync-profile.view
  (:require
    [quo.core :as quo]
    re-frame.db
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.check-before-syncing.view :as check-before-syncing]
    [status-im.common.metrics-confirmation-modal.view :as metrics-modal]
    [status-im.common.resources :as resources]
    [status-im.config :as config]
    [status-im.contexts.onboarding.create-or-sync-profile.style :as style]
    [status-im.contexts.onboarding.getting-started-doc.view :as getting-started-doc]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- navigate-to-create-profile
  []
  (debounce/throttle-and-dispatch
   [:onboarding/navigate-to-create-profile]
   1000))

(defn- navigate-to-sign-in-by-syncing
  []
  (rf/dispatch [:syncing/clear-syncing-fallback-flow])
  (debounce/throttle-and-dispatch
   [:onboarding/navigate-to-sign-in-by-syncing]
   1000))

(defn- show-check-before-syncing
  []
  (rf/dispatch
   [:show-bottom-sheet
    {:content (fn [] [check-before-syncing/view
                      {:on-submit navigate-to-sign-in-by-syncing}])
     :shell?  true}]))

(defn- navigate-to-sign-in-by-seed-phrase
  [create-profile?]
  (rf/dispatch [:syncing/clear-syncing-fallback-flow])
  (rf/dispatch [:onboarding/navigate-to-sign-in-by-seed-phrase
                (if create-profile?
                  :screen/onboarding.new-to-status
                  :screen/onboarding.sync-or-recover-profile)]))

(defn- option-card-max-height
  [window-height]
  (- window-height
     (* 2 56) ;; two other list items
     (* 2 16) ;; spacing between items
     220)) ;; extra spacing (top bar)


(defn- create-profile-option-card
  [window-height]
  [quo/small-option-card
   {:variant             :main
    :title               (i18n/label :t/generate-keys)
    :subtitle            (i18n/label :t/generate-keys-subtitle)
    :button-label        (i18n/label :t/lets-go)
    :accessibility-label :generate-key-option-card
    :image               (resources/get-image :generate-keys)
    :max-height          (option-card-max-height window-height)
    :on-press            navigate-to-create-profile}])

(defn- sync-profile-option-card
  [window-height]
  [quo/small-option-card
   {:variant             :main
    :title               (i18n/label :t/sign-in-by-syncing)
    :subtitle            (i18n/label :t/if-you-have-status-on-another-device)
    :button-label        (i18n/label :t/scan-sync-code)
    :accessibility-label :scan-sync-code-option-card
    :image               (resources/get-image :generate-keys)
    :max-height          (option-card-max-height window-height)
    :on-press            show-check-before-syncing}])

(defn sign-in-options
  [sign-in-type]
  (let [window-height                      (rf/sub [:dimensions/window-height])
        create-profile?                    (= sign-in-type :create-profile)
        nav-to-seed-phrase-with-cur-screen (rn/use-callback
                                            #(navigate-to-sign-in-by-seed-phrase
                                              create-profile?)
                                            [create-profile?])
        main-option-card                   (if create-profile?
                                             create-profile-option-card
                                             sync-profile-option-card)]
    [rn/view {:style style/options-container}
     [quo/text
      {:style  style/title
       :size   :heading-1
       :weight :semi-bold}
      (i18n/label (if create-profile? :t/create-profile :t/sync-or-recover-profile))]
     [main-option-card window-height]
     [rn/view {:style style/subtitle-container}
      [quo/text
       {:style  style/subtitle
        :size   :paragraph-2
        :weight :medium}
       (i18n/label (if create-profile? :t/experienced-web3 :t/dont-have-statatus-on-another-device))]]
     [rn/view
      [quo/small-option-card
       {:variant             :icon
        :title               (i18n/label :t/use-recovery-phrase)
        :subtitle            (i18n/label :t/use-recovery-phrase-subtitle)
        :accessibility-label :use-recovery-phrase-option-card
        :image               (resources/get-image :ethereum-address)
        :on-press            nav-to-seed-phrase-with-cur-screen}]
      [rn/view {:style style/space-between-suboptions}]
      [quo/small-option-card
       {:variant             :icon
        :title               (i18n/label :t/use-keycard)
        :subtitle            (i18n/label :t/profile-keys-on-keycard)
        :accessibility-label :use-keycard-option-card
        :image               (resources/get-image :use-keycard)
        :on-press            (fn []
                               (rf/dispatch [:open-modal :screen/keycard.check
                                             {:on-press
                                              #(rf/dispatch
                                                [:keycard.login/check-card])}]))}]]]))

(defn- navigate-back
  []
  (rf/dispatch [:onboarding/overlay-dismiss])
  (rf/dispatch [:navigate-back]))

(defn- navigate-to-quo-preview
  []
  (rf/dispatch [:navigate-to :quo-preview]))

(defn- internal-view
  [sign-in-type]
  (rn/use-mount #(rf/dispatch [:centralized-metrics/check-modal metrics-modal/view]))
  (let [{:keys [top]} (safe-area/get-insets)]
    [rn/view {:style style/content-container}
     [quo/page-nav
      {:margin-top top
       :type       :no-title
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   navigate-back
       :right-side [{:icon-name :i/info
                     :on-press  getting-started-doc/show-as-bottom-sheet}
                    (when config/quo-preview-enabled?
                      {:icon-name :i/reveal-whitelist
                       :on-press  navigate-to-quo-preview})]}]
     [sign-in-options sign-in-type]]))

(defn create-profile
  []
  [internal-view :create-profile])

(defn sync-or-recover-profile
  []
  [internal-view :sync-or-recover-profile])
