(ns status-im2.contexts.syncing.standard-authentication.enter-password
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [utils.re-frame :as rf]
            [clojure.string :as string]
            [utils.security.core :as security]
            [status-im2.contexts.syncing.standard-authentication.style :as style]
            [status-im2.contexts.syncing.standard-authentication.enter-password :as enter-password]))

(defn- get-error-message
  [error]
  (if (and (some? error)
           (or (= error "file is not a database")
               (string/starts-with? error "failed to set ")
               (string/starts-with? error "Failed")))
    (i18n/label :t/oops-wrong-password)
    error))

(defn view
  [set-code button-label]
  (let [entered-password                    (atom "")
        {:keys [key-uid display-name
                customization-color]}       (rf/sub [:profile/multiaccount])
        {:keys [error processing password]} (rf/sub [:profile/login])
        sign-in-enabled?                    (rf/sub [:sign-in-enabled?])
        profile-picture                     (rf/sub [:profile/login-profiles-picture key-uid])
        error                               (get-error-message error)]
    [background/view true]
    [:<>
     [rn/view {:style style/enter-password-container}
      [rn/view
       [quo/text
        {:accessibility-label :sync-code-generated
         :weight              :bold
         :size                :heading-1
         :style               {:color         (colors/theme-colors colors/neutral-100 colors/white)
                               :margin-bottom 4}}
        (i18n/label :t/enter-password)]

       [rn/view
        {:style style/context-tag}
        [quo/context-tag {:blur? false} profile-picture display-name]]

       [rn/view
        [quo/input
         {:type                :password
          :blur?               true
          :placeholder         (i18n/label :t/type-your-password)
          :auto-focus          true
          :show-cancel         false
          :error?              (seq error)
          :accessibility-label :password-input
          :label               (i18n/label :t/profile-password)
          :on-change-text      (fn [password]
                                 (rf/dispatch [:set-in [:profile/login :password]
                                               (security/mask-data password)])
                                 (reset! entered-password password)
                                 (rf/dispatch [:set-in [:profile/login :error] ""]))}]]
       (when (seq error)
         [rn/view {:style style/error-message}
          [quo/info-message
           {:type :error
            :size :default
            :icon :i/info}
           error]
          [rn/touchable-opacity
           {:hit-slop       {:top 6 :bottom 20 :left 0 :right 0}
            :disabled       false
            :active-opacity 1
            :on-press       #(reset! entered-password %)}
           [quo/text
            {:style                 {:text-decoration-line :underline
                                     :color                colors/danger-60}
             :size                  :paragraph-2
             :suppress-highlighting true}
            (i18n/label :t/forgot-password)]]])

       [rn/view {:style style/enter-password-button}
        [quo/button
         {:size                40
          :type                :primary
          :customization-color (or customization-color :primary)
          :accessibility-label :login-button
          :icon-left           :i/reveal
          :disabled?           (or (not sign-in-enabled?) processing)
          :on-press            (fn []
                                 (rf/dispatch [:set-in [:profile/login :key-uid] key-uid])
                                 (rf/dispatch
                                  [:syncing/get-connection-string-for-bootstrapping-another-device
                                   password set-code]))}
         button-label]]]]]))
