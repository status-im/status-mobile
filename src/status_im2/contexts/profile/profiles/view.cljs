(ns status-im2.contexts.profile.profiles.view
  (:require [clojure.string :as string]
            [native-module.core :as native-module]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im2.common.confirmation-drawer.view :as confirmation-drawer]
            [status-im2.config :as config]
            [status-im2.constants :as constants]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [status-im2.contexts.profile.profiles.style :as style]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [utils.security.core :as security]
            [utils.transforms :as types]))

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

(defn new-account-options
  []
  [quo/action-drawer
   [[{:icon                :i/profile
      :label               (i18n/label :t/create-new-profile)
      :on-press            (fn []
                             (when @push-animation-fn-atom
                               (@push-animation-fn-atom))
                             (rf/dispatch [:open-modal :new-to-status]))
      :accessibility-label :create-new-profile}
     {:icon                :i/multi-profile
      :label               (i18n/label :t/add-existing-status-profile)
      :on-press            #(rf/dispatch [:open-modal :sign-in])
      :accessibility-label :multi-profile}]]])

(defn show-new-account-options
  []
  (rf/dispatch [:show-bottom-sheet
                {:content new-account-options
                 :shell?  true}]))

(defn delete-profile-confirmation
  [key-uid context]
  [confirmation-drawer/confirmation-drawer
   {:title               (i18n/label :remove-profile?)
    :description         (i18n/label :remove-profile-confirm-message)
    :accessibility-label :remove-profile-confirm
    :context             context
    :button-text         (i18n/label :t/remove)
    :close-button-text   (i18n/label :t/cancel)
    :on-press            (fn []
                           (rf/dispatch [:hide-bottom-sheet])
                           (native-module/delete-multiaccount
                            key-uid
                            (fn [result]
                              (let [{:keys [error]} (types/json->clj result)]
                                (rf/dispatch [:onboarding-2/on-delete-profile-success key-uid])
                                (log/info "profile deleted: error" error)))))}])

(defn show-confirmation
  [key-uid context]
  (rf/dispatch [:show-bottom-sheet
                {:content (fn [] [delete-profile-confirmation key-uid context])}]))

(defn profile-options
  [key-uid context]
  [quo/action-drawer
   [[{:icon                :i/delete
      :label               (i18n/label :remove-profile-message)
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
                              (rf/dispatch
                               [:profile/profile-selected key-uid])
                              (when-not keycard-pairing (set-hide-profiles)))}]))

