(ns status-im.contexts.profile.profiles.view
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [status-im.common.confirmation-drawer.view :as confirmation-drawer]
    [status-im.common.standard-authentication.core :as standard-authentication]
    [status-im.config :as config]
    [status-im.constants :as constants]
    [status-im.contexts.keycard.pin.view :as keycard.pin]
    [status-im.contexts.onboarding.common.background.view :as background]
    [status-im.contexts.profile.profiles.style :as style]
    [taoensso.timbre :as log]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.transforms :as transforms]))

(defonce push-animation-fn-atom (atom nil))
(defonce pop-animation-fn-atom (atom nil))
;; we need this for hotreload, overwise on hotreload translate-x will be reseted
(defonce translate-x-atom (atom 0))

(defn push-animation
  [translate-x]
  (let [window-width (:width (rn/get-window))]
    (reset! translate-x-atom (- window-width))
    (reanimated/animate-shared-value-with-delay translate-x
                                                (- window-width)
                                                constants/onboarding-modal-animation-duration
                                                :linear
                                                100)))

(defn pop-animation
  [translate-x]
  (reset! translate-x-atom 0)
  (reanimated/animate-shared-value-with-delay translate-x
                                              0
                                              constants/onboarding-modal-animation-duration
                                              :linear
                                              50))

(defn- navigate-to-new-to-status
  []
  (when @push-animation-fn-atom
    (@push-animation-fn-atom))
  (rf/dispatch [:onboarding/use-temporary-display-name true])
  (debounce/throttle-and-dispatch
   [:open-modal :screen/onboarding.new-to-status]
   1000))

(defn- navigate-to-sync-or-recover-profile
  []
  (when @push-animation-fn-atom
    (@push-animation-fn-atom))
  (rf/dispatch [:onboarding/use-temporary-display-name false])
  (debounce/throttle-and-dispatch
   [:open-modal :screen/onboarding.sync-or-recover-profile]
   1000))

(defn new-account-options
  []
  [quo/action-drawer
   [[{:icon                :i/profile
      :label               (i18n/label :t/create-new-profile)
      :on-press            navigate-to-new-to-status
      :accessibility-label :create-new-profile}
     {:icon                :i/multi-profile
      :label               (i18n/label :t/sync-or-recover-profile)
      :on-press            navigate-to-sync-or-recover-profile
      :accessibility-label :multi-profile}]]])

(defn show-new-account-options
  []
  (rf/dispatch [:show-bottom-sheet
                {:content new-account-options
                 :shell?  true}]))

(defn delete-profile-confirmation
  [key-uid context]
  [confirmation-drawer/confirmation-drawer
   {:title               (i18n/label :t/remove-profile?)
    :description         (i18n/label :t/remove-profile-confirm-message)
    :accessibility-label :remove-profile-confirm
    :context             context
    :button-text         (i18n/label :t/remove)
    :close-button-text   (i18n/label :t/cancel)
    :on-press            (fn []
                           (rf/dispatch [:hide-bottom-sheet])
                           (native-module/delete-multiaccount
                            key-uid
                            (fn [result]
                              (let [{:keys [error]} (transforms/json->clj result)]
                                (rf/dispatch [:onboarding/on-delete-profile-success key-uid])
                                (log/info "profile deleted: error" error)))))}])

(defn show-confirmation
  [key-uid context]
  (rf/dispatch [:show-bottom-sheet
                {:content (fn [] [delete-profile-confirmation key-uid context])}]))

(defn profile-options
  [key-uid context]
  [quo/action-drawer
   [[{:icon                :i/delete
      :label               (i18n/label :t/remove-profile-message)
      :on-press            #(show-confirmation key-uid context)
      :accessibility-label :remove-profile
      :danger?             true}]]])

(defn show-profile-options
  [key-uid context]
  (rf/dispatch [:show-bottom-sheet
                {:content (fn [] [profile-options key-uid context])
                 :shell?  true}]))

(defn profile-card
  [{:keys [name key-uid customization-color keycard-pairing]}
   index
   _
   {:keys [last-index set-hide-profiles]}]
  (let [last-item?      (= last-index index)
        profile-picture (rf/sub [:profile/login-profiles-picture key-uid])]
    [quo/profile-card
     {:name                 name
      :login-card?          true
      :last-item?           (= last-index index)
      :customization-color  (or customization-color :primary)
      :keycard-account?     keycard-pairing
      :show-options-button? true
      :profile-picture      profile-picture
      :card-style           (style/profiles-profile-card last-item?)
      :on-options-press     #(show-profile-options
                              key-uid
                              {:name            name
                               :color           customization-color
                               :profile-picture profile-picture})
      :on-card-press        (fn []
                              (rf/dispatch-sync [:profile/profile-selected key-uid])
                              (rf/dispatch
                               [:profile.login/login-with-biometric-if-available key-uid])
                              (set-hide-profiles))}]))

