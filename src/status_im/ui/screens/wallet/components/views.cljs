(ns status-im.ui.screens.wallet.components.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.screens.wallet.components.styles :as styles]
            [status-im.i18n :as i18n]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.animation :as animation]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.money :as money]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.wallet.components.animations :as animations]
            [status-im.ui.screens.wallet.utils :as wallet.utils]))

(views/defview tooltip [label & [style]]
  (views/letsubs [bottom-value (animation/create-value 16)
                  opacity-value (animation/create-value 0)]
    {:component-did-mount (animations/animate-tooltip bottom-value opacity-value)}
    [react/view styles/tooltip-container
     [react/animated-view {:style (merge (styles/tooltip-animated bottom-value opacity-value) style)}
      [react/view styles/tooltip-text-container
       [react/text {:style styles/tooltip-text} label]]
      [vector-icons/icon :icons/tooltip-triangle {:color :white :style styles/tooltip-triangle}]]]))

(defn amount-input []
  (let [active? (reagent/atom false)]
    (fn [& [{:keys [input-options style error disabled?]}]]
      (let [{:keys [on-focus on-blur]} input-options]
        [react/view components.styles/flex
         [react/text {:style styles/label} (i18n/label :t/amount)]
         [react/view styles/amount-text-input-container
          [react/view (merge (styles/amount-container @active?) (if disabled? styles/container-disabled style))
           [react/text-input
            (merge
             {:style styles/text-input}
             (if disabled?
               {:editable false}
               {:keyboard-type          :numeric
                :auto-capitalize        "none"
                :placeholder            "0.000"
                :placeholder-text-color "#ffffff66"
                :selection-color        :white
                :style                  styles/text-input
                :on-focus               #(do (reset! active? true)
                                             (when on-focus (on-focus)))
                :on-blur                #(do (reset! active? false)
                                             (when on-blur (on-blur)))})
             (dissoc input-options :on-focus :on-blur))]]
          (when-not (nil? error)
            [tooltip error])]]))))

(views/defview view-currency [style]
  (views/letsubs [visible-tokens [:wallet.settings/visible-tokens]
                  symbol         [:wallet.send/symbol]]
    [react/view
     [react/text {:style styles/label} (i18n/label :t/currency)]
     [react/view (merge styles/currency-container
                        style)
      [react/text
       (name symbol)]]]))

(views/defview choose-currency [style]
  (views/letsubs [visible-tokens [:wallet.settings/visible-tokens]
                  symbol         [:wallet.send/symbol]]
    [react/view
     [react/text {:style styles/label} (i18n/label :t/currency)]
     [react/view (merge styles/currency-container
                        style)
      [react/picker {:selected   (name symbol)
                     :style      {:color "white"}
                     :item-style styles/wallet-name
                     :on-change  #(re-frame/dispatch [:wallet.send/set-symbol (keyword %)])}
       (map (fn [s] {:value (name s) :color "white"}) (conj visible-tokens (:symbol tokens/ethereum)))]]]))

(defn choose-recipient-content [{:keys [address name on-press style]}]
  (let [address? (and (not (nil? address)) (not= address ""))]
    [react/view
     [react/text {:style styles/label} (i18n/label :t/recipient)]
     [react/view (merge styles/recipient-container
                        (when-not on-press styles/container-disabled)
                        style)
      (when name
        [react/view styles/recipient-name-container
         [react/text {:style           (styles/participant true)
                      :number-of-lines 1}
          name]])
      [react/view components.styles/flex
       [react/text {:style           (styles/participant (and (not name) address?))
                    :number-of-lines 1
                    :ellipsizeMode   :middle}
        (if address? address "Choose recipient...")]]
      (when on-press
        [vector-icons/icon :icons/forward {:color :white}])]]))

(defn choose-recipient [{:keys [on-press] :as m}]
  (if on-press
    [react/touchable-highlight {:on-press on-press}
     [react/view ;; TODO(jeluard) remove extra view when migrating to latest RN
      [choose-recipient-content m]]]
    [react/view
     [choose-recipient-content m]]))

(views/defview choose-wallet [& [style]]
  (views/letsubs [network  [:network]
                  balance  [:balance]
                  symbol   [:wallet.send/symbol]]
    (let [amount   (get balance symbol)
          decimals (:decimals (tokens/asset-for (ethereum/network->chain-keyword network) symbol))]
      [react/view
       [react/text {:style styles/label} (i18n/label :t/wallet)]
       [react/view (merge styles/wallet-container
                          style)
        [react/text {:style styles/wallet-name} (i18n/label :t/main-wallet)]
        (if amount
          [react/view {:style styles/wallet-value-container}
           [react/text {:style           (merge styles/wallet-value styles/wallet-value-amount)
                        :number-of-lines 1
                        :ellipsize-mode  :tail}
            (wallet.utils/format-amount amount decimals)]
           [react/text {:style styles/wallet-value}
            (name symbol)]]
          [react/text {:style styles/wallet-value}
           "..."])]])))

(defn separator []
  [react/view styles/separator])

(defn button-text [label]
  [react/text {:style      styles/button-text
               :font       (if platform/android? :medium :default)
               :uppercase? (get-in platform/platform-specific [:uppercase?])} label])

(defn change-display [change]
  (let [pos-change? (or (pos? change) (zero? change))]
    [react/view {:style (if pos-change?
                          styles/today-variation-container-positive
                          styles/today-variation-container-negative)}
     [react/text {:style (if pos-change?
                           styles/today-variation-positive
                           styles/today-variation-negative)}
      (if change
        (str (when pos-change? "+") change "%")
        "-%")]]))
