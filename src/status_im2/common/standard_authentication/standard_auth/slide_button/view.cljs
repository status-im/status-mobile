(ns status-im2.common.standard-authentication.standard-auth.slide-button.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.common.standard-authentication.standard-auth.authorize :as authorize]))

(defn- view-internal
  [_]
  (let [reset-slider? (reagent/atom false)
        on-close      #(reset! reset-slider? true)]
    (fn [{:keys [biometric-auth?
                 track-text
                 customization-color
                 auth-button-label
                 on-enter-password
                 on-auth-success
                 on-auth-fail
                 auth-button-icon-left
                 size
                 theme
                 blur?
                 container-style]}]
      [rn/view {:style {:flex 1}}
       [quo/slide-button
        {:size                size
         :container-style     container-style
         :customization-color customization-color
         :on-reset            (when @reset-slider? #(reset! reset-slider? false))
         :on-complete         #(authorize/authorize {:on-close              on-close
                                                     :auth-button-icon-left auth-button-icon-left
                                                     :theme                 theme
                                                     :blur?                 blur?
                                                     :on-enter-password     on-enter-password
                                                     :biometric-auth?       biometric-auth?
                                                     :on-auth-success       on-auth-success
                                                     :on-auth-fail          on-auth-fail
                                                     :auth-button-label     auth-button-label})
         :track-icon          (if biometric-auth? :i/face-id :password)
         :track-text          track-text}]])))

(def view (quo.theme/with-theme view-internal))
