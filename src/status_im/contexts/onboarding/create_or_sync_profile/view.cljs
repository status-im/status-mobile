(ns status-im.contexts.onboarding.create-or-sync-profile.view
  (:require
    [quo.core :as quo]
    re-frame.db
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.check-before-syncing.view :as check-before-syncing]
    [status-im.common.events-helper :as events-helper]
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

(defn- navigate-to-sign-in-by-recovery-phrase
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

(defn- start-fresh-main-card
  [window-height]
  [quo/small-option-card
   {:variant             :main
    :title               (i18n/label :t/start-fresh)
    :subtitle            (i18n/label :t/start-fresh-subtitle)
    :button-label        (i18n/label :t/lets-go)
    :accessibility-label :start-fresh-main-card
    :image               (resources/get-image :generate-keys)
    :max-height          (option-card-max-height window-height)
    :on-press            navigate-to-create-profile}])

(defn- log-in-with-recovery-phrase-main-card
  [window-height]
  [quo/small-option-card
   {:variant             :main
    :title               (i18n/label :t/log-in-with-recovery-phrase)
    :subtitle            (i18n/label :t/log-in-with-recovery-phrase-subtitle)
    :button-label        (i18n/label :t/use-recovery-phrase)
    :accessibility-label :log-in-with-recovery-phrase-main-card
    :image               (resources/get-image :use-recovery-phrase)
    :max-height          (option-card-max-height window-height)
    :on-press            #(navigate-to-sign-in-by-recovery-phrase false)}])

(defn- use-recovery-phrase-icon-card
  []
  [quo/small-option-card
   {:variant             :icon
    :title               (i18n/label :t/use-a-recovery-phrase)
    :subtitle            (i18n/label :t/use-a-recovery-phrase-subtitle)
    :accessibility-label :use-a-recovery-phrase-icon-card
    :image               (resources/get-image :ethereum-address)
    :on-press            #(navigate-to-sign-in-by-recovery-phrase true)}])

(defn- log-in-by-syncing-icon-card
  []
  [quo/small-option-card
   {:variant             :icon
    :title               (i18n/label :t/log-in-by-syncing)
    :subtitle            (i18n/label :t/log-in-by-syncing-subtitle)
    :accessibility-label :log-in-by-syncing-icon-card
    :image               (resources/get-image :login-syncing)
    :on-press            show-check-before-syncing}])

(defn- use-empty-keycard-icon-card
  []
  [quo/small-option-card
   {:variant             :icon
    :title               (i18n/label :t/use-an-empty-keycard)
    :subtitle            (i18n/label :t/use-an-empty-keycard-subtitle)
    :accessibility-label :use-an-empty-keycard-icon-card
    :image               (resources/get-image :use-keycard)
    :on-press            #(rf/dispatch [:open-modal :screen/keycard.create-profile])}])

(defn- log-in-with-keycard-icon-card
  []
  [quo/small-option-card
   {:variant             :icon
    :title               (i18n/label :t/log-in-with-keycard)
    :subtitle            (i18n/label :t/log-in-with-keycard-subtitle)
    :accessibility-label :log-in-with-keycard
    :image               (resources/get-image :use-keycard)
    :on-press            (fn []
                           (rf/dispatch [:open-modal :screen/keycard.check
                                         {:on-press #(rf/dispatch [:keycard.login/check-card])}]))}])

(defn sign-in-options
  [create-profile?]
  (let [window-height (rf/sub [:dimensions/window-height])]
    [rn/view {:style style/options-container}
     [quo/text
      {:style  style/title
       :size   :heading-1
       :weight :semi-bold}
      (i18n/label (if create-profile? :t/create-profile :t/log-in))]
     (if create-profile?
       [start-fresh-main-card window-height]
       [log-in-with-recovery-phrase-main-card window-height])
     [rn/view {:style style/subtitle-container}
      [quo/text
       {:style  style/subtitle
        :size   :paragraph-2
        :weight :medium}
       (i18n/label :t/other-options)]]
     (if create-profile?
       [use-recovery-phrase-icon-card]
       [log-in-by-syncing-icon-card])
     [rn/view {:style style/space-between-suboptions}]
     (if create-profile?
       [use-empty-keycard-icon-card]
       [log-in-with-keycard-icon-card])]))

(defn- navigate-to-quo-preview
  []
  (rf/dispatch [:navigate-to :quo-preview]))

(defn- internal-view
  [sign-in-type]
  (let [{:keys [top]} (safe-area/get-insets)]
    [rn/view {:style style/content-container}
     [quo/page-nav
      {:margin-top top
       :type       :no-title
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   events-helper/navigate-back
       :right-side [{:icon-name :i/info
                     :on-press  getting-started-doc/show-as-bottom-sheet}
                    (when config/quo-preview-enabled?
                      {:icon-name :i/reveal-whitelist
                       :on-press  navigate-to-quo-preview})]}]
     [sign-in-options (= sign-in-type :create-profile)]]))

(defn create-profile
  []
  [internal-view :create-profile])

(defn sync-or-recover-profile
  []
  [internal-view :sync-or-recover-profile])
