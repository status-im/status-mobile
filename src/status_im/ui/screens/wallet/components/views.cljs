(ns status-im.ui.screens.wallet.components.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.components.react :as react]
            [status-im.components.styles :as components.styles]
            [status-im.ui.screens.wallet.components.styles :as styles]
            [status-im.i18n :as i18n]
            [reagent.core :as reagent]
            [status-im.components.icons.vector-icons :as vector-icons]
            [status-im.components.animation :as animation]
            [status-im.utils.money :as money]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.wallet.components.animations :as animations]))

(views/defview tooltip [label & [style]]
  (views/letsubs [bottom-value (animation/create-value 16)
                  opacity-value (animation/create-value 0)]
    {:component-did-mount (animations/animate-tooltip bottom-value opacity-value)}
    [react/view styles/tooltip-container
     [react/animated-view {:style (merge (styles/tooltip-animated bottom-value opacity-value) style)}
      [react/view styles/tooltip-text-container
       [react/text {:style styles/tooltip-text} label]]
      [vector-icons/icon :icons/tooltip-triangle {:color :white :style styles/tooltip-triangle}]]]))

;;TODO (andrey) temporary, should be removed later
(defn amount-input-disabled [amount]
  [react/view components.styles/flex
   [react/text {:style styles/label} (i18n/label :t/amount)]
   [react/view styles/amount-text-input-container
    [react/view (merge (styles/amount-container false) styles/container-disabled)
     [react/text-input
      {:editable      false
       :default-value amount
       :style         styles/text-input}]]]])

(defn amount-input []
  (let [active? (reagent/atom false)]
    (fn [& [{:keys [input-options style error]}]]
      (let [{:keys [on-focus on-blur]} input-options]
        [react/view components.styles/flex
         [react/text {:style styles/label} (i18n/label :t/amount)]
         [react/view styles/amount-text-input-container
          [react/view (merge (styles/amount-container @active?) style)
           [react/text-input
            (merge
              {:keyboard-type          :numeric
               :placeholder            "0.000"
               :placeholder-text-color "#ffffff66"
               :selection-color        :white
               :style                  styles/text-input
               :on-focus               #(do (reset! active? true)
                                            (when on-focus (on-focus)))
               :on-blur                #(do (reset! active? false)
                                            (when on-blur (on-blur)))}
              (dissoc input-options :on-focus :on-blur))]]
          (when-not (nil? error)
            [tooltip error])]]))))


;;TODO (andrey) this should be choose component with the list of currencies
(defn choose-currency [& [style]]
  [react/view
   [react/text {:style styles/label} (i18n/label :t/currency)]
   [react/view (merge styles/currency-container
                      style)
    [react/text {:style styles/wallet-name} "ETH"]]])

(defn choose-recipient-disabled [{:keys [address name]}]
  [react/view
   [react/text {:style styles/label} (i18n/label :t/recipient)]
   [react/view (merge styles/recipient-container
                      styles/container-disabled)
    (when name
      [react/view styles/recipient-name-container
       [react/text {:style           (styles/participant true)
                    :number-of-lines 1}
        name]])
    [react/view components.styles/flex
     [react/text {:style           (styles/participant (not name))
                  :number-of-lines 1
                  :ellipsizeMode   :middle}
      address]]]])

(defn choose-recipient [{:keys [address name on-press style]}]
  (let [address? (and (not (nil? address)) (not= address ""))]
    [react/touchable-highlight {:on-press on-press}
     [react/view
      [react/text {:style styles/label} (i18n/label :t/recipient)]
      [react/view (merge styles/recipient-container
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
       [vector-icons/icon :icons/forward {:color :white}]]]]))

;;TODO (andrey) this should be choose component with the list of wallets
(views/defview choose-wallet [& [style]]
  (views/letsubs [balance [:balance]]
    [react/view
     [react/text {:style styles/label} (i18n/label :t/wallet)]
     [react/view (merge styles/wallet-container
                        style)
      [react/text {:style styles/wallet-name} (i18n/label :t/main-wallet)]
      [react/text {:style           styles/wallet-value
                   :number-of-lines 1
                   :ellipsizeMode   :middle}
       (if balance
         (money/wei->str :eth balance)
         "...")]]]))

(defn network-label
  ([n] (network-label [{} n]))
  ([style n] [react/view (merge styles/network-container
                                style)
              [react/text {:style styles/network} n]]))

(defn separator []
  [react/view styles/separator])

(defn button-text [label]
  [react/text {:style      styles/button-text
               :font       (if platform/android? :medium :default)
               :uppercase? (get-in platform/platform-specific [:uppercase?])} label])