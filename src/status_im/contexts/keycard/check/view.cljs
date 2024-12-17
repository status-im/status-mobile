(ns status-im.contexts.keycard.check.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.resources :as resources]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [{:keys [on-press]} (rf/sub [:get-screen-params])]
    [:<>
     [quo/page-nav
      {:icon-name :i/arrow-left
       :on-press  events-helper/navigate-back}]
     [quo/page-top
      {:title            (i18n/label :t/check-keycard)
       :description      :text
       :description-text (i18n/label :t/see-keycard-ready)}]
     [rn/view {:style {:flex 1 :align-items :center :justify-content :center}}
      [rn/image
       {:resize-mode :contain
        :source      (resources/get-image :check-your-keycard)}]]
     [quo/divider-label (i18n/label :t/tips-scan-keycard)]
     [quo/markdown-list {:description (i18n/label :t/remove-phone-case)}]
     [quo/markdown-list {:description (i18n/label :t/keep-card-steady)}]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-label (i18n/label :t/ready-to-scan)
       :button-one-props {:on-press on-press}}]]))
