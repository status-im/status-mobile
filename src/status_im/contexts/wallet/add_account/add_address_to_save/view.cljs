(ns status-im.contexts.wallet.add-account.add-address-to-save.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.contexts.wallet.add-account.add-address-to-save.style :as style]
    [status-im.contexts.wallet.common.activity-indicator.view :as activity-indicator]
    [status-im.contexts.wallet.common.address-input.view :as address-input]
    [status-im.contexts.wallet.utils :as wallet.utils]
    [status-im.subs.wallet.add-account.address-to-watch]
    [utils.ens.core :as utils.ens]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- on-press-confirm-add-address-to-save
  [input-value]
  (rf/dispatch
   [:wallet/confirm-add-address-to-save
    {:address input-value
     :ens?    (utils.ens/is-valid-eth-name?
               input-value)}]))

(defn view
  []
  (let [addresses (rf/sub [:wallet/lowercased-addresses])
        {:keys [title description input-title accessibility-label]}
        (rf/sub [:wallet/currently-added-address])
        validate #(wallet.utils/validate-fn % addresses)
        customization-color (rf/sub [:profile/customization-color])]
    (rn/use-unmount #(wallet.utils/clear-activity-and-scanned-address))
    (fn []
      (let [activity-state                          (rf/sub [:wallet/watch-address-activity-state])
            validated-address                       (rf/sub [:wallet/watch-address-validated-address])
            [input-value set-input-value]           (rn/use-state "")
            [validation-msg set-validation-message] (rn/use-state "")
            clear-input                             (fn []
                                                      (set-input-value "")
                                                      (set-validation-message "")
                                                      (wallet.utils/clear-activity-and-scanned-address))]
        [rn/view
         {:style {:flex 1}}
         [floating-button-page/view
          {:header [quo/page-nav
                    {:type      :no-title
                     :icon-name :i/close
                     :on-press  wallet.utils/on-press-close}]
           :footer [quo/button
                    {:customization-color customization-color
                     :disabled?           (or (string/blank? input-value)
                                              (some? (validate input-value))
                                              (= activity-state :invalid-ens)
                                              (= activity-state :scanning)
                                              (not validated-address))
                     :on-press            (fn []
                                            (on-press-confirm-add-address-to-save input-value)
                                            (clear-input))
                     :container-style     {:z-index 2}}
                    (i18n/label :t/continue)]}
          [quo/page-top
           {:container-style  style/header-container
            :title            (i18n/label title)
            :description      :text
            :description-text (i18n/label description)}]
          [address-input/view
           {:input-value            input-value
            :validate               validate
            :validation-msg         validation-msg
            :clear-input            clear-input
            :set-validation-message set-validation-message
            :set-input-value        set-input-value
            :input-title            (i18n/label input-title)
            :accessibility-label    accessibility-label}]
          (if validation-msg
            [quo/info-message
             {:accessibility-label :error-message
              :size                :default
              :icon                :i/info
              :type                :error
              :style               style/info-message}
             validation-msg]
            [activity-indicator/view activity-state])]]))))
