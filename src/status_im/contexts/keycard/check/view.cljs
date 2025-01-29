(ns status-im.contexts.keycard.check.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.resources :as resources]
            [status-im.constants :as constants]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- keycard-documentation
  []
  [quo/documentation-drawers
   {:title  (i18n/label :t/keycard)
    :shell? true}
   [rn/view
    [quo/text {:size :paragraph-2}
     (i18n/label :t/keycard-documentation)]
    [quo/button
     {:size                40
      :type                :primary
      :container-style     {:margin-top 24 :margin-bottom 12}
      :background          :blur
      :icon-right          :i/external
      :on-press            #(rf/dispatch [:browser.ui/open-url constants/get-keycard-url])
      :accessibility-label :get-keycard}
     (i18n/label :t/buy-keycard)]]])

(defn view
  []
  (let [{:keys [on-press]} (rf/sub [:get-screen-params])]
    [:<>
     [quo/page-nav
      {:icon-name  :i/arrow-left
       :on-press   events-helper/navigate-back
       :right-side [{:icon-name :i/info
                     :on-press  #(rf/dispatch [:show-bottom-sheet
                                               {:content keycard-documentation
                                                :shell?  true}])}]}]
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
