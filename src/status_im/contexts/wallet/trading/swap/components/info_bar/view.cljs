(ns status-im.contexts.wallet.trading.swap.components.info-bar.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [status-im.contexts.wallet.common.utils :as utils]
            [status-im.contexts.wallet.trading.swap.components.info-bar.style :as style]
            [status-im.contexts.wallet.trading.swap.components.slippage-settings :as slippage-settings]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn container
  [& args]
  (let [[props children] (if (map? (first args))
                           [(first args) (rest args)]
                           [nil args])]
    (into [rn/view
           {:style [style/bar-container (:style props)]}]
          children)))

(def ^:private default-data-item-props
  {:container-style style/bar-item
   :blur?           false
   :card?           false
   :subtitle-type   :default
   :status          :default
   :size            :small})

(defn network
  []
  (let [{:keys [source full-name]} (rf/sub [:trading.swap/network])]
    [quo/data-item
     (assoc default-data-item-props
            :subtitle-type :network
            :network-image source
            :title         (i18n/label :t/network)
            :subtitle      full-name)]))

(defn fiat-max-fee
  [{:keys [fee-amount subtitle-error?]}]
  (let [theme           (quo.theme/use-theme)
        currency-symbol (rf/sub [:profile/currency-symbol])
        formatted-fee   (utils/fiat-formatted-for-ui currency-symbol
                                                     fee-amount)]
    [quo/data-item
     (assoc default-data-item-props
            :title          (i18n/label :t/max-fees)
            :status         (if fee-amount :default :loading)
            :subtitle       formatted-fee
            :subtitle-color (when subtitle-error?
                              (colors/theme-colors colors/danger-50
                                                   colors/danger-60
                                                   theme)))]))

(defn estimated-time
  [{:keys [estimated-time-mins]}]
  [quo/data-item
   (assoc default-data-item-props
          :title    (i18n/label :t/est-time)
          :status   (if estimated-time-mins :default :loading)
          :subtitle (i18n/label :t/time-in-mins {:minutes (str estimated-time-mins)}))])

(defn max-slippage
  [{:keys [slippage disabled?]}]
  [quo/data-item
   (assoc default-data-item-props
          :title         (i18n/label :t/max-slippage)
          :subtitle      (str slippage "%")
          :subtitle-type :editable
          :icon          :i/edit
          :size          :small
          :on-press      (fn []
                           (when-not disabled?
                             (rf/dispatch [:show-bottom-sheet
                                           {:content slippage-settings/view}]))))])
