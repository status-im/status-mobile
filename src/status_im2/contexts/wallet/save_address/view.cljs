(ns status-im2.contexts.wallet.save-address.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [re-frame.core :as rf]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.common.floating-button-page.view :as floating-button-page]
    [status-im2.contexts.wallet.common.account-subtitle.view :as subtitle]
    [status-im2.contexts.wallet.save-address.style :as style]
    [utils.i18n :as i18n]))

(defn- f-view
  [_]
  (let [account-color (reagent/atom :blue)
        address-name  (reagent/atom "")
        address       (reagent/atom "0x39cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2")]
    (fn [{:keys [theme]}]
      [floating-button-page/view
       [quo/page-nav
        {:type       :no-title
         :text-align :left
         :right-side []
         :background :blur
         :icon-name  :i/close
         :on-press   #(rf/dispatch [:dismiss-modal :wallet-save-address])}]

       [rn/view {:style {:flex 1}}
        [quo/wallet-user-avatar
         {:container-style     {:margin-bottom     12
                                :margin-horizontal 20}
          :f-name              @address-name
          :l-name              @address-name
          :customization-color @account-color}]
        [quo/title-input
         {:blur?           true
          :on-change-text  #(reset! address-name %)
          :auto-focus      true
          :placeholder     "Address Name"
          :max-length      24
          :container-style {:margin-bottom     16
                            :margin-horizontal 20}}]
        [quo/divider-line]
        [rn/view
         {:style style/color-picker-container}
         [quo/text
          {:size   :paragraph-2
           :weight :medium
           :style  (style/color-label theme)}
          (i18n/label :t/colour)]
         [quo/color-picker
          {:default-selected @account-color
           :on-change        #(reset! account-color %)
           :container-style  {:padding-horizontal 12
                              :padding-vertical   12}}]]
        [quo/divider-line]
        [quo/data-item
         {:container-style     {:margin-top        20
                                :margin-horizontal 20}
          :description         :default
          :on-press            #(js/alert "to be implemented")
          :icon-right?         true
          :right-icon          :i/advanced
          :card?               true
          :label               :none
          :status              :default
          :size                :default
          :title               "Address"
          :customization-color @account-color
          :custom-subtitle     (fn [] [subtitle/view
                                       {:networks [{:short-name   "eth"
                                                    :network-name :ethereum}
                                                   {:short-name   "arb1"
                                                    :network-name :arbitrum}
                                                   {:short-name   "opt"
                                                    :network-name :optimism}]
                                        :address  @address}])}]]

       [quo/button
        {:container-style     {:z-index 2}
         :customization-color @account-color
         :on-press            #(js/alert "to be implemented")}
        "Save address"]])))

(defn view-internal [props] [:f> f-view props])

(def view (quo.theme/with-theme view-internal))
