(ns status-im.ui.screens.wallet.send.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.contact.db :as contact.db]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.bottom-buttons.view :as bottom-buttons]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.list.styles :as list.styles]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.wallet.components.styles :as wallet.components.styles]
            [status-im.ui.screens.wallet.components.views :as components]
            [status-im.ui.screens.wallet.components.views :as wallet.components]
            [status-im.ui.screens.wallet.db :as wallet.db]
            [status-im.ui.screens.wallet.send.animations :as send.animations]
            [status-im.ui.screens.wallet.send.styles :as styles]
            [status-im.ui.screens.wallet.send.events :as events]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.ui.screens.wallet.main.views :as wallet.main.views]
            [status-im.utils.money :as money]
            [status-im.utils.security :as security]
            [status-im.utils.utils :as utils]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.ethereum.ens :as ens]
            [taoensso.timbre :as log]
            [reagent.core :as reagent]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.wallet.utils :as wallet.utils]))

(defn- toolbar [modal? title]
  (let [action (if modal? actions/close-white actions/back-white)]
    [toolbar/toolbar {:style wallet.styles/toolbar}
     [toolbar/nav-button (action (if modal?
                                   #(re-frame/dispatch [:wallet/discard-transaction-navigate-back])
                                   #(actions/default-handler)))]
     [toolbar/content-title {:color :white :font-weight :bold :font-size 17} title]]))

(defn- advanced-cartouche [native-currency {:keys [max-fee gas gas-price]}]
  [react/view
   [wallet.components/cartouche {:on-press  #(do (re-frame/dispatch [:wallet.send/clear-gas])
                                                 (re-frame/dispatch [:navigate-to :wallet-transaction-fee]))}
    (i18n/label :t/wallet-transaction-fee)
    [react/view {:style               styles/advanced-options-text-wrapper
                 :accessibility-label :transaction-fee-button}
     [react/text {:style styles/advanced-fees-text}
      (str max-fee " " (wallet.utils/display-symbol native-currency))]
     [react/text {:style styles/advanced-fees-details-text}
      (str (money/to-fixed gas) " * " (money/to-fixed (money/wei-> :gwei gas-price)) (i18n/label :t/gwei))]]]])

(defn- advanced-options [advanced? native-currency transaction scroll]
  [react/view {:style styles/advanced-wrapper}
   [react/touchable-highlight {:on-press (fn []
                                           (re-frame/dispatch [:wallet.send/toggle-advanced (not advanced?)])
                                           (when (and scroll @scroll) (utils/set-timeout #(.scrollToEnd @scroll) 350)))}
    [react/view {:style styles/advanced-button-wrapper}
     [react/view {:style               styles/advanced-button
                  :accessibility-label :advanced-button}
      [react/i18n-text {:style (merge wallet.components.styles/label
                                      styles/advanced-label)
                        :key   :wallet-advanced}]
      [vector-icons/icon (if advanced? :main-icons/dropdown-up :main-icons/dropdown) {:color :white}]]]]
   (when advanced?
     [advanced-cartouche native-currency transaction])])

(defview password-input-panel [message-label spinning?]
  (letsubs [account         [:account/account]
            wrong-password? [:wallet.send/wrong-password?]
            signing-phrase  (:signing-phrase @account)
            bottom-value    (animation/create-value -250)
            opacity-value   (animation/create-value 0)]
    {:component-did-mount #(send.animations/animate-sign-panel opacity-value bottom-value)}
    [react/animated-view {:style (styles/animated-sign-panel bottom-value)}
     (when wrong-password?
       [tooltip/tooltip (i18n/label :t/wrong-password) styles/password-error-tooltip])
     [react/animated-view {:style (styles/sign-panel opacity-value)}
      [react/view styles/spinner-container
       (when spinning?
         [react/activity-indicator {:animating true
                                    :size      :large}])]
      [react/view styles/signing-phrase-container
       [react/text {:style               styles/signing-phrase
                    :accessibility-label :signing-phrase-text}
        signing-phrase]]
      [react/i18n-text {:style styles/signing-phrase-description :key message-label}]
      [react/view {:style                       styles/password-container
                   :important-for-accessibility :no-hide-descendants}
       [react/text-input
        {:auto-focus             true
         :secure-text-entry      true
         :placeholder            (i18n/label :t/enter-password)
         :placeholder-text-color colors/gray
         :on-change-text         #(re-frame/dispatch [:wallet.send/set-password (security/mask-data %)])
         :style                  styles/password
         :accessibility-label    :enter-password-input
         :auto-capitalize        :none}]]]]))

;; "Cancel" and "Sign Transaction >" or "Sign >" buttons, signing with password
(defview enter-password-buttons [spinning? cancel-handler sign-handler sign-label]
  (letsubs [sign-enabled? [:wallet.send/sign-password-enabled?]
            network-status [:network-status]]
    [bottom-buttons/bottom-buttons
     styles/sign-buttons
     [button/button {:style               components.styles/flex
                     :on-press            cancel-handler
                     :accessibility-label :cancel-button}
      (i18n/label :t/cancel)]
     [button/button {:style               (wallet.styles/button-container sign-enabled?)
                     :on-press            sign-handler
                     :disabled?           (or spinning?
                                              (not sign-enabled?)
                                              (= :offline network-status))
                     :accessibility-label :sign-transaction-button}
      (i18n/label sign-label)
      [vector-icons/icon :main-icons/next {:color colors/white}]]]))

;; "Sign Transaction >" button
(defn- sign-transaction-button [amount-error to amount sufficient-funds? sufficient-gas? modal? online?]
  (let [sign-enabled? (and (nil? amount-error)
                           (or modal? (not (empty? to))) ;;NOTE(goranjovic) - contract creation will have empty `to`
                           (not (nil? amount))
                           sufficient-funds?
                           sufficient-gas?
                           online?)]
    [bottom-buttons/bottom-buttons
     styles/sign-buttons
     [react/view]
     [button/button {:style               components.styles/flex
                     :disabled?           (not sign-enabled?)
                     :on-press            #(re-frame/dispatch [:set-in
                                                               [:wallet :send-transaction :show-password-input?]
                                                               true])
                     :text-style          {:color :white}
                     :accessibility-label :sign-transaction-button}
      (i18n/label :t/transactions-sign-transaction)
      [vector-icons/icon :main-icons/next {:color (if sign-enabled? colors/white colors/white-light-transparent)}]]]))

(defn- render-send-transaction-view [{:keys [modal? transaction scroll advanced? network all-tokens amount-input network-status]}]
  (let [{:keys [amount amount-text amount-error asset-error show-password-input? to to-name sufficient-funds?
                sufficient-gas? in-progress? from-chat? symbol]} transaction
        chain                        (ethereum/network->chain-keyword network)
        native-currency              (tokens/native-currency chain)
        {:keys [decimals] :as token} (tokens/asset-for all-tokens chain symbol)
        online? (= :online network-status)]
    [wallet.components/simple-screen {:avoid-keyboard? (not modal?)
                                      :status-bar-type (if modal? :modal-wallet :wallet)}
     [toolbar modal? (i18n/label :t/send-transaction)]
     [react/view components.styles/flex
      [common/network-info {:text-color :white}]
      [react/scroll-view {:keyboard-should-persist-taps :always
                          :ref                          #(reset! scroll %)
                          :on-content-size-change       #(when (and (not modal?) scroll @scroll)
                                                           (.scrollToEnd @scroll))}
       (when-not online?
         [wallet.main.views/snackbar :t/error-cant-send-transaction-offline])
       [react/view styles/send-transaction-form
        [components/recipient-selector {:disabled? (or from-chat? modal?)
                                        :address   to
                                        :name      to-name
                                        :modal?    modal?}]
        [components/asset-selector {:disabled? (or from-chat? modal?)
                                    :error     asset-error
                                    :type      :send
                                    :symbol    symbol}]
        [components/amount-selector {:disabled?     (or from-chat? modal?)
                                     :error         (or amount-error
                                                        (when-not sufficient-funds? (i18n/label :t/wallet-insufficient-funds))
                                                        (when-not sufficient-gas? (i18n/label :t/wallet-insufficient-gas)))
                                     :amount        amount
                                     :amount-text   amount-text
                                     :input-options {:on-change-text #(re-frame/dispatch [:wallet.send/set-and-validate-amount % symbol decimals])
                                                     :ref            (partial reset! amount-input)}} token]
        [advanced-options advanced? native-currency transaction scroll]]]
      (if show-password-input?
        [enter-password-buttons in-progress?
         #(re-frame/dispatch [:wallet/cancel-entering-password])
         #(re-frame/dispatch [:wallet/send-transaction])
         :t/transactions-sign-transaction]
        [sign-transaction-button amount-error to amount sufficient-funds? sufficient-gas? modal? online?])
      (when show-password-input?
        [password-input-panel :t/signing-phrase-description in-progress?])
      (when in-progress? [react/view styles/processing-view])]]))

(defn address-entry []
  (reagent/create-class
   {:reagent-render
    (fn [opts]
      [react/view [react/text "Select address"]])}))

;; ----------------------------------------------------------------------
;; Step 1 choosing an address or contact to send the transaction to
;; ----------------------------------------------------------------------

(defn simple-tab-navigator
  "A simple tab navigator that that takes a map of tabs and the key of
  the starting tab 

  Example:
  (simple-tab-navigator
   {:main {:name \"Main\" :component (fn [] [react/text \"Hello\"])}
    :other {:name \"Other\" :component (fn [] [react/text \"Goodbye\"])}}
   :main)"
  [tab-map default-key]
  {:pre [(keyword? default-key)]}
  (let [tab-key (reagent/atom default-key)]
    (fn [tab-map _]
      (let [tab-name @tab-key]
        [react/view {:flex 1}
         ;; tabs row
         [react/view {:flex-direction :row}
          (map (fn [[key {:keys [name component]}]]
                 (let [current? (= key tab-name)]
                   ^{:key (str key)}
                   [react/view {:flex             1
                                :background-color colors/black-transparent}
                    [react/touchable-highlight {:on-press #(reset! tab-key key)
                                                :disabled current?}
                     [react/view {:height              44
                                  :align-items         :center
                                  :justify-content     :center
                                  :border-bottom-width 2
                                  :border-bottom-color (if current? colors/white "rgba(0,0,0,0)")}
                      [react/text {:style {:color (if current? :white "rgba(255,255,255,0.4)")
                                           :font-size 15}} name]]]]))
               tab-map)]
         (when-let [component-thunk (some-> tab-map tab-name :component)]
           [component-thunk])]))))

;; just a helper for the buttons in choose address view
(defn- address-button [{:keys [disabled? on-press underlay-color background-color]} content]
  [react/touchable-highlight {:underlay-color underlay-color
                              :disabled disabled?
                              :on-press on-press
                              :style {:height 44
                                      :background-color background-color
                                      :border-radius 8
                                      :flex 1
                                      :align-items :center
                                      :justify-content :center
                                      :margin 3}}
   content])

;; event code

(defn open-qr-scanner [chain text-input transaction]
  (.blur @text-input)
  (re-frame/dispatch [:navigate-to :recipient-qr-code
                      {:on-recipient
                       (fn [qr-data]
                         (if-let [parsed-qr-data (events/extract-qr-code-details chain qr-data)]
                           (let [{:keys [chain-id]} parsed-qr-data
                                 tx-data (events/qr-data->transaction-data parsed-qr-data)]
                             (if (= chain-id (ethereum/chain-keyword->chain-id chain))
                               (swap! transaction merge tx-data)
                               (utils/show-popup (i18n/label :t/error)
                                                 (i18n/label :t/wallet-invalid-chain-id {:data  qr-data
                                                                                         :chain chain-id}))))
                           (utils/show-confirmation
                            {:title               (i18n/label :t/error)
                             :content             (i18n/label :t/wallet-invalid-address {:data qr-data})
                             :cancel-button-text  (i18n/label :t/see-it-again)
                             :confirm-button-text (i18n/label :t/got-it)
                             :on-cancel           (partial open-qr-scanner chain text-input transaction)})))}]))

(defn update-recipient [chain transaction error-message value]
  (if (ens/is-valid-eth-name? value)
    (do (ens/get-addr (get @re-frame.db/app-db :web3)
                      (get ens/ens-registries chain)
                      value
                      #(if (ethereum/address? %)
                         (swap! transaction assoc :to-ens value :to %)
                         (reset! error-message "Unknown ENS name"))))
    (do (swap! transaction assoc :to value)
        (reset! error-message nil))))

(defn choose-address-view
  "A view that allows you to choose an address"
  [{:keys [chain transaction on-address]}]
  {:pre [(keyword? chain) (fn? on-address)]}
  (fn []
    (let [error-message (reagent/atom nil)
          text-input    (atom nil)]
      (fn []
        [react/view {:flex 1}
         [react/view {:flex 1}]
         [react/view styles/centered
          (when @error-message
            [tooltip/tooltip @error-message {:color        colors/white
                                             :font-size    12
                                             :bottom-value 15}])
          [react/text-input
           {:on-change-text (partial update-recipient chain transaction error-message)
            :auto-focus true
            :auto-capitalize :none
            :auto-correct false
            :placeholder "0x... or name.eth"
            :placeholder-text-color "rgb(143,162,234)"
            :multiline true
            :max-length 84
            :ref #(reset! text-input %)
            ;;NOTE(goranjovic)- :default-value instead of :value to prevent the flickering due to two way binding
            :default-value (or (:to-ens @transaction) (:to @transaction))
            :selection-color colors/green
            :accessibility-label :recipient-address-input
            :style styles/choose-recipient-text-input}]]
         [react/view {:flex 1}]
         [react/view {:flex-direction :row :padding 3}
          [address-button {:underlay-color colors/white-transparent
                           :background-color colors/black-transparent
                           :on-press #(react/get-from-clipboard
                                       (fn [addr]
                                         (when (and addr (not (string/blank? addr)))
                                           (swap! transaction assoc :to (string/trim addr)))))}
           [react/view {:flex-direction :row
                        :padding-horizontal 18}
            [vector-icons/icon :icons/paste {:color colors/white-transparent}]
            [react/view {:flex 1 :flex-direction :row :justify-content :center}
             [react/text {:style {:color colors/white
                                  :font-size 15
                                  :line-height 22}}
              (i18n/label :t/paste)]]]]
          [address-button {:underlay-color colors/white-transparent
                           :background-color colors/black-transparent
                           :on-press
                           (fn []
                             (re-frame/dispatch
                              [:request-permissions {:permissions [:camera]
                                                     :on-allowed (partial open-qr-scanner chain text-input transaction)
                                                     :on-denied
                                                     #(utils/set-timeout
                                                       (fn []
                                                         (utils/show-popup (i18n/label :t/error)
                                                                           (i18n/label :t/camera-access-error)))
                                                       50)}]))}
           [react/view {:flex-direction :row
                        :padding-horizontal 18}
            [vector-icons/icon :icons/qr {:color colors/white-transparent}]
            [react/view {:flex 1 :flex-direction :row :justify-content :center}
             [react/text {:style {:color colors/white
                                  :font-size 15
                                  :line-height 22}}
              (i18n/label :t/scan)]]]]
          (let [disabled? (string/blank? (:to @transaction))]
            [address-button {:disabled? disabled?
                             :underlay-color colors/black-transparent
                             :background-color (if disabled? colors/blue colors/white)
                             :on-press
                             #(events/chosen-recipient chain (:to @transaction) on-address
                                                       (fn on-error [_]
                                                         (reset! error-message (i18n/label :t/invalid-address))))}
             [react/text {:style {:color (if disabled? colors/white colors/blue)
                                  :font-size 15
                                  :line-height 22}}
              (i18n/label :t/next)]])]]))))

;;  #(re-frame/dispatch [:wallet/fill-request-from-contact contact])

(defn info-page [message]
  [react/view {:style {:flex 1
                       :align-items :center
                       :justify-content :center
                       :background-color colors/blue}}
   [vector-icons/icon :icons/info {:color colors/white}]
   [react/text {:style {:max-width 144
                        :margin-top 15
                        :color colors/white
                        :font-size 15
                        :text-align :center
                        :line-height 22}}
    message]])

(defn render-contact [on-contact contact]
  {:pre [(fn? on-contact) (map? contact) (:address contact)]}
  [react/touchable-highlight {:underlay-color colors/white-transparent
                              :on-press #(on-contact contact)}
   [react/view {:flex 1
                :flex-direction :row
                :padding-right 23
                :padding-left 16
                :padding-top 12}
    [react/view {:margin-top 3}
     [photos/photo (:photo-path contact) {:size list.styles/image-size}]]
    [react/view {:margin-left 16
                 :flex 1}
     [react/view {:accessibility-label :contact-name-text
                  :margin-bottom 2}
      [react/text {:style {:font-size 15
                           :font-weight "500"
                           :line-height 22
                           :color colors/white}}
       (:name contact)]]
     [react/text {:style {:font-size 15
                          :line-height 22
                          :color "rgba(255,255,255,0.4)"}
                  :accessibility-label :contact-address-text}
      (ethereum/normalized-address (:address contact))]]]])

(defn choose-contact-view [{:keys [contacts on-contact]}]
  {:pre [(every? map? contacts) (fn? on-contact)]}
  (if (empty? contacts)
    (info-page (i18n/label :t/wallet-no-contacts))
    [react/view {:flex 1}
     [list/flat-list {:data      contacts
                      :key-fn    :address
                      :render-fn (partial
                                  render-contact
                                  on-contact)}]]))

;; TODO clean up all dependencies here, leaving these in place until all behavior is verified on
;; all platforms
(defn- choose-address-contact [{:keys [modal? contacts scroll advanced? transaction network all-tokens amount-input network-status] :as opts}]

  (let [transaction                  (reagent/atom transaction)
        chain                        (ethereum/network->chain-keyword network)
        native-currency              (tokens/native-currency chain)
        {:keys [decimals] :as token} (tokens/asset-for all-tokens chain symbol)
        online? (= :online network-status)]
    [wallet.components/simple-screen {:avoid-keyboard? (not modal?)
                                      :status-bar-type (if modal? :modal-wallet :wallet)}
     [toolbar modal? (i18n/label :t/send-to)]
     [simple-tab-navigator {:address  {:name "Address"
                                       :component (choose-address-view
                                                   {:chain chain
                                                    :transaction transaction
                                                    :on-address
                                                    #(re-frame/dispatch [:navigate-to :wallet-choose-amount
                                                                         {:transaction (swap! transaction assoc :to %)
                                                                          :native-currency native-currency
                                                                          :modal? modal?}])})}
                            :contacts {:name "Contacts"
                                       :component (partial
                                                   choose-contact-view
                                                   {:contacts contacts
                                                    :on-contact
                                                    (fn [{:keys [address]}]
                                                      (re-frame/dispatch [:navigate-to :wallet-choose-amount
                                                                          {:modal? modal?
                                                                           :native-currency native-currency
                                                                           :transaction (swap! transaction assoc :to address)}]))})}}
      :address]]))

;; ----------------------------------------------------------------------
;; Step 2 choosing an amount and token to send
;; ----------------------------------------------------------------------

(declare network-fees)

;; worthy abstraction
(defn- anim-ref-send
  "Call one of the methods in an animation ref.

Takes an animation ref (a map of keys to animation tiggering methods)

A keyword that should equal one of the keys in the map

and optional args to be sent to the animation.

Example:
(anim-ref-send slider-ref :open!)
(anim-ref-send slider-ref :move-top-left! 25 25)"
  [anim-ref signal & args]
  (when anim-ref
    (assert (get anim-ref signal)
            (str "Key " signal
                 " was not found in animation ref. Should be in "
                 (pr-str (keys anim-ref)))))
  (some-> anim-ref (get signal) (apply args)))

(defn- slide-up-modal
  "Creates a modal that slides up from the bottom of the screen and
  responds to a swipe down gesture to dismiss 
   
  The modal initially renders in the closed position.

  It takes an options map and the react child to be displayed in the
  modal. 

  Options:
    :anim-ref - takes a function that will be called with a map of
        animation methods.
    :swipe-dismiss? - a boolean that determines wether the modal screen
        should be dismissed on swipe down gesture

  This slide-up-modal will callback the `anim-ref` fn and provides a
  map with 2 animation methods:

  :open!  - opens and displays the modal
  :close! - closes the modal"
  [{:keys [anim-ref swipe-dismiss?]} children]
  {:pre [(fn? anim-ref)]}
  ;; Add swipe down to dissmiss
  (let [window-height (:height (react/get-dimensions "window") 1000)

        bottom-position  (animation/create-value (- window-height))

        modal-screen-bg-color
        (animation/interpolate bottom-position
                               {:inputRange [(- window-height) 0]
                                :outputRange ["rgba(0,0,0,0)"
                                              "rgba(0,0,0,0.7)"]})
        modal-screen-top
        (animation/interpolate bottom-position
                               {:inputRange [(- window-height)
                                             (+ (- window-height) 1)
                                             0]
                                :outputRange [window-height -200 -200]})

        vertical-slide-to (fn [view-bottom]
                            (animation/start
                             (animation/timing bottom-position {:toValue  view-bottom
                                                                :duration 500})))
        open-panel! #(vertical-slide-to 0)
        close-panel! #(vertical-slide-to (- window-height))
        ;; swipe-down-panhandler
        swipe-down-handlers
        (when swipe-dismiss?
          (js->clj
           (.-panHandlers
            (.create react/pan-responder
                     #js {:onMoveShouldSetPanResponder
                          (fn [e g]
                            (when-let [distance (.-dy g)]
                              (< 50 distance)))
                          :onMoveShouldSetPanResponderCapture
                          (fn [e g]
                            (when-let [distance (.-dy g)]
                              (< 50 distance)))
                          :onPanResponderRelease  (fn [e g]
                                                    (when-let [distance (.-dy g)]
                                                      (when (< 200 distance)
                                                        (close-panel!))))}))))]
    (anim-ref {:open! open-panel!
               :close! close-panel!})
    (fn [{:keys [anim-ref] :as opts} children]
      [react/animated-view (merge
                            {:style
                             {:position :absolute
                              :top      modal-screen-top
                              :bottom   0
                              :left     0
                              :right    0
                              :z-index  1
                              :background-color modal-screen-bg-color}}
                            swipe-down-handlers)
       [react/touchable-highlight {:on-press (fn [] (close-panel!))
                                   :style {:flex 1}}
        [react/view]]
       [react/animated-view {:style
                             {:position :absolute
                              :left     0
                              :right    0
                              :z-index  2
                              :bottom   bottom-position}}
        children]])))

(defn- custom-gas-panel-action [{:keys [label active on-press icon background-color]} child]
  {:pre [label (boolean? active) on-press icon background-color]}
  [react/view {:style {:flex-direction :row
                       :padding-horizontal 22
                       :padding-vertical 11
                       :align-items :center}}
   [react/touchable-highlight
    {:disabled active
     :on-press on-press}
    [react/animated-view {:style {:border-radius 21
                                  :width 40
                                  :height 40
                                  :justify-content :center
                                  :align-items :center
                                  :background-color background-color}}
     [vector-icons/icon icon {:color (if active colors/white colors/gray)}]]]
   [react/touchable-highlight
    {:disabled active
     :on-press on-press
     :style {:flex 1}}
    [react/text {:style {:color colors/black
                         :font-size 17
                         :padding-left 17
                         :line-height 40}}
     label]]
   child])

(defn- custom-gas-edit
  [{:keys [on-gas-input-change
           on-gas-price-input-change
           gas-input
           gas-price-input]}]
  (let [gas-error (reagent/atom nil)
        gas-price-error (reagent/atom nil)]
    (fn [{:keys [on-gas-input-change
                 on-gas-price-input-change
                 gas-input
                 gas-price-input]}]
      [react/view {:style {:padding-horizontal 22
                           :padding-vertical 11}}
       [react/text "Gas price"]
       (when @gas-price-error
         [react/view {:style {:z-index 100}}
          [tooltip/tooltip @gas-price-error
           {:color        colors/blue-light
            :font-size    12
            :bottom-value -3}]])
       [react/view {:style {:border-radius 8
                            :background-color colors/gray-light
                            :padding-vertical 16
                            :padding-horizontal 16
                            :flex-direction :row
                            :align-items :flex-end
                            :margin-vertical 7}}
        [react/text-input {:keyboard-type :numeric
                           :placeholder "0"
                           :on-change-text (fn [x]
                                             (if-not (money/bignumber x)
                                               (reset! gas-price-error "Invalid number format")
                                               (reset! gas-price-error nil))
                                             (on-gas-price-input-change x))
                           :value gas-price-input
                           :style {:font-size 15
                                   :flex 1}}]
        [react/text "Gwei"]]
       [react/text {:style {:color colors/gray
                            :font-size 12}}
        "Gas price is the amount you are willing to pay per unit of gas. Increasing this price may help your transaction get processed faster."]
       [react/text {:style {:margin-top 22}} "Gas limit"]
       (when @gas-error
         [react/view {:style {:z-index 100}}
          [tooltip/tooltip @gas-error
           {:color        colors/blue-light
            :font-size    12
            :bottom-value -3}]])
       [react/view {:style {:border-radius 8
                            :background-color colors/gray-light
                            :padding-vertical 16
                            :padding-horizontal 16
                            :flex-direction :row
                            :align-items :flex-end
                            :margin-vertical 7}}
        [react/text-input {:keyboard-type :numeric
                           :placeholder "0"
                           :on-change-text
                           (fn [x]
                             (if-not (money/bignumber x)
                               (reset! gas-error "Invalid number format")
                               (reset! gas-error nil))
                             (on-gas-input-change x))
                           :value gas-input
                           :style {:font-size 15
                                   :flex 1}}]]
       [react/text {:style {:color colors/gray
                            :font-size 12}}
        "Gas limit is the maximum units of gas you're willing to spend on this transaction."]])))

;; TODOs

;; - add stronger validation and a tool tip for bad input
;; - ensure that gas passed back to teh parent view is correct
;; - wire in gas and gas-price to the parent view

(defn custom-gas-derived-state [{:keys [gas-input gas-price-input custom-open?]}
                                {:keys [custom-gas custom-gas-price
                                        optimal-gas optimal-gas-price
                                        gas-gas-price->fiat] :as opts}]
  (let [custom-input-gas
        (or (when (not (string/blank? gas-input))
              (money/bignumber gas-input))
            custom-gas
            optimal-gas)
        custom-input-gas-price
        (or (when (not (string/blank? gas-price-input))
              (money/->wei :gwei gas-price-input))
            custom-gas-price
            optimal-gas-price)]
    {:optimal-fiat-price
     (str "~ $" (gas-gas-price->fiat optimal-gas optimal-gas-price))
     :custom-fiat-price
     (if custom-open?
       (str "~ $" (gas-gas-price->fiat custom-input-gas custom-input-gas-price))
       (str "..."))
     :gas-price-input-value
     (str (or gas-price-input
              (some->> custom-gas-price (money/wei-> :gwei))
              (some->> optimal-gas-price (money/wei-> :gwei))))
     :gas-input-value
     (str (or gas-input custom-gas optimal-gas))
     :gas-map-for-submit
     (if custom-open?
       {:gas optimal-gas :gas-price optimal-gas-price}
       {:gas custom-input-gas :gas-price custom-input-gas-price})}))

;; Choosing the gas amount
(defn custom-gas-input-panel [{:keys [custom-gas custom-gas-price
                                      optimal-gas optimal-gas-price
                                      gas-gas-price->fiat on-submit] :as opts}]
  {:pre [optimal-gas optimal-gas-price gas-gas-price->fiat on-submit]}
  (let [custom-open? (and custom-gas custom-gas-price)
        state-atom   (reagent.core/atom {:custom-open? (boolean custom-open?)
                                         :gas-input nil
                                         :gas-price-input nil})

        ;; slider animations
        slider-height (animation/create-value (if custom-open? 290 0))
        slider-height-to #(animation/start
                           (animation/timing slider-height {:toValue  %
                                                            :duration 500}))

        optimal-button-bg-color
        (animation/interpolate slider-height
                               {:inputRange [0 200 290]
                                :outputRange [colors/blue colors/gray-light colors/gray-light]})

        custom-button-bg-color
        (animation/interpolate slider-height
                               {:inputRange [0 200 290]
                                :outputRange [colors/gray-light colors/blue colors/blue]})

        open-slider!  #(do
                         (slider-height-to 290)
                         (swap! state-atom assoc :custom-open? true))
        close-slider! #(do
                         (slider-height-to 0)
                         (swap! state-atom assoc :custom-open? false))]
    (fn [opts]
      (let [{:keys [optimal-fiat-price
                    custom-fiat-price
                    gas-price-input-value
                    gas-input-value
                    gas-map-for-submit]}
            (custom-gas-derived-state @state-atom opts)]
        [react/view {:style {:background-color colors/white
                             :border-top-left-radius 8
                             :border-top-right-radius 8}}
         [react/view {:style {:justify-content :center
                              :padding-top 22
                              :padding-bottom 7}}
          [react/text
           {:style {:color colors/black
                    :font-size 22
                    :line-height 28
                    :font-weight :bold
                    :text-align :center}}
           "Network fee settings"]
          [react/text
           {:style {:color colors/gray
                    :font-size 15
                    :line-height 22
                    :text-align :center
                    :padding-horizontal 45
                    :padding-vertical 8}}
           "This fee, known as gas is paid directly to the Ethereum network. Status does not collect any of these funds"]]
         [react/view {:style {:border-top-width 1
                              :border-top-color colors/black-transparent
                              :padding-top 11
                              :padding-bottom 7}}
          (custom-gas-panel-action
           {:icon :icons/time
            :label "Optimal"
            :on-press close-slider!
            :background-color optimal-button-bg-color
            :active  (not (:custom-open? @state-atom))}
           [react/text {:style {:color colors/gray
                                :font-size 17
                                :padding-left 17
                                :line-height 20}}
            optimal-fiat-price])
          (custom-gas-panel-action
           {:icon :icons/sliders
            :label "Custom"
            :on-press open-slider!
            :background-color custom-button-bg-color
            :active (:custom-open? @state-atom)}
           [react/text {:style {:color colors/gray
                                :font-size 17
                                :padding-left 17
                                :line-height 20
                                :text-align :center
                                :min-width 60}}
            custom-fiat-price])
          [react/animated-view {:style {:background-color colors/white
                                        :height slider-height
                                        :overflow :hidden}}
           [custom-gas-edit
            {:on-gas-price-input-change
             #(when (money/bignumber %)
                (swap! state-atom assoc :gas-price-input %))
             :on-gas-input-change
             #(when (money/bignumber %)
                (swap! state-atom assoc :gas-input %))
             :gas-price-input gas-price-input-value
             :gas-input gas-input-value}]]
          [react/view {:style {:flex-direction :row
                               :justify-content :center
                               :padding-vertical 16}}
           [react/touchable-highlight
            {:on-press #(on-submit gas-map-for-submit)
             :style {:padding-horizontal 39
                     :padding-vertical 12
                     :border-radius 8
                     :background-color colors/blue-light}}
            [react/text {:style {:font-size 15
                                 :line-height 22
                                 :color colors/blue}}
             "Update"]]]]]))))

