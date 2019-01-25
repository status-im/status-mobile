(ns status-im.ui.screens.wallet.components.views
  (:require-macros [status-im.utils.views :as views])
  (:require [clojure.string :as string]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.bottom-buttons.view :as bottom-buttons]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.list.styles :as list.styles]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.screens.wallet.components.styles :as styles]
            [status-im.ui.screens.wallet.choose-recipient.views :as choose-recipient]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.ui.screens.wallet.utils :as wallet.utils]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.money :as money]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.core :as utils.core]
            [status-im.utils.utils :as utils.utils]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.icons.vector-icons :as vector-icons]))

;; Wallet tab has a different coloring scheme (dark) that forces color changes (background, text)
;; It might be replaced by some theme mechanism

(defn text-input [props text]
  [react/text-input (utils.core/deep-merge {:placeholder-text-color colors/white-transparent
                                            :selection-color        colors/white
                                            :style                  {:color          colors/white
                                                                     :font-size      15
                                                                     :height         52
                                                                     :letter-spacing -0.2}}
                                           props)
   text])

(def default-action (actions/back-white actions/default-handler))

(defn toolbar
  ([title] (toolbar {} title))
  ([props title] (toolbar props default-action title))
  ([props action title] (toolbar props action title nil))
  ([props action title options]
   [toolbar/toolbar (utils.core/deep-merge {:style wallet.styles/toolbar}
                                           props)
    [toolbar/nav-button action]
    [toolbar/content-title {:color :white :font-weight "700"}
     title]
    options]))

(defn- top-view [avoid-keyboard?]
  (if avoid-keyboard?
    react/keyboard-avoiding-view
    react/view))

(defn simple-screen
  ([toolbar content] (simple-screen nil toolbar content))
  ([{:keys [avoid-keyboard? status-bar-type]} toolbar content]
   [(top-view avoid-keyboard?) {:flex 1 :background-color colors/blue}
    [status-bar/status-bar {:type (or status-bar-type :wallet)}]
    toolbar
    content]))

(defn- cartouche-content [{:keys [disabled?]} content]
  [react/view {:style (styles/cartouche-content-wrapper disabled?)}
   [react/view {:flex 1}
    content]])

(defn cartouche [{:keys [disabled? on-press icon icon-opts] :or {icon :icons/forward} :as m} header content]
  [react/view {:style styles/cartouche-container}
   [react/text {:style styles/cartouche-header}
    header]
   (if (or disabled? (nil? on-press))
     [cartouche-content m content]
     [react/touchable-highlight {:on-press on-press}
      [react/view
       [cartouche-content m
        (if-not (true? disabled?)
          [react/view styles/cartouche-icon-wrapper
           [react/view {:flex 1} ;; Let content shrink if needed
            content]
           [vector-icons/icon icon (merge {:color :white} icon-opts)]]
          content)]]])])

(defn- cartouche-primary-text [s]
  [react/text {:style styles/cartouche-primary-text}
   s])

(defn cartouche-secondary-text [s]
  [react/text {:style styles/cartouche-secondary-text}
   s])

(defn cartouche-text-content [primary secondary]
  [react/view styles/cartouche-text-wrapper
   [cartouche-primary-text primary]
   [cartouche-secondary-text secondary]])

(defn view-asset [symbol]
  [react/view
   [react/i18n-text {:style styles/label :key :wallet-asset}]
   [react/view styles/asset-container-read-only
    [react/text {:style styles/asset-text}
     (name symbol)]]])

(defn- type->handler [k]
  (case k
    :send    :wallet.send/set-symbol
    :request :wallet.request/set-symbol
    (throw (str "Unknown type: " k))))

(defn- render-token [{:keys [symbol name icon decimals amount] :as token} type]
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
        (wallet.utils/display-symbol token)]]
      [list/item-secondary (wallet.utils/format-amount amount decimals)]]]]])

