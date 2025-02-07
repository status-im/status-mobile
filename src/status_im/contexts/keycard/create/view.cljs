(ns status-im.contexts.keycard.create.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.resources :as resources]
            [status-im.constants :as constants]
            [status-im.contexts.keycard.common.view :as common.view]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  [:<>
   [quo/page-nav
    {:key        :header
     :background :blur
     :icon-name  :i/arrow-left
     :on-press   events-helper/navigate-back}]
   [quo/page-top
    {:title (i18n/label :t/create-profile-keycard)}]
   [rn/view {:style {:padding-horizontal 20 :padding-top 20}}
    [quo/small-option-card
     {:variant             :main
      :title               (i18n/label :t/check-keycard)
      :subtitle            (i18n/label :t/see-keycard-ready)
      :button-label        (i18n/label :t/scan-keycard)
      :accessibility-label :get-keycard
      :image               (resources/get-image :check-your-keycard)
      :on-press            #(rf/dispatch [:keycard/create.check-empty-card])}]
    [rn/view {:style {:height 12}}]
    [quo/small-option-card
     {:variant             :icon
      :title               (i18n/label :t/learn-more-keycard)
      :subtitle            (i18n/label :t/secure-wallet-card)
      :accessibility-label :setup-keycard
      :image               (resources/get-image :use-keycard)
      :on-press            #(rf/dispatch [:browser.ui/open-url constants/get-keycard-url])}]]
   [rn/view {:style {:flex 1}}]
   [common.view/tips]])

(defn- ready-to-add-documentation
  []
  [quo/documentation-drawers
   {:shell? true}
   [quo/text {:size :paragraph-2}
    (i18n/label :t/ready-add-keypair-keycard-documentation)]])

(defn ready-to-add
  []
  [:<>
   [quo/page-nav
    {:icon-name  :i/close
     :on-press   events-helper/navigate-back
     :right-side [{:icon-name :i/info
                   :on-press  #(rf/dispatch [:show-bottom-sheet
                                             {:content ready-to-add-documentation
                                              :theme   :dark
                                              :shell?  true}])}]}]
   [quo/page-top
    {:title            (i18n/label :t/ready-add-keypair-keycard)
     :description      :text
     :description-text ""}]
   [rn/image
    {:resize-mode :contain
     :style       {:flex 1 :align-self :center :margin-vertical 37}
     :source      (resources/get-image :add-key-to-keycard)}]
   [common.view/tips]
   [quo/bottom-actions
    {:actions          :one-action
     :button-one-label (i18n/label :t/scan-keycard)
     :button-one-props {:on-press #(rf/dispatch [:keycard/create.start])}}]])