;; Choosing the asset

(defn white-toolbar [modal? title]
  (let [action (if modal? actions/close actions/back)]
    [toolbar/toolbar {:style {:background-color colors/white
                              :border-bottom-width 1
                              :border-bottom-color colors/black-transparent}}
     [toolbar/nav-button (action (if modal?
                                   #(re-frame/dispatch [:wallet/discard-transaction-navigate-back])
                                   #(actions/default-handler)))]
     [toolbar/content-title {:color colors/black :font-size 17 :font-weight :bold} title]]))

(defn- render-token-item [{:keys [symbol name icon decimals amount] :as token}]
  [list/item
   [list/item-image icon]
   [list/item-content
    [react/text {:style {:margin-right 10, :color colors/black}} name]
    [list/item-secondary (str (wallet.utils/format-amount amount decimals)
                              " "
                              (wallet.utils/display-symbol token))]]])

;; TODO parameterize this with on-asset handler
(defview choose-asset []
  (letsubs [assets [:wallet/transferrable-assets-with-amount]
            {:keys [on-asset]} [:get-screen-params :wallet-choose-asset]]
    [react/keyboard-avoiding-view {:flex 1 :background-color colors/white}
     [status-bar/status-bar {:type :modal-white}]
     [white-toolbar false "Choose asset" #_(i18n/label :t/wallet-assets)]
     [react/view {:style (assoc components.styles/flex :background-color :white)}
      [list/flat-list {:default-separator? false ;true
                       :data               assets
                       :key-fn             (comp str :symbol)
                       :render-fn          #(do
                                              [react/touchable-highlight {:on-press
                                                                          (fn []
                                                                            (on-asset %))
                                                                          :underlay-color colors/black-transparent}
                                               (render-token-item %)])}]]]))

(defn show-current-asset [{:keys [name icon decimals amount] :as token}]
  [react/view {:style {:flex-direction :row,
                       :justify-content :center
                       :padding-horizontal 21
                       :padding-vertical 12}}
   [list/item-image icon]
   [react/view {:margin-horizontal 9
                :flex 1}
    [list/item-content
     [react/text {:style {:margin-right 10,
                          :font-weight "500"
                          :font-size 15
                          :color colors/white}} name]
     [react/text {:style {:font-size 14
                          :padding-top 4
                          :color "rgba(255,255,255,0.4)"}
                  :ellipsize-mode :middle
                  :number-of-lines 1}
      (str (wallet.utils/format-amount amount decimals)
           " "
           (wallet.utils/display-symbol token))]]]
   list/item-icon-forward])

;; TODOs
;; consistent input validation throughout looking at wallet.db/parse-amount
;; handle incoming error text :amount-error ??
;; consider :amount-text
;; use incoming gas-price
;; look at how callers are invoking send-transaction status-im.chat.commands.impl.transactions
;; look at what happens to gas-price on token change? Nothing I suspect
;; look at initial network fees

(defn fetch-token [all-tokens network token-symbol]
  {:pre [(map? all-tokens) (map? network)]}
  (when (keyword? token-symbol)
    (tokens/asset-for all-tokens
                      (ethereum/network->chain-keyword network)
                      token-symbol)))

(defn create-initial-state [{:keys [symbol decimals] :as token} amount]
  {:input-amount (when amount
                   (when-let [amount' (money/internal->formatted amount symbol decimals)]
                     (str amount')))
   :inverted false
   :edit-gas false
   :error-message nil})

(defn toggle-edit-gas [state]
  (swap! state update :edit-gas not))

(defn input-currency-symbol [{:keys [inverted] :as state} {:keys [symbol] :as token} {:keys [code] :as fiat-currency}]
  {:pre [(boolean? inverted) (keyword? symbol) (string? code)]}
  (if-not (:inverted state) (name (:symbol token)) code))

(defn converted-currency-symbol [{:keys [inverted] :as state} {:keys [symbol] :as token} {:keys [code] :as fiat-currency}]
  {:pre [(boolean? inverted) (keyword? symbol) (string? code)]}
  (if (:inverted state) (name (:symbol token)) code))

(defn token->fiat-conversion [prices token fiat-currency value]
  {:pre [(map? prices) (map? token) (map? fiat-currency) value]}
  (when-let [price (get-in prices [(:symbol token)
                                   (-> fiat-currency :code keyword)
                                   :price])]
    (some-> value
            money/bignumber
            (money/crypto->fiat price))))

(defn fiat->token-conversion [prices token fiat-currency value]
  {:pre [(map? prices) (map? token) (map? fiat-currency) value]}
  (when-let [price (get-in prices [(:symbol token)
                                   (-> fiat-currency :code keyword)
                                   :price])]
    (some-> value
            money/bignumber
            (.div (money/bignumber price)))))

(defn valid-input-amount? [input-amount]
  (and (not (string/blank? input-amount))
       ;; we are ignoring precision for this case
       (not (:error (wallet.db/parse-amount input-amount 100)))))

(defn converted-currency-amount [{:keys [input-amount inverted]} token fiat-currency prices]
  (when (valid-input-amount? input-amount)
    (if-not inverted
      (some-> (token->fiat-conversion prices token fiat-currency input-amount)
              (money/with-precision 2))
      (some-> (fiat->token-conversion prices token fiat-currency input-amount)
              (money/with-precision 8)))))

(defn converted-currency-phrase [state token fiat-currency prices]
  (str (if-let [amount-bn (converted-currency-amount state token fiat-currency prices)]
         (str amount-bn)
         "0")
       " " (converted-currency-symbol state token fiat-currency)))

(defn current-token-input-amount [{:keys [input-amount inverted] :as state} token fiat-currency prices]
  {:pre [(map? state) (map? token) (map? fiat-currency) (map? prices)]}
  (when input-amount
    (when-let [amount-bn (if inverted
                           (fiat->token-conversion prices token fiat-currency input-amount)
                           (money/bignumber input-amount))]
      amount-bn
      (money/formatted->internal amount-bn (:symbol token) (:decimals token)))))

(defn update-input-errors [{:keys [input-amount inverted] :as state} token fiat-currency prices]
  {:pre [(map? state) (map? token) (map? fiat-currency) (map? prices)]}
  (let [{:keys [value error]}
        (wallet.db/parse-amount input-amount
                                (if inverted 2 (:decimals token)))]
    (if-let [error-msg
             (cond
               error error
               (not (money/sufficient-funds? (current-token-input-amount state token fiat-currency prices)
                                             (:amount token)))
               "Insufficient funds"
               :else nil)]
      (assoc state :error-message error-msg)
      state)))

(defn update-input-amount [state input-str token fiat-currency prices]
  {:pre [(map? state) (map? token) (map? fiat-currency) (map? prices)]}
  (cond-> (-> state
              (assoc :input-amount input-str)
              (dissoc :error-message))
    (not (string/blank? input-str))
    (update-input-errors token fiat-currency prices)))

(defn max-fee [gas gas-price]
  {:pre [gas gas-price]}
  (money/wei->ether (.times gas gas-price)))

(defn network-fees [prices token fiat-currency gas-ether-price]
  (some-> (token->fiat-conversion prices token fiat-currency gas-ether-price)
          (money/with-precision 2)))

;; TODO derived state

;; !!! only send gas and gas-price in a transaction if they are custom gas prices!!!
(defn choose-amount-token-helper [{:keys [balance network prices fiat-currency
                                          native-currency
                                          all-tokens
                                          modal?
                                          transaction]}]
  {:pre [(map? native-currency)]}
  (let [tx-atom (reagent/atom transaction)
        token (or (fetch-token all-tokens network (:symbol @tx-atom))
                  native-currency)
        optimal-gas-price-atom (reagent/atom nil)
        state-atom (reagent/atom (create-initial-state token (:amount @tx-atom)))
        network-fees-modal-ref (atom nil)
        open-network-fees! #(anim-ref-send @network-fees-modal-ref :open!)
        close-network-fees! #(anim-ref-send @network-fees-modal-ref :close!)]
    ;; initialize the starting gas price
    (ethereum/gas-price (:web3 @re-frame.db/app-db)
                        (fn [_ gas-price]
                          (when gas-price
                            (reset! optimal-gas-price-atom gas-price))))
    (fn [{:keys [balance network prices fiat-currency
                 native-currency all-tokens modal? transaction]}]
      (let [{:keys [symbol to] :or {symbol (:symbol native-currency)} :as transaction} @tx-atom
            token (-> (tokens/asset-for all-tokens (ethereum/network->chain-keyword network) symbol)
                      (assoc :amount (get balance symbol (money/bignumber 0))))
            optimal-gas (ethereum/estimate-gas symbol)
            gas-gas-price->fiat
            (fn [gas' gas-price']
              (network-fees prices token fiat-currency (max-fee gas' gas-price')))]
        [wallet.components/simple-screen {:avoid-keyboard? (not modal?)
                                          :status-bar-type (if modal? :modal-wallet :wallet)}
         [toolbar modal? "Send amount"]
         (if (empty? balance)
           (info-page "You don't have any assets yet")
           (let [{:keys [error-message input-amount] :as state} @state-atom
                 input-symbol (input-currency-symbol state token fiat-currency)
                 converted-phrase (converted-currency-phrase state token fiat-currency prices)]
             [react/view {:flex 1}
              ;; network fees modal
              (when @optimal-gas-price-atom
                [slide-up-modal {:anim-ref #(reset! network-fees-modal-ref %)
                                 :swipe-dismiss? true}
                 [custom-gas-input-panel
                  {:on-submit (fn [{:keys [gas gas-price]}]
                                #_(assert (and gas gas-price))
                                (swap! tx-atom assoc :gas gas :gas-price gas-price)
                                (close-network-fees!))
                   :custom-gas          (:gas @tx-atom)
                   :custom-gas-price    (:gas-price @tx-atom)
                   :optimal-gas         optimal-gas
                   :optimal-gas-price   @optimal-gas-price-atom
                   :gas-gas-price->fiat gas-gas-price->fiat}]])
              [react/touchable-highlight {:style {:background-color colors/black-transparent}
                                          :on-press #(re-frame/dispatch
                                                      [:navigate-to
                                                       :wallet-choose-asset
                                                       {:on-asset (fn [{:keys [symbol]}]
                                                                    (when symbol
                                                                      (swap! tx-atom assoc :symbol symbol))
                                                                    (re-frame/dispatch [:navigate-back]))}])
                                          :underlay-color colors/white-transparent}
               [show-current-asset token]]
              [react/view {:flex 1}
               [react/view {:flex 1}]
               [react/view {:justify-content :center
                            :align-items :flex-end
                            :flex-direction :row}
                (when error-message
                  [tooltip/tooltip error-message {:color        colors/white
                                                  :font-size    12
                                                  :bottom-value 15}])
                [react/text-input
                 {:on-change-text #(swap! state-atom update-input-amount % token fiat-currency prices)
                  :keyboard-type :numeric
                  :accessibility-label :amount-input
                  :auto-focus true
                  :auto-capitalize :none
                  :auto-correct false
                  :placeholder "0"
                  :placeholder-text-color "rgb(143,162,234)"
                  :multiline true
                  :max-length 42
                  :value input-amount
                  :selection-color colors/green
                  :style {:color colors/white
                          :font-size 30
                          :font-weight :bold
                          :padding-horizontal 10
                          :max-width 290}}]
                [react/text {:style {:color (if (not (string/blank? input-amount))
                                              colors/white
                                              "rgb(143,162,234)")
                                     :font-size 30
                                     :font-weight :bold}}
                 input-symbol]]
               [react/view {}
                [react/text {:style {:text-align :center
                                     :margin-top 16
                                     :font-size 15
                                     :line-height 22
                                     :color "rgb(143,162,234)"}}
                 converted-phrase]]
               [react/view {:justify-content :center :flex-direction :row}
                [react/touchable-highlight {:on-press open-network-fees!
                                            :style {:background-color colors/black-transparent
                                                    :padding-horizontal 13
                                                    :padding-vertical 7
                                                    :margin-top 1
                                                    :border-radius 8
                                                    :opacity (if (valid-input-amount? input-amount) 1 0)}}
                 [react/text {:style {:color colors/white
                                      :font-size 15
                                      :line-height 22}}

                  (str "network fee ~ "
                       (when @optimal-gas-price-atom
                         (gas-gas-price->fiat
                          (:gas @tx-atom optimal-gas)
                          (:gas-price @tx-atom @optimal-gas-price-atom)))
                       " "
                       (:code fiat-currency))]]]
               [react/view {:flex 1}]

               [react/view {:flex-direction :row :padding 3}
                [address-button {:underlay-color colors/white-transparent
                                 :background-color colors/black-transparent
                                 :on-press #(swap! state-atom update :inverted not)}
                 [react/view {:flex-direction :row}
                  [react/text {:style {:color colors/white
                                       :font-size 15
                                       :line-height 22
                                       :padding-right 10}}
                   (:code fiat-currency)]
                  [vector-icons/icon :icons/change {:color colors/white-transparent}]
                  [react/text {:style {:color colors/white
                                       :font-size 15
                                       :line-height 22
                                       :padding-left 11}}
                   (name symbol)]]]
                (let [disabled? (string/blank? input-amount)]
                  [address-button {:disabled? disabled?
                                   :underlay-color colors/black-transparent
                                   :background-color (if disabled? colors/blue colors/white)
                                   :on-press
                                   (fn []
                                     ;; TODO handle moving onto overview from here

                                     #_(prn (money/bignumber amount) (money/wei->ether (:amount token)))
                                     #_(if-let [new-amount (money/bignumber amount)]
                                         ;; look at sufficient funds and sufficient gas for this 
                                         (if (.greaterThanOrEqualTo new-amount (money/wei->ether (:amount token)))
                                           (do 'good) ;; Move onto overview after adding amount to tx
                                           (reset! error-message "Insufficient funds"))
                                         (reset! error-message "Invalid amount")))}
                   [react/text {:style {:color (if disabled? colors/white colors/blue)
                                        :font-size 15
                                        :line-height 22}}
                    (i18n/label :t/next)]])]]]))]))))

(defview choose-amount-token []
  (letsubs [{:keys [transaction modal? native-currency]} [:get-screen-params :wallet-choose-amount]
            balance [:balance]
            prices  [:prices]
            network [:account/network]
            all-tokens [:wallet/all-tokens]
            fiat-currency [:wallet/currency]]
    [choose-amount-token-helper {:balance balance
                                 :network network
                                 :all-tokens all-tokens
                                 :modal? modal?
                                 :prices prices
                                 :native-currency native-currency
                                 :fiat-currency fiat-currency
                                 :transaction transaction}]))

;; ----------------------------------------------------------------------
;; Step 3 Final Overview
;; ----------------------------------------------------------------------

#_(defview final-tx-overview [])

;; MAIN SEND TRANSACTION VIEW
(defn- send-transaction-view [{:keys [scroll] :as opts}]
  (let [amount-input (atom nil)
        handler      (fn [_]
                       (when (and scroll @scroll @amount-input
                                  (.isFocused @amount-input))
                         (log/debug "Amount field focused, scrolling down")
                         (.scrollToEnd @scroll)))]
    (reagent/create-class
     {:component-will-mount (fn [_]
                              ;;NOTE(goranjovic): keyboardDidShow is for android and keyboardWillShow for ios
                              (.addListener react/keyboard "keyboardDidShow" handler)
                              (.addListener react/keyboard "keyboardWillShow" handler))
      :reagent-render       (fn [opts] [choose-address-contact (assoc opts :amount-input amount-input)])})))

;; SEND TRANSACTION FROM WALLET (CHAT)
(defview send-transaction []
  (letsubs [transaction    [:wallet.send/transaction]
            advanced?      [:wallet.send/advanced?]
            network        [:account/network]
            scroll         (atom nil)
            network-status [:network-status]
            all-tokens     [:wallet/all-tokens]
            contacts       [:contacts/all-added-people-contacts]]
    [send-transaction-view {:modal?         false
                            ;; TODO only send gas and gas-price when they are custom
                            :transaction    (dissoc transaction :gas :gas-price)
                            :scroll         scroll
                            :advanced?      advanced?
                            :network        network
                            :all-tokens     all-tokens
                            :contacts       contacts
                            :network-status network-status}]))

;; SEND TRANSACTION FROM DAPP
(defview send-transaction-modal []
  (letsubs [transaction    [:wallet.send/transaction]
            advanced?      [:wallet.send/advanced?]
            network        [:account/network]
            scroll         (atom nil)
            network-status [:network-status]
            all-tokens     [:wallet/all-tokens]]
    (if transaction
      [send-transaction-view {:modal?         true
                              :transaction    transaction
                              :scroll         scroll
                              :advanced?      advanced?
                              :network        network
                              :all-tokens     all-tokens
                              :network-status network-status}]
      [react/view wallet.styles/wallet-modal-container
       [react/view components.styles/flex
        [status-bar/status-bar {:type :modal-wallet}]
        [toolbar true (i18n/label :t/send-transaction)]
        [react/i18n-text {:style styles/empty-text
                          :key   :unsigned-transaction-expired}]]])))

;; SIGN MESSAGE FROM DAPP
(defview sign-message-modal []
  (letsubs [{:keys [data in-progress?]} [:wallet.send/transaction]
            network-status [:network-status]]
    [wallet.components/simple-screen {:status-bar-type :modal-wallet}
     [toolbar true (i18n/label :t/sign-message)]
     [react/view components.styles/flex
      [react/scroll-view
       (when (= network-status :offline)
         [wallet.main.views/snackbar :t/error-cant-sign-message-offline])
       [react/view styles/send-transaction-form
        [wallet.components/cartouche {:disabled? true}
         (i18n/label :t/message)
         [components/amount-input
          {:disabled?     true
           :input-options {:multiline true
                           :height    100}
           :amount-text   data}
          nil]]]]
      [enter-password-buttons false
       #(re-frame/dispatch [:wallet/discard-transaction-navigate-back])
       #(re-frame/dispatch [:wallet/sign-message])
       :t/transactions-sign]
      [password-input-panel :t/signing-message-phrase-description false]
      (when in-progress?
        [react/view styles/processing-view])]]))