(defn- f-profiles-section
  [{:keys [set-hide-profiles]}]
  (let [profiles    (vals (rf/sub [:profile/profiles-overview]))
        translate-x (reanimated/use-shared-value @translate-x-atom)]
    (rn/use-effect (fn []
                     (reset! push-animation-fn-atom #(push-animation translate-x))
                     (reset! pop-animation-fn-atom #(pop-animation translate-x))
                     (fn []
                       (reset! push-animation-fn-atom nil)
                       (reset! pop-animation-fn-atom nil))))
    [reanimated/view
     {:style (style/profiles-container translate-x)}
     [rn/view
      {:style style/profiles-header}
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
       :render-data             {:last-index        (- (count profiles) 1)
                                 :set-hide-profiles set-hide-profiles}
       :render-fn               profile-card}]]))

(defn profiles-section
  [props]
  [:f> f-profiles-section props])

(defn forget-password-doc
  []
  [quo/documentation-drawers
   {:title  (i18n/label :t/forgot-your-password-info-title)
    :shell? true}
   [rn/view
    {:style style/forget-password-doc-container}
    [quo/text {:size :paragraph-2} (i18n/label :t/forgot-your-password-info-description)]

    [rn/view {:style style/forget-password-step-container}
     [quo/step {:in-blur-view? true} 1]
     [rn/view
      {:style style/forget-password-step-content}
      [quo/text {:size :paragraph-2 :weight :semi-bold}
       (i18n/label :t/forgot-your-password-info-remove-app)]
      [quo/text {:size :paragraph-2} (i18n/label :t/forgot-your-password-info-remove-app-description)]]]

    [rn/view {:style style/forget-password-step-container}
     [quo/step {:in-blur-view? true} 2]
     [rn/view
      {:style style/forget-password-step-content}
      [quo/text {:size :paragraph-2 :weight :semi-bold}
       (i18n/label :t/forgot-your-password-info-reinstall-app)]
      [quo/text {:size :paragraph-2}
       (i18n/label :t/forgot-your-password-info-reinstall-app-description)]]]

    [rn/view {:style style/forget-password-step-container}
     [quo/step {:in-blur-view? true} 3]
     [rn/view
      {:style style/forget-password-step-content}
      [rn/view
       {:style style/forget-password-step-title}
       [quo/text {:size :paragraph-2} (str (i18n/label :t/sign-up) " ")]
       [quo/text {:size :paragraph-2 :weight :semi-bold}
        (i18n/label :t/forgot-your-password-info-signup-with-key)]]
      [quo/text {:size :paragraph-2}
       (i18n/label :t/forgot-your-password-info-signup-with-key-description)]]]

    [rn/view {:style style/forget-password-step-container}
     [quo/step {:in-blur-view? true} 4]
     [rn/view
      {:style style/forget-password-step-content}
      [quo/text {:size :paragraph-2 :weight :semi-bold}
       (i18n/label :t/forgot-your-password-info-create-new-password)]
      [quo/text {:size :paragraph-2}
       (i18n/label :t/forgot-your-password-info-create-new-password-description)]]]]])

(defn- get-error-message
  [error]
  (if (and (some? error)
           (or (= error "file is not a database")
               (string/starts-with? error "failed to set ")
               (string/starts-with? error "Failed")))
    (i18n/label :t/oops-wrong-password)
    error))

(defn login-section
  [{:keys [set-show-profiles]}]
  (let [{:keys [error processing password]}        (rf/sub [:profile/login])
        {:keys [key-uid name customization-color]} (rf/sub [:profile/login-profile])
        sign-in-enabled?                           (rf/sub [:sign-in-enabled?])
        profile-picture                            (rf/sub [:profile/login-profiles-picture key-uid])
        error                                      (get-error-message error)
        login-multiaccount                         #(rf/dispatch [:profile.login/login])]
    [rn/keyboard-avoiding-view
     {:style                  style/login-container
      :keyboardVerticalOffset (- (safe-area/get-bottom))}
     [rn/view
      {:style style/multi-profile-button-container}
      (when config/quo-preview-enabled?
        [quo/button
         {:size                32
          :type                :grey
          :background          :blur
          :icon-only?          true
          :on-press            #(rf/dispatch [:navigate-to :quo2-preview])
          :disabled?           processing
          :accessibility-label :quo2-preview
          :container-style     {:margin-right 12}}
         :i/reveal-whitelist])
      [quo/button
       {:size                32
        :type                :grey
        :background          :blur
        :icon-only?          true
        :on-press            set-show-profiles
        :disabled?           processing
        :accessibility-label :show-profiles}
       :i/multi-profile]]
     [rn/scroll-view
      {:keyboard-should-persist-taps :always
       :style                        {:flex 1}}
      [quo/profile-card
       {:name                name
        :customization-color (or customization-color :primary)
        :profile-picture     profile-picture
        :card-style          style/login-profile-card}]
      [quo/input
       {:type              :password
        :blur?             true
        :disabled?         processing
        :placeholder       (i18n/label :t/type-your-password)
        :auto-focus        true
        :error?            (seq error)
        :label             (i18n/label :t/profile-password)
        :on-change-text    (fn [password]
                             (rf/dispatch [:set-in [:profile/login :password]
                                           (security/mask-data password)])
                             (rf/dispatch [:set-in [:profile/login :error] ""]))
        :default-value     (security/safe-unmask-data password)
        :on-submit-editing (when sign-in-enabled? login-multiaccount)}]
      (when (seq error)
        [rn/view {:style style/error-message}
         [quo/info-message
          {:type :error
           :size :default
           :icon :i/info}
          error]
         [rn/touchable-opacity
          {:hit-slop       {:top 6 :bottom 20 :left 0 :right 0}
           :disabled       processing
           :active-opacity 1
           :on-press       (fn []
                             (rn/dismiss-keyboard!)
                             (rf/dispatch [:show-bottom-sheet
                                           {:content forget-password-doc :shell? true}]))}
          [rn/text
           {:style                 {:text-decoration-line :underline
                                    :color                colors/danger-60}
            :size                  :paragraph-2
            :suppress-highlighting true}
           (i18n/label :t/forgot-password)]]])]
     [quo/button
      {:size                40
       :type                :primary
       :customization-color (or customization-color :primary)
       :accessibility-label :login-button
       :icon-left           :i/unlocked
       :disabled?           (or (not sign-in-enabled?) processing)
       :on-press            login-multiaccount
       :container-style     {:margin-bottom (+ (safe-area/get-bottom) 12)}}
      (i18n/label :t/log-in)]]))

;; we had to register it here, because of hotreload, overwise on hotreload it will be reseted
(defonce show-profiles? (reagent/atom false))

(defn view
  []
  (let [set-show-profiles #(reset! show-profiles? true)
        set-hide-profiles #(reset! show-profiles? false)]
    (fn []
      [:<>
       [background/view true]
       (if @show-profiles?
         [profiles-section {:set-hide-profiles set-hide-profiles}]
         [login-section {:set-show-profiles set-show-profiles}])])))
