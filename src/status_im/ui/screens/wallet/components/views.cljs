(ns status-im.ui.screens.wallet.components.views
  (:require-macros [status-im.utils.views :as views])
  (:require [clojure.string :as string]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.bottom-buttons.view :as bottom-buttons]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.list.styles :as list.styles]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.screens.wallet.components :as components]
            [status-im.ui.screens.wallet.components.animations :as animations]
            [status-im.ui.screens.wallet.components.styles :as styles]
            [status-im.ui.screens.wallet.choose-recipient.views :as choose-recipient]
            [status-im.ui.screens.wallet.main.views :as main]
            [status-im.ui.screens.wallet.utils :as wallet.utils]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.platform :as platform]))

(views/defview tooltip [label]
  (views/letsubs [bottom-value (animation/create-value -30)
                  opacity-value (animation/create-value 0)]
    {:component-did-mount (animations/animate-tooltip bottom-value opacity-value)}
    [react/view styles/tooltip-container
     [react/animated-view {:style (styles/tooltip-animated bottom-value opacity-value)}
      [react/view styles/tooltip-text-container
       [react/text {:style styles/tooltip-text} label]]
      [vector-icons/icon :icons/tooltip-triangle {:color :white :style styles/tooltip-triangle}]]]))

(defn view-asset [symbol]
  [react/view
   [react/text {:style styles/label}
    (i18n/label :t/wallet-asset)]
   [react/view styles/asset-container-read-only
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
       [react/text {:style styles/text-list-primary-content}
        name]
       [react/text {:uppercase? true}
        (clojure.core/name symbol)]]
      [list/item-secondary (wallet.utils/format-amount (symbol balance) decimals)]]]]])

(views/defview assets [type]
  (views/letsubs [network        [:network]
                  visible-tokens [:wallet.settings/visible-tokens]
                  balance        [:balance]]
    [components/simple-screen (i18n/label :t/wallet-assets)
     [react/view {:style (assoc components.styles/flex :background-color :white)}
      [list/flat-list {:default-separator? true
                       :data               (concat [tokens/ethereum] (main/current-tokens visible-tokens network))
                       :render-fn          #(render-token % balance type)}]]]))

(defn send-assets []
  [assets :send])

(defn request-assets []
  [assets :request])

(defn- type->view [k]
  (case k
    :send    :wallet-send-assets
    :request :wallet-request-assets
    (throw (str "Unknown type: " k))))

(views/defview asset-selector [{:keys [disabled? type symbol]}]
  (views/letsubs [balance  [:balance]
                  network  [:network]]
    (let [{:keys [name icon decimals]} (tokens/asset-for (ethereum/network->chain-keyword network) symbol)]
      [components/cartouche {:disabled? disabled? :on-press #(re-frame/dispatch [:navigate-to (type->view type)])}
       (i18n/label :t/wallet-asset)
       [react/view styles/asset-content-container
        [list/item-image (assoc icon :style styles/asset-icon :image-style {:width 32 :height 32})]
        [react/view styles/asset-text-content
         [react/view styles/asset-label-content
          [react/text {:style (merge styles/text-content styles/asset-label)}
           name]
          [react/text {:style styles/text-secondary-content}
           (clojure.core/name symbol)]]
         [react/text {:style (merge styles/text-secondary-content styles/asset-label)}
          (str (wallet.utils/format-amount (symbol balance) decimals))]]]])))

(defn- recipient-address [address]
  [react/text {:style (merge styles/recipient-address (when-not address styles/recipient-no-address))}
   (or (ethereum/normalized-address address) (i18n/label :t/specify-recipient))])

(views/defview recipient-contact [address name]
  (views/letsubs [contact [:contact/by-address address]]
    (let [address? (and (not (nil? address)) (not= address ""))]
      [react/view styles/recipient-container
       [react/view styles/recipient-icon
        [chat-icon/chat-icon (:photo-path contact) {:size list.styles/image-size}]]
       [react/view styles/recipient-name
        [react/text {:style           (styles/participant true)
                     :number-of-lines 1}
         name]
        [react/text {:style (styles/participant (and (not name) address?))}
         (ethereum/normalized-address address)]]])))

(defn render-contact [contact]
  [list/touchable-item #(re-frame/dispatch [:wallet/fill-request-from-contact contact])
   [list/item
    [chat-icon/chat-icon (:photo-path contact) {:size list.styles/image-size}]
    [list/item-content
     [list/item-primary (:name contact)]
     [react/text {:style list.styles/secondary-text}
      (ethereum/normalized-address (:address contact))]]]])

(views/defview recent-recipients []
  (views/letsubs [contacts [:contacts-filtered :all-added-people-contacts]]
    [components/simple-screen
     (i18n/label :t/recipient)
     [react/view styles/recent-recipients
      [list/flat-list {:data      contacts
                       :render-fn render-contact}]]]))

(defn contact-code []
  (let [content (reagent/atom nil)]
    (fn []
      [react/view components.styles/flex
       [components/simple-screen
        (i18n/label :t/recipient)
        [react/view components.styles/flex
         [components/cartouche {}
          (i18n/label :t/recipient)
          [components/text-input {:multiline      true
                                  :placeholder    (i18n/label :t/recipient-code)
                                  :on-change-text #(reset! content %)}]]
         [bottom-buttons/bottom-button
          [button/button {:disabled? (string/blank? @content)
                          :on-press  #(re-frame/dispatch [:wallet/fill-request-from-url @content])}
           (i18n/label :t/done)]]]]])))

(defn recipient-qr-code []
  [choose-recipient/choose-recipient])

(defn- request-camera-permissions []
  (re-frame/dispatch [:request-permissions [:camera]
                      #(re-frame/dispatch [:navigate-to :recipient-qr-code])]))

(defn- on-choose-recipient []
  (list-selection/show {:title   (i18n/label :t/wallet-choose-recipient)
                        :options [{:label  (i18n/label :t/recent-recipients)
                                   :action #(re-frame/dispatch [:navigate-to :recent-recipients])}
                                  {:label  (i18n/label :t/scan-qr)
                                   :action request-camera-permissions}
                                  {:label  (i18n/label :t/enter-contact-code)
                                   :action #(re-frame/dispatch [:navigate-to :contact-code])}]}))

(defn recipient-selector [{:keys [name address disabled?]}]
  [components/cartouche {:on-press on-choose-recipient :disabled? disabled? :icon :icons/dots-horizontal}
   (i18n/label :t/wallet-choose-recipient)
   (if name
     [recipient-contact address name]
     [recipient-address address])])

(defn- amount-input [{:keys [input-options disabled?]}]
  [react/view components.styles/flex
   [components/text-input
    (merge
      (if disabled?
        {:editable false}
        {:keyboard-type :numeric
         :placeholder   (i18n/label :t/amount-placeholder)
         :style         components.styles/flex})
      input-options)]])

(defn amount-selector [{:keys [error disabled?] :as m}]
  [react/view
   [components/cartouche {:disabled? disabled?}
    (i18n/label :t/amount)
    [amount-input m]]
   (when error
     [tooltip error])])

(defn separator []
  [react/view styles/separator])

(defn button-text [label]
  [react/text {:style      styles/button-text
               :font       (if platform/android? :medium :default)
               :uppercase? (get-in platform/platform-specific [:uppercase?])} label])
