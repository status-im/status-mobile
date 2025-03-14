(ns status-im.contexts.keycard.migrate.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.resources :as resources]
            [status-im.contexts.keycard.common.view :as common.view]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [profile-name        (rf/sub [:profile/name])
        profile-picture     (rf/sub [:profile/image])
        customization-color (rf/sub [:profile/customization-color])]
    [:<>
     [quo/page-nav
      {:icon-name :i/close
       :on-press  events-helper/navigate-back}]
     [quo/page-top
      {:title       (i18n/label :t/ready-to-migrate-key-pair)
       :description :context-tag
       :context-tag {:full-name           profile-name
                     :profile-picture     profile-picture
                     :customization-color customization-color}}]
     [rn/image
      {:resize-mode :contain
       :style       {:flex 1 :width (:width (rn/get-window))}
       :source      (resources/get-image :keycard-migration)}]
     [common.view/tips]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-label (i18n/label :t/scan-keycard)
       :button-one-props {:on-press #(rf/dispatch [:keycard/migration.connect-and-load-keys])}}]]))