(defn- profiles-section
  [{:keys [hide-profiles]}]
  (let [profiles    (vals (rf/sub [:profile/profiles-overview]))
        translate-x (reanimated/use-shared-value @translate-x-atom)]
    (rn/use-mount (fn []
                    (reset! push-animation-fn-atom #(push-animation translate-x))
                    (reset! pop-animation-fn-atom #(pop-animation translate-x))
                    (fn []
                      (when-let [pop-animation-fn @pop-animation-fn-atom]
                        (when (not= translate-x 0)
                          (pop-animation-fn)))
                      (reset! push-animation-fn-atom nil)
                      (reset! pop-animation-fn-atom nil))))
    [reanimated/view {:style (style/profiles-container translate-x)}
     [rn/view {:style style/profiles-header}
      [quo/text
       {:size   :heading-1
        :weight :semi-bold
        :style  style/profiles-header-text}
       (i18n/label :t/profiles-on-device)]
      [quo/button
       {:type                :primary
        :customization-color :blue
        :size                32
        :icon-only?          true
        :on-press            show-new-account-options
        :accessibility-label :show-new-account-options}
       :main-icons/add]]
     [rn/flat-list
      {:data                    (sort-by :timestamp > profiles)
       :key-fn                  :key-uid
       :content-container-style {:padding-bottom 20}
       :render-data             {:last-index        (dec (count profiles))
                                 :set-hide-profiles hide-profiles}
       :render-fn               profile-card}]]))

(defn password-input
  [processing error auth-method]
  (let [on-press-biometrics (fn []
                              (rf/dispatch
                               [:biometric/authenticate
                                {:on-success #(rf/dispatch
                                               [:profile.login/biometric-success])
                                 :on-fail    #(rf/dispatch
                                               [:profile.login/biometric-auth-fail %])}]))]
    [standard-authentication/password-input
     {:processing          processing
      :error               error
      :shell?              true
      :blur?               true
      :on-press-biometrics (when (= auth-method constants/auth-method-biometric)
                             on-press-biometrics)}]))

(defn- get-error-message
  [error]
  (if (and (some? error)
           (or (= error "file is not a database")
               (string/starts-with? (string/lower-case error) "failed")))
    (i18n/label :t/oops-wrong-password)
    error))

(defn login-section
  [{:keys [show-profiles]}]
  (let [{:keys [key-uid name keycard-pairing auth-method
                customization-color]} (rf/sub [:profile/login-profile])
        sign-in-enabled?              (rf/sub [:sign-in-enabled?])
        profile-picture               (rf/sub [:profile/login-profiles-picture key-uid])
        {:keys [error processing]}    (rf/sub [:profile/login])
        error-message                 (rn/use-memo #(get-error-message error) [error])
        error                         {:error?        (boolean (seq error-message))
                                       :error-message error-message}
        login-profile                 (rn/use-callback #(rf/dispatch [:profile.login/login]))]
    [rn/keyboard-avoiding-view
     {:style                    style/login-container
      :keyboard-vertical-offset (- (safe-area/get-bottom))}
     [rn/view {:style style/multi-profile-button-container}
      (when config/quo-preview-enabled?
        [quo/button
         {:size                32
          :type                :grey
          :background          :blur
          :icon-only?          true
          :on-press            #(rf/dispatch [:navigate-to :quo-preview])
          :disabled?           processing
          :accessibility-label :quo-preview
          :container-style     {:margin-right 12}}
         :i/reveal-whitelist])
      [quo/button
       {:size                32
        :type                :grey
        :background          :blur
        :icon-only?          true
        :on-press            show-profiles
        :disabled?           processing
        :accessibility-label :show-profiles}
       :i/multi-profile]]
     [(if keycard-pairing rn/view rn/scroll-view)
      {:keyboard-should-persist-taps :always
       :style                        {:flex 1}}
      [quo/profile-card
       {:name                name
        :customization-color (or customization-color :primary)
        :profile-picture     profile-picture
        :card-style          style/login-profile-card}]
      (if keycard-pairing
        [keycard.pin/auth
         {:error error
          :on-complete
          (fn [pin]
            (rf/dispatch
             [:keycard/connect
              {:key-uid key-uid
               :on-success
               (fn []
                 (rf/dispatch
                  [:keycard/get-keys
                   {:pin        pin
                    :on-success #(rf/dispatch [:keycard.login/on-get-keys-success %])
                    :on-failure #(rf/dispatch [:keycard/on-action-with-pin-error %])}]))}]))}]
        [password-input processing error auth-method])]
     (when-not keycard-pairing
       [quo/button
        {:size                40
         :type                :primary
         :customization-color (or customization-color :primary)
         :accessibility-label :login-button
         :icon-left           :i/unlocked
         :disabled?           (or (not sign-in-enabled?) processing)
         :on-press            login-profile
         :container-style     {:margin-bottom (+ (safe-area/get-bottom) 12)}}
        (i18n/label :t/log-in)])]))

(defn view
  []
  (let [[show-profiles? set-show-profiles] (rn/use-state true)
        show-profiles                      (rn/use-callback #(set-show-profiles true))
        hide-profiles                      (rn/use-callback #(set-show-profiles false))]
    (rn/use-mount #(rf/dispatch [:centralized-metrics/check-user-confirmation]))
    [:<>
     [background/view true]
     (if show-profiles?
       [profiles-section {:hide-profiles hide-profiles}]
       [login-section {:show-profiles show-profiles}])]))
