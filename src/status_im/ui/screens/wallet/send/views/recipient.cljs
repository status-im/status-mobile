(ns status-im.ui.screens.wallet.send.views.recipient
  (:require [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [reagent.core :as reagent]
            [status-im.ui.screens.wallet.components.views :as wallet.components]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.wallet.send.views.common :as common]
            [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.styles :as list.styles]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.screens.wallet.send.events :as events]
            [status-im.utils.utils :as utils]
            [clojure.string :as string]
            [status-im.ui.screens.wallet.send.styles :as styles]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.utils.ethereum.ens :as ens]
            [status-im.ui.components.list.views :as list]
            [taoensso.timbre :as log]))

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
                                  :border-bottom-color (if current? colors/white colors/transparent)}
                      [react/text {:style {:color     (if current? colors/white colors/white-transparent)
                                           :font-size 15}} name]]]]))
               tab-map)]
         (when-let [component-thunk (some-> tab-map tab-name :component)]
           [component-thunk])]))))

(defn open-qr-scanner [chain all-tokens contacts text-input transaction]
  (.blur @text-input)
  (re-frame/dispatch [:navigate-to :recipient-qr-code
                      {:on-recipient
                       (fn [qr-data]
                         (if-let [parsed-qr-data (events/extract-qr-code-details chain all-tokens qr-data)]
                           (let [{:keys [chain-id]} parsed-qr-data
                                 tx-data            (events/qr-data->transaction-data parsed-qr-data contacts)]
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
                             :on-cancel           (partial open-qr-scanner chain all-tokens contacts text-input transaction)})))}]))

(defn update-recipient [chain transaction error-message value]
  (if (ens/is-valid-eth-name? value)
    (do (ens/get-addr (get ens/ens-registries chain)
                      value
                      #(if (ethereum/address? %)
                         (swap! transaction assoc :to-ens value :to %)
                         (reset! error-message (i18n/label :t/error-unknown-ens-name)))))
    (do (swap! transaction assoc :to value)
        (reset! error-message nil))))

(defn choose-address-view
  "A view that allows you to choose an address"
  [{:keys [web3 chain all-tokens contacts transaction on-address]}]
  {:pre [(keyword? chain) (fn? on-address)]}
  (fn []
    (let [error-message (reagent/atom nil)
          text-input    (atom nil)]
      (fn []
        [react/view {:flex 1}
         [react/view {:flex 1}]
         [react/view {:justify-content :center
                      :align-items     :center}
          (when @error-message
            [tooltip/tooltip @error-message {:color        colors/white
                                             :font-size    12
                                             :bottom-value 15}])
          [react/text-input
           {:on-change-text         (partial update-recipient chain transaction error-message)
            :auto-focus             true
            :auto-capitalize        :none
            :auto-correct           false
            :placeholder            (i18n/label :t/address-or-ens-placeholder)
            :placeholder-text-color colors/blue-shadow
            :multiline              true
            :max-length             84
            :ref                    #(reset! text-input %)
            :default-value          (or (:to-ens @transaction) (:to @transaction))
            :selection-color        colors/green
            :accessibility-label    :recipient-address-input
            :keyboard-appearance    :dark
            :style                  styles/choose-recipient-text-input}]]
         [react/view {:flex 1}]
         [react/view {:flex-direction :row
                      :padding        3}
          [common/action-button {:underlay-color   colors/white-transparent
                                 :background-color colors/black-transparent
                                 :on-press         #(react/get-from-clipboard
                                                     (fn [addr]
                                                       (when (and addr (not (string/blank? addr)))
                                                         (swap! transaction assoc :to (string/trim addr)))))}
           [react/view {:flex-direction     :row
                        :padding-horizontal 18}
            [vector-icons/icon :main-icons/paste {:color colors/white-transparent}]
            [react/view {:flex            1
                         :flex-direction  :row
                         :justify-content :center}
             [react/text {:style {:color       colors/white
                                  :font-size   15
                                  :line-height 22}}
              (i18n/label :t/paste)]]]]
          [common/action-button {:underlay-color   colors/white-transparent
                                 :background-color colors/black-transparent
                                 :on-press         (fn []
                                                     (re-frame/dispatch
                                                      [:request-permissions {:permissions [:camera]
                                                                             :on-allowed (partial open-qr-scanner chain all-tokens contacts text-input transaction)
                                                                             :on-denied  #(utils/set-timeout
                                                                                           (fn []
                                                                                             (utils/show-popup (i18n/label :t/error)
                                                                                                               (i18n/label :t/camera-access-error)))
                                                                                           50)}]))}
           [react/view {:flex-direction     :row
                        :padding-horizontal 18}
            [vector-icons/icon :main-icons/qr {:color colors/white-transparent}]
            [react/view {:flex            1
                         :flex-direction  :row
                         :justify-content :center}
             [react/text {:style {:color       colors/white
                                  :font-size   15
                                  :line-height 22}}
              (i18n/label :t/scan)]]]]
          (let [disabled? (string/blank? (:to @transaction))]
            [common/action-button {:disabled?        disabled?
                                   :underlay-color   colors/black-transparent
                                   :background-color (if disabled? colors/blue colors/white)
                                   :on-press         #(events/chosen-recipient chain @transaction on-address
                                                                               (fn on-error [code]
                                                                                 (reset! error-message (i18n/label code))))}
             [react/text {:style {:color       (if disabled?
                                                 (colors/alpha colors/white 0.3)
                                                 colors/blue)
                                  :font-size   15
                                  :line-height 22}}
              (i18n/label :t/next)]])]]))))

