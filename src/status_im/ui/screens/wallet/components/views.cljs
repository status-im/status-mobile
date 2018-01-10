(ns status-im.ui.screens.wallet.components.views
  (:require-macros [status-im.utils.views :as views])
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.screens.wallet.components.styles :as styles]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.wallet.components.animations :as animations]
            [status-im.ui.screens.wallet.main.views :as main]
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
                :placeholder            (i18n/label :t/amount-placeholder)
                :placeholder-text-color components.styles/color-white-transparent
                :selection-color        components.styles/color-white
                :style                  styles/text-input
                :on-focus               #(do (reset! active? true)
                                             (when on-focus (on-focus)))
                :on-blur                #(do (reset! active? false)
                                             (when on-blur (on-blur)))})
             (dissoc input-options :on-focus :on-blur))]]
          (when-not (nil? error)
            [tooltip error])]]))))

(views/defview view-asset [symbol]
  [react/view
   [react/text {:style styles/label}
    (i18n/label :t/wallet-asset)]
   [react/view styles/asset-container
    [react/text {:style styles/asset-text}
     (name symbol)]]])

(defn- type->handler [k]
  (case k
    :send    :wallet.send/set-symbol
    :request :wallet.request/set-symbol
    (throw (str "Unknown type: " k))))

(defn- render-token [{:keys [symbol name icon decimals]} balance type]
  [list/touchable-item  #(do (re-frame/dispatch [(type->handler type) symbol])
                             (re-frame/dispatch [:navigate-back]))
   [react/view
    [list/item
     [list/item-image icon]
     [list/item-content
      [react/view {:flex-direction :row}
       [react/text {:style           (merge styles/text-list-primary-content)
                    :number-of-lines 1}
        name]
       [react/text {:uppercase?      true
                    :number-of-lines 1}
        (clojure.core/name symbol)]]
      [list/item-secondary (wallet.utils/format-amount (symbol balance) decimals)]]]]])

(views/defview assets [type]
  (views/letsubs [network        [:network]
                  visible-tokens [:wallet.settings/visible-tokens]
                  balance        [:balance]]
    [react/view components.styles/flex
     [status-bar/status-bar {}]
     [toolbar/toolbar {}
      [toolbar/nav-clear-text (i18n/label :t/done)]
      [toolbar/content-title (i18n/label :t/wallet-assets)]]
     [react/view {:style components.styles/flex}
      [list/flat-list {:data      (concat [tokens/ethereum] (main/current-tokens visible-tokens network))
                       :render-fn #(render-token % balance type)}]]]))

(defn send-assets []
  [assets :send])

(defn request-assets []
  [assets :request])

(defn- type->view [k]
  (case k
    :send    :wallet-send-assets
    :request :wallet-request-assets
    (throw (str "Unknown type: " k))))

(views/defview choose-asset [{:keys [type symbol]}]
  (views/letsubs [balance  [:balance]
                  network  [:network]]
    (let [{:keys [name icon decimals]} (tokens/asset-for (ethereum/network->chain-keyword network) symbol)]
      [react/view
       [react/text {:style styles/label}
        (i18n/label :t/wallet-asset)]
       [react/touchable-highlight {:style    styles/asset-container
                                   :on-press #(re-frame/dispatch [:navigate-to (type->view type)])}
        [react/view styles/asset-content-container
         [list/item-image (assoc icon :style styles/asset-icon :image-style {:width 32 :height 32})]
         [react/view styles/asset-text-content
          [react/view styles/asset-label-content
           [react/text {:style (merge styles/text-content styles/asset-label)}
            name]
           [react/text {:style styles/text-secondary-content}
            (clojure.core/name symbol)]]
          [react/text {:style (merge styles/text-secondary-content styles/asset-label)}
           (str (wallet.utils/format-amount (symbol balance) decimals))]]
         [vector-icons/icon :icons/forward {:color :white}]]]])))

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
