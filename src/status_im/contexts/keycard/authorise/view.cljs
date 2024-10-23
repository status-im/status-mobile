(ns status-im.contexts.keycard.authorise.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.standard-authentication.core :as standard-auth]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [profile-name         (rf/sub [:profile/name])
        profile-picture      (rf/sub [:profile/image])
        customization-color  (rf/sub [:profile/customization-color])
        {:keys [on-success]} (rf/sub [:get-screen-params])]
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
     [rn/view {:style {:flex 1 :padding-horizontal 20 :justify-content :space-between}}
      [quo/text (i18n/label :t/migrate-key-pair-authorise)]
      [standard-auth/slide-button
       {:size                :size-48
        :container-style     {}
        :customization-color customization-color
        :track-text          (i18n/label :t/slide-to-authorise)
        :on-auth-success     #(when on-success (on-success %))
        :auth-button-label   (i18n/label :t/confirm)}]]]))
