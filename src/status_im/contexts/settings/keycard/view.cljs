(ns status-im.contexts.settings.keycard.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.resources :as resources]
            [status-im.constants :as constants]
            [status-im.contexts.settings.keycard.style :as style]
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
    {:title (i18n/label :t/keycard)}]
   [rn/view {:style style/container}
    [quo/small-option-card
     {:variant             :main
      :title               (i18n/label :t/get-keycard)
      :subtitle            (i18n/label :t/secure-wallet-card)
      :button-label        (i18n/label :t/buy-keycard)
      :button-props        {:type       :primary
                            :icon-right :i/external}
      :accessibility-label :get-keycard
      :image               (resources/get-image :keycard-buy)
      :on-press            #(rf/dispatch [:browser.ui/open-url constants/get-keycard-url])}]
    [rn/view {:style style/text-container}
     [quo/text
      {:style  style/text
       :size   :paragraph-2
       :weight :medium}
      (i18n/label :t/own-keycard)]]
    [quo/small-option-card
     {:variant             :icon
      :title               (i18n/label :t/manage-keycard)
      :subtitle            (i18n/label :t/setup-keycard-description)
      :accessibility-label :setup-keycard
      :image               (resources/get-image :use-keycard)
      :on-press            (fn []
                             (rf/dispatch [:open-modal :screen/keycard.check
                                           {:on-press
                                            #(rf/dispatch [:keycard/manage.check-card])}]))}]]])
