(ns status-im.contexts.keycard.migrate.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.resources :as resources]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [profile-name        (rf/sub [:profile/name])
        profile-picture     (rf/sub [:profile/image])
        customization-color (rf/sub [:profile/customization-color])
        initialized?        (rf/sub [:keycard/initialized?])]
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
     [rn/view {:style {:flex 1 :align-items :center :justify-content :center}}
      [rn/image
       {:resize-mode :contain
        :source      (resources/get-image :keycard-migration)}]]
     [quo/divider-label (i18n/label :t/tips-scan-keycard)]
     [quo/markdown-list {:description (i18n/label :t/remove-phone-case)}]
     [quo/markdown-list {:description (i18n/label :t/keep-card-steady)}]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-label (if initialized? (i18n/label :t/enter-keycard-pin) (i18n/label :t/scan-keycard))
       :button-one-props {:on-press
                          (fn []
                            (if initialized?
                              (do
                                (rf/dispatch [:navigate-back])
                                (rf/dispatch [:open-modal :screen/keycard.pin.enter
                                              {:on-complete #(rf/dispatch
                                                              [:keycard/migration.pin-entered %])}]))
                              (rf/dispatch [:keycard/migration.start])))}}]]))