(defn render-contact [on-contact contact]
  {:pre [(fn? on-contact) (map? contact) (:address contact)]}
  [react/touchable-highlight {:underlay-color colors/white-transparent
                              :on-press       #(on-contact contact)}
   [react/view {:flex           1
                :flex-direction :row
                :padding-right  23
                :padding-left   16
                :padding-top    12}
    [react/view {:margin-top 3}
     [photos/photo (:photo-path contact) {:size list.styles/image-size}]]
    [react/view {:margin-left 16
                 :flex        1}
     [react/view {:accessibility-label :contact-name-text
                  :margin-bottom       2}
      [react/text {:style {:font-size   15
                           :font-weight "500"
                           :line-height 22
                           :color       colors/white}}
       (:name contact)]]
     [react/text {:style               {:font-size   15
                                        :line-height 22
                                        :color       colors/white-transparent}
                  :accessibility-label :contact-address-text}
      (ethereum/normalized-address (:address contact))]]]])

(defn choose-contact-view [{:keys [contacts on-contact]}]
  {:pre [(every? map? contacts) (fn? on-contact)]}
  (if (empty? contacts)
    (common/info-page (i18n/label :t/wallet-no-contacts))
    [react/view {:flex 1}
     [list/flat-list {:data      contacts
                      :key-fn    :address
                      :render-fn (partial render-contact on-contact)}]]))

(defn render-choose-recipient [{:keys [modal? web3 contacts all-tokens transaction network network-status]}]
  (let [transaction     (reagent/atom transaction)
        chain           (ethereum/network->chain-keyword network)
        native-currency (tokens/native-currency chain)
        online?         (= :online network-status)]
    [wallet.components/simple-screen {:avoid-keyboard? (not modal?)
                                      :status-bar-type (if modal? :modal-wallet :wallet)}
     [common/toolbar :wallet (i18n/label :t/send-to) nil]
     [simple-tab-navigator
      {:address  {:name      (i18n/label :t/wallet-address-tab-title)
                  :component (choose-address-view
                              {:web3        web3
                               :chain       chain
                               :all-tokens  all-tokens
                               :contacts    contacts
                               :transaction transaction
                               :on-address  #(re-frame/dispatch [:navigate-to :wallet-choose-amount
                                                                 {:transaction     (swap! transaction assoc :to %)
                                                                  :native-currency native-currency
                                                                  :modal?          modal?}])})}
       :contacts {:name      (i18n/label :t/wallet-contacts-tab-title)
                  :component (partial choose-contact-view
                                      {:contacts   contacts
                                       :on-contact (fn [{:keys [address] :as contact}]
                                                     (re-frame/dispatch
                                                      [:navigate-to :wallet-choose-amount
                                                       {:modal?          modal?
                                                        :native-currency native-currency
                                                        :contact         contact
                                                        :transaction     (swap! transaction assoc :to address)}]))})}}
      :address]]))
