(ns status-im.ui.screens.wallet.components.views
  (:require-macros [status-im.utils.views :as views])
  (:require [clojure.string :as string]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.bottom-buttons.view :as bottom-buttons]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.list.styles :as list.styles]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.screens.wallet.components :as components]
            [status-im.ui.screens.wallet.components.styles :as styles]
            [status-im.ui.screens.wallet.choose-recipient.views :as choose-recipient]
            [status-im.ui.screens.wallet.views :as wallet]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.ui.screens.wallet.utils :as wallet.utils]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.money :as money]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.utils.utils :as utils]))

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

(defn- render-token [{:keys [symbol name icon decimals amount]} type]
  [list/touchable-item  #(do (re-frame/dispatch [(type->handler type) symbol])
                             (re-frame/dispatch [:navigate-back]))
   [react/view
    [list/item
     [list/item-image icon]
     [list/item-content
      [react/view {:flex-direction :row}
       [react/text {:style styles/text-list-primary-content}
        name]
       [react/text {:force-uppercase? true}
        (clojure.core/name symbol)]]
      [list/item-secondary (wallet.utils/format-amount amount decimals)]]]]])

(views/defview assets [type]
  (views/letsubs [assets [:wallet/visible-assets-with-amount]]
    [components/simple-screen
     [components/toolbar (i18n/label :t/wallet-assets)]
     [react/view {:style (assoc components.styles/flex :background-color :white)}
      [list/flat-list {:default-separator? true
                       :data               assets
                       :key-fn             (comp str :symbol)
                       :render-fn          #(render-token % type)}]]]))

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
       [react/view {:style               styles/asset-content-container
                    :accessibility-label :choose-asset-button}
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
  [react/text {:style               (merge styles/recipient-address (when-not address styles/recipient-no-address))
               :accessibility-label :recipient-address-text}
   (or (ethereum/normalized-address address) (i18n/label :t/specify-recipient))])

(views/defview recipient-contact [address name request?]
  (views/letsubs [contact [:get-contact-by-address address]]
    (let [address? (and (not (nil? address)) (not= address ""))]
      [react/view styles/recipient-container
       [react/view styles/recipient-icon
        [chat-icon/chat-icon (:photo-path contact) {:size list.styles/image-size}]]
       [react/view {:style styles/recipient-name}
        [react/text {:style               (styles/participant true)
                     :accessibility-label (if request? :contact-name-text :recipient-name-text)
                     :number-of-lines     1}
         name]
        [react/text {:style               (styles/participant (and (not name) address?))
                     :accessibility-label (if request? :contact-address-text :recipient-address-text)}
         (ethereum/normalized-address address)]]])))

(defn render-contact [contact]
  [list/touchable-item #(re-frame/dispatch [:wallet/fill-request-from-contact contact])
   [list/item
    [chat-icon/chat-icon (:photo-path contact) {:size list.styles/image-size}]
    [list/item-content
     [list/item-primary {:accessibility-label :contact-name-text}
      (:name contact)]
     [react/text {:style list.styles/secondary-text
                  :accessibility-label :contact-address-text}
      (ethereum/normalized-address (:address contact))]]]])

(views/defview recent-recipients []
  (views/letsubs [contacts [:all-added-people-contacts]]
    [components/simple-screen
     [components/toolbar (i18n/label :t/recipient)]
     [react/view styles/recent-recipients
      [list/flat-list {:data      contacts
                       :key-fn    :address
                       :render-fn render-contact}]]]))

(defn contact-code []
  (let [content (reagent/atom nil)]
    (fn []
      [components/simple-screen {:avoid-keyboard? true}
       [components/toolbar {:style wallet.styles/toolbar-bottom-line}
        components/default-action
        (i18n/label :t/recipient)]
       [react/view components.styles/flex
        [components/cartouche {}
         (i18n/label :t/recipient)
         [components/text-input {:multiline           true
                                 :style               styles/contact-code-text-input
                                 :placeholder         (i18n/label :t/recipient-code)
                                 :on-change-text      #(reset! content %)
                                 :accessibility-label :recipient-address-input}]]
        [bottom-buttons/bottom-button
         [button/button {:disabled?    (string/blank? @content)
                         :on-press     #(re-frame/dispatch [:wallet/fill-request-from-url @content])
                         :fit-to-text? false}
          (i18n/label :t/done)]]]])))

(defn recipient-qr-code []
  [choose-recipient/choose-recipient])

(defn- request-camera-permissions []
  (re-frame/dispatch [:request-permissions {:permissions [:camera]
                                            :on-allowed  #(re-frame/dispatch [:navigate-to :recipient-qr-code])
                                            :on-denied   #(utils/show-popup (i18n/label :t/error)
                                                                            (i18n/label :t/camera-access-error))}]))

(defn- on-choose-recipient [contact-only?]
  (list-selection/show {:title   (i18n/label :t/wallet-choose-recipient)
                        :options (concat
                                  [{:label  (i18n/label :t/recent-recipients)
                                    :action #(re-frame/dispatch [:navigate-to :recent-recipients])}]
                                  (when-not contact-only?
                                    [{:label  (i18n/label :t/scan-qr)
                                      :action request-camera-permissions}
                                     {:label  (i18n/label :t/recipient-code)
                                      :action #(re-frame/dispatch [:navigate-to :contact-code])}]))}))

(defn recipient-selector [{:keys [name address disabled? contact-only? request?]}]
  [components/cartouche {:on-press  #(on-choose-recipient contact-only?)
                         :disabled? disabled?
                         :icon      :icons/dots-horizontal
                         :icon-opts {:accessibility-label :choose-contact-button}}
   (i18n/label :t/wallet-choose-recipient)
   [react/view {:accessibility-label :choose-recipient-button}
    (if name
      [recipient-contact address name request?]
      [recipient-address address])]])

(defn- amount-input [{:keys [input-options amount amount-text disabled?]}
                     {:keys [symbol decimals]}]
  [react/view {:style               components.styles/flex
               :accessibility-label :specify-amount-button}
   [components/text-input
    (merge
     input-options
     ;; We only auto-correct and prettify user's input when it is valid and positive.
     ;; Otherwise, user might want to fix his input and autocorrection will give more harm than good.
     ;; Positive check is because we don't want to replace unfinished 0.000 with just plain 0, that is annoying and
     ;; potentially dangerous on this screen (e.g. sending 7 ETH instead of 0.0007)
     {:default-value  (if (empty? amount-text)
                        (str (money/to-fixed (money/internal->formatted amount symbol decimals)))
                        amount-text)}
     (if disabled?
       {:editable false}
       {:keyboard-type       :numeric
        :placeholder         (i18n/label :t/amount-placeholder)
        :style               components.styles/flex
        :accessibility-label :amount-input}))]])

(defn amount-selector [{:keys [error disabled?] :as m} token]
  [react/view
   [components/cartouche {:disabled? disabled?}
    (i18n/label :t/amount)
    [amount-input m]]
   (when error
     [tooltip/tooltip error])])

(defn separator []
  [react/view styles/separator])

(defn button-text [label]
  [react/text {:style      styles/button-text
               :font       (if platform/android? :medium :default)
               :uppercase? true}
   label])