(views/defview assets [type]
  (views/letsubs [assets [:wallet/transferrable-assets-with-amount]]
    [simple-screen
     [toolbar (i18n/label :t/wallet-assets)]
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

(views/defview asset-selector [{:keys [disabled? type symbol error]}]
  (views/letsubs [balance    [:balance]
                  network    [:network]
                  all-tokens [:wallet/all-tokens]]
    (let [{:keys [name icon decimals] :as token} (tokens/asset-for all-tokens (ethereum/network->chain-keyword network) symbol)]
      (when name
        [react/view
         [cartouche {:disabled? disabled? :on-press #(re-frame/dispatch [:navigate-to (type->view type)])}
          (i18n/label :t/wallet-asset)
          [react/view {:style               styles/asset-content-container
                       :accessibility-label :choose-asset-button}
           [list/item-image (assoc icon :style styles/asset-icon :image-style {:width 32 :height 32})]
           [react/view styles/asset-text-content
            [react/view styles/asset-label-content
             [react/text {:style (merge styles/text-content styles/asset-label)}
              name]
             [react/text {:style styles/text-secondary-content}
              (wallet.utils/display-symbol token)]]
            [react/text {:style (merge styles/text-secondary-content styles/asset-label)}
             (str (wallet.utils/format-amount (get balance symbol) decimals))]]]]
         (when error
           [tooltip/tooltip error {}])]))))

(defn- recipient-address [address modal?]
  [react/text {:style               (merge styles/recipient-address (when-not address styles/recipient-no-address))
               :accessibility-label :recipient-address-text}
   (or (ethereum/normalized-address address)
       (if modal?
         (i18n/label :t/new-contract)
         (i18n/label :t/specify-recipient)))])

(views/defview recipient-contact [address name request?]
  (views/letsubs [contact [:contacts/contact-by-address address]]
    (let [address? (and (not (nil? address)) (not= address ""))]
      [react/view styles/recipient-container
       [react/view styles/recipient-icon
        (when contact
          [photos/photo (:photo-path contact) {:size list.styles/image-size}])]
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
    [photos/photo (:photo-path contact) {:size list.styles/image-size}]
    [list/item-content
     [list/item-primary {:accessibility-label :contact-name-text}
      (:name contact)]
     [react/text {:style list.styles/secondary-text
                  :accessibility-label :contact-address-text}
      (ethereum/normalized-address (:address contact))]]]])

(views/defview recent-recipients []
  (views/letsubs [contacts [:contacts/all-added-people-contacts-with-address]]
    [simple-screen
     [toolbar (i18n/label :t/recipient)]
     [react/view styles/recent-recipients
      [list/flat-list {:data      contacts
                       :key-fn    :address
                       :render-fn render-contact}]]]))

(defn contact-code []
  (let [content (reagent/atom nil)]
    (fn []
      [simple-screen {:avoid-keyboard? true}
       [toolbar {:style wallet.styles/toolbar-bottom-line}
        default-action
        (i18n/label :t/recipient)]
       [react/view components.styles/flex
        [cartouche {}
         (i18n/label :t/recipient)
         [text-input {:multiline           true
                      :style               styles/contact-code-text-input
                      :placeholder         (i18n/label :t/recipient-code)
                      :on-change-text      #(reset! content %)
                      :accessibility-label :recipient-address-input}]]
        [bottom-buttons/bottom-button
         [button/button {:disabled?    (string/blank? @content)
                         :on-press     #(re-frame/dispatch [:wallet.send/set-recipient @content])
                         :fit-to-text? false}
          (i18n/label :t/done)]]]])))

(defn recipient-qr-code []
  [choose-recipient/choose-recipient])

(defn- request-camera-permissions []
  (re-frame/dispatch [:request-permissions {:permissions [:camera]
                                            :on-allowed  #(re-frame/dispatch [:navigate-to :recipient-qr-code])
                                            :on-denied   #(utils.utils/set-timeout
                                                           (fn []
                                                             (utils.utils/show-popup (i18n/label :t/error)
                                                                                     (i18n/label :t/camera-access-error)))
                                                           50)}]))

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

(defn recipient-selector [{:keys [name address disabled? contact-only? request? modal?]}]
  [cartouche {:on-press  #(on-choose-recipient contact-only?)
              :disabled? disabled?
              :icon      :icons/dots-horizontal
              :icon-opts {:accessibility-label :choose-contact-button}}
   (i18n/label :t/wallet-choose-recipient)
   [react/view {:accessibility-label :choose-recipient-button}
    (if name
      [recipient-contact address name request?]
      [recipient-address address modal?])]])

(defn amount-input [{:keys [input-options amount amount-text disabled?]}
                    {:keys [symbol decimals]}]
  [react/view {:style               components.styles/flex
               :accessibility-label :specify-amount-button}
   [text-input
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
       {:editable    false
        :placeholder ""}
       {:keyboard-type       :numeric
        :placeholder         (i18n/label :t/amount-placeholder)
        :style               components.styles/flex
        :accessibility-label :amount-input}))]])

(defn amount-selector [{:keys [error disabled?] :as m} token]
  [react/view
   [cartouche {:disabled? disabled?}
    (i18n/label :t/amount)
    [amount-input m token]]
   (when error
     [tooltip/tooltip error])])

(defn separator []
  [react/view styles/separator])

(defn button-text [label]
  [react/text {:style      styles/button-text
               :font       (if platform/android? :medium :default)
               :uppercase? true}
   label])
