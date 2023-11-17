(ns status-im2.common.standard-authentication.standard-auth.button.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [reagent.core :as reagent]
    [status-im2.common.standard-authentication.standard-auth.authorize :as authorize]))

(defn- view-internal
  [_]
  (let [reset-slider? (reagent/atom false)
        on-close      #(reset! reset-slider? true)]
    (fn [{:keys [biometric-auth?
                 customization-color
                 auth-button-label
                 on-enter-password
                 on-auth-success
                 on-press
                 on-auth-fail
                 auth-button-icon-left
                 size
                 button-label
                 theme
                 blur?
                 container-style
                 icon-left]}]
      [quo/button
       {:size                size
        :container-style     container-style
        :customization-color customization-color
        :icon-left           icon-left
        :on-press            (if on-press
                               on-press
                               #(authorize/authorize {:on-close              on-close
                                                      :auth-button-icon-left auth-button-icon-left
                                                      :theme                 theme
                                                      :blur?                 blur?
                                                      :on-enter-password     on-enter-password
                                                      :biometric-auth?       biometric-auth?
                                                      :on-auth-success       on-auth-success
                                                      :on-auth-fail          on-auth-fail
                                                      :auth-button-label     auth-button-label}))}
       button-label])))

(def view (quo.theme/with-theme view-internal))
