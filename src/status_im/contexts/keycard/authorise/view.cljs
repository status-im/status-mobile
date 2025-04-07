(ns status-im.contexts.keycard.authorise.view
  (:require [quo.context]
            [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.resources :as resources]
            [status-im.common.standard-authentication.core :as standard-auth]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [profile-name         (rf/sub [:profile/name])
        profile-picture      (rf/sub [:profile/image])
        customization-color  (rf/sub [:profile/customization-color])
        {:keys [on-success]} (quo.context/use-screen-params)]
    [:<>
     [quo/page-nav
      {:icon-name :i/close
       :on-press  events-helper/navigate-back}]
     [quo/page-top
      {:title       (i18n/label :t/authorise-with-password)
       :description :context-tag
       :context-tag {:full-name           profile-name
                     :profile-picture     profile-picture
                     :customization-color customization-color}}]
     [rn/view {:style {:flex 1 :padding-horizontal 20}}
      [quo/text (i18n/label :t/migrate-key-pair-authorise)]
      [rn/image
       {:resize-mode :contain
        :style       {:flex 1 :width (- (:width (rn/get-window)) 40)}
        :source      (resources/get-image :keycard-migration)}]
      [standard-auth/slide-auth
       {:size                :size-48
        :container-style     {}
        :customization-color customization-color
        :track-text          (i18n/label :t/slide-to-authorise)
        :on-success          #(when on-success (on-success %))
        :auth-button-label   (i18n/label :t/confirm)}]]]))
