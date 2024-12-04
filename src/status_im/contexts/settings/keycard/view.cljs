(ns status-im.contexts.settings.keycard.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.resources :as resources]
            [status-im.constants :as constants]
            [status-im.contexts.settings.keycard.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn registered-keycard
  [{:keys [profile-name profile-image customization-color]}]
  [rn/view {:style style/keycard-row}
   [quo/icon :i/keycard-card
    {:size  20
     :color colors/white-70-blur}]
   [rn/view
    [quo/text profile-name]
    [rn/view {:style style/keycard-owner}
     [quo/user-avatar
      {:full-name           profile-name
       :profile-picture     profile-image
       :customization-color customization-color
       :status-indicator    false
       :ring?               false
       :size                :xxxs}]
     [quo/text
      {:size  :paragraph-2
       :style style/keycard-owner-name}
      profile-name]]]])

(defn registered-keycards
  []
  (let [keycards (rf/sub [:keycard/registered-keycards])]
    [:<>
     [quo/divider-label
      {:counter? false
       :tight?   true
       :blur?    true}
      (i18n/label :t/registered-keycards)]
     [rn/view {:style style/registered-keycards-container}
      (for [keycard keycards]
        ^{:key (:keycard-uid keycard)}
        [registered-keycard keycard])]]))

(defn view
  []
  (let [keycard-profile? (rf/sub [:keycard/keycard-profile?])]
    [:<>
     [quo/page-nav
      {:key        :header
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   events-helper/navigate-back}]
     [quo/page-top
      {:title (i18n/label :t/keycard)}]
     (if keycard-profile?
       [registered-keycards]
       [rn/view {:style {:padding-horizontal 28 :padding-top 20}}
        [quo/small-option-card
         {:variant             :main
          :title               (i18n/label :t/get-keycard)
          :subtitle            (i18n/label :t/secure-wallet-card)
          :button-label        (i18n/label :t/buy-keycard)
          :accessibility-label :get-keycard
          :image               (resources/get-image :keycard-buy)
          :on-press            #(rf/dispatch [:browser.ui/open-url constants/get-keycard-url])}]
        [rn/view {:style {:margin-top 24}}
         [quo/text
          {:style  {:margin-bottom 1
                    :color         colors/white-opa-70}
           :size   :paragraph-2
           :weight :medium}
          (i18n/label :t/own-keycard)]]
        [quo/small-option-card
         {:variant             :icon
          :title               (i18n/label :t/setup-keycard)
          :subtitle            (i18n/label :t/ready-keycard)
          :accessibility-label :setup-keycard
          :image               (resources/get-image :use-keycard)
          :on-press            (fn []
                                 (rf/dispatch [:open-modal :screen/keycard.check
                                               {:on-press
                                                #(rf/dispatch
                                                  [:keycard/migration.check-empty-card])}]))}]])]))
