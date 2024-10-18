(ns status-im.contexts.settings.keycard.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.resources :as resources]
            [status-im.constants :as constants]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

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
       [:<>]
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
          :on-press            #(rf/dispatch [:open-modal :screen/keycard.check])}]])]))
