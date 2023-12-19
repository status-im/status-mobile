(ns legacy.status-im.ui.screens.wallet.send.views
  (:require-macros [legacy.status-im.utils.views :as views])
  (:require
    [legacy.status-im.ui.components.chat-icon.screen :as chat-icon]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as components.core]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.keyboard-avoid-presentation :as kb-presentation]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.toolbar :as toolbar]
    [legacy.status-im.ui.components.tooltip.views :as tooltip]
    [legacy.status-im.ui.screens.wallet.components.views :as components]
    [legacy.status-im.ui.screens.wallet.send.sheets :as sheets]
    [legacy.status-im.ui.screens.wallet.send.styles :as styles]
    [legacy.status-im.utils.utils :as utils]
    [legacy.status-im.wallet.utils :as wallet.utils]
    [quo.core :as quo]
    [re-frame.core :as re-frame]
    [status-im2.contexts.profile.utils :as profile.utils]
    [utils.address :as address]
    [utils.i18n :as i18n]
    [utils.money :as money]))

(defn header
  [{:keys [label small-screen?]}]
  [react/view (styles/header small-screen?)
   [react/view {:flex 1}
    [react/text {:style (merge {:typography :title-bold} (when small-screen? {:font-size 15}))}
     (i18n/label (or label :t/send-transaction))]]])

(defn asset-selector
  [{:keys [request? token from]} window-width]
  (let [{:keys [name icon color]} token]
    [react/touchable-highlight
     {:on-press (when-not request?
                  #(do
                     (re-frame/dispatch [:dismiss-keyboard])
                     (re-frame/dispatch [:bottom-sheet/show-sheet-old
                                         {:content        (fn [] [sheets/assets (:address from)])
                                          :content-height 300}])))}
     [react/view
      {:style               {:flex-direction :row
                             :align-items    :center
                             :margin-left    16}
       :accessibility-label :choose-asset-button}
      (if icon
        [components/token-icon
         (assoc icon
                :style       {:background-color colors/gray-lighter
                              :border-radius    50}
                :image-style {:width 32 :height 32})]
        [chat-icon/custom-icon-view-list name color 32])
      [react/text
       {:style           {:margin-left 8
                          :max-width   (/ window-width 4)}
        :number-of-lines 2}
       (wallet.utils/display-symbol token)]
      (when-not request?
        [icons/icon :main-icons/dropdown {:color colors/gray}])]]))

(defn render-account
  [account {:keys [amount decimals] :as token} event]
  [list.item/list-item
   {:icon     [chat-icon/custom-icon-view-list (:name account) (:color account)]
    :title    (:name account)
    :subtitle (when token
                (str (wallet.utils/format-amount amount decimals)
                     " "
                     (wallet.utils/display-symbol token)))
    :chevron  true
    :on-press #(do
                 (re-frame/dispatch [:dismiss-keyboard])
                 (re-frame/dispatch [:bottom-sheet/show-sheet-old
                                     {:content        (fn [] [sheets/accounts-list :from event])
                                      :content-height 300}]))}])

(defn render-contact
  [{:keys [address] :as contact} from-chat?]
  (if from-chat?
    [list.item/list-item
     {:title    (profile.utils/displayed-name contact)
      :subtitle [components.core/text
                 {:monospace true
                  :color     :secondary}
                 (utils/get-shortened-checksum-address address)]
      :icon     [chat-icon/contact-icon-contacts-tab contact]}]
    [list.item/list-item
     (merge {:title               (if-not contact
                                    (i18n/label :t/wallet-choose-recipient)
                                    [components.core/text
                                     {:size      :large
                                      :monospace true}
                                     (utils/get-shortened-checksum-address
                                      (if (string? contact) contact address))])
             :accessibility-label :choose-recipient-button
             :on-press            #(do
                                     (re-frame/dispatch [:dismiss-keyboard])
                                     (re-frame/dispatch
                                      [:wallet-legacy.send/navigate-to-recipient-code]))
             :chevron             true}
            (when-not contact
              {:icon  :main-icons/add
               :theme :accent}))]))

(defn set-max
  [token]
  [react/touchable-highlight
   {:on-press
    #(when token
       (re-frame/dispatch [:wallet-legacy.send/set-max-amount token]))}
   [react/view {:style (styles/set-max-button)}
    [react/text {:style {:color colors/blue}} (i18n/label :t/set-max)]]])

(defn fiat-value
  [amount {sym :symbol} prices wallet-currency]
  (when-let [price (get-in prices [(keyword sym) (keyword (:code wallet-currency)) :price])]
    (let [norm-amount (js/parseFloat (money/normalize amount))
          amount      (if (js/isNaN norm-amount) 0 norm-amount)]
      [react/text
       {:style {:color         colors/gray
                :margin-left   16
                :margin-bottom 8
                :font-size     15
                :line-height   22}}
       (str (i18n/format-currency (* amount price) (:code wallet-currency))
            " "
            (:code wallet-currency))])))

(views/defview request-transaction
  [_]
  (views/letsubs [{:keys [amount-error amount-text from token sign-enabled?] :as tx}
                  [:wallet-legacy.request/prepare-transaction-with-balance]
                  window-width [:dimensions/window-width]
                  prices [:prices]
                  wallet-currency [:wallet-legacy/currency]]
    [kb-presentation/keyboard-avoiding-view {:style {:flex 1}}
     [:<>
      [react/scroll-view
       {:style                        {:flex 1}
        :keyboard-should-persist-taps :handled}
       [react/view {:style (styles/sheet)}
        [react/view
         {:flex-direction     :row
          :padding-horizontal 24
          :align-items        :center
          :margin-vertical    16}
         [react/text-input
          {:style               {:font-size   38
                                 :color       (if amount-error colors/red colors/black)
                                 :flex-shrink 1}
           :keyboard-type       :decimal-pad
           :auto-capitalize     :words
           :accessibility-label :amount-input
           :default-value       amount-text
           :auto-focus          true
           :on-change-text      #(re-frame/dispatch [:wallet-legacy.request/set-amount-text %])
           :placeholder         "0.0 "}]
         [asset-selector tx window-width]
         (when amount-error
           [tooltip/tooltip amount-error
            {:bottom-value 2
             :font-size    12}])]
        [fiat-value amount-text token prices wallet-currency]
        [components/separator]
        [components.core/list-header
         (i18n/label :t/to-capitalized)]
        [react/view {:flex-direction :row :flex 1 :align-items :center}
         [react/view {:flex 1}
          [render-account from token :wallet-legacy.request/set-field]]]]]
      [toolbar/toolbar
       {:show-border? true
        :right
        [components.core/button
         {:type                :secondary
          :after               :main-icon/next
          :accessibility-label :request-transaction-bottom-sheet
          :disabled            (not sign-enabled?)
          :on-press            #(do
                                  (re-frame/dispatch
                                   [:wallet-legacy.ui/request-transaction-button-clicked tx])
                                  (re-frame/dispatch [:navigate-back]))}
         (i18n/label :t/wallet-request)]}]]]))

(views/defview prepare-send-transaction
  [_]
  (views/letsubs [{:keys [amount-error amount-text
                          request?
                          from token to sign-enabled? from-chat?]
                   :as   tx}
                  [:wallet-legacy.send/prepare-transaction-with-balance]
                  prices [:prices]
                  wallet-currency [:wallet-legacy/currency]
                  window-width [:dimensions/window-width]]
    (let [to-norm (address/normalized-hex (if (string? to) to (:address to)))]
      [kb-presentation/keyboard-avoiding-view {:style {:flex 1}}
       [:<>
        [quo/page-nav
         {:type                :title
          :text-align          :left
          :title               (i18n/label :t/send-transaction)
          :icon-name           :i/arrow-left
          :on-press            (fn []
                                 (re-frame/dispatch [:navigate-back])
                                 (re-frame/dispatch [:wallet-legacy/cancel-transaction-command]))
          :accessibility-label :back-button}]
        [react/scroll-view
         {:style                        {:flex 1}
          :keyboard-should-persist-taps :handled}
         [react/view {:style (styles/sheet)}
          [react/view
           {:flex-direction     :row
            :padding-horizontal 16
            :align-items        :center
            :margin-top         12
            :margin-bottom      4}
           [react/text-input
            {:style               {:font-size 38
                                   :max-width (- (* (/ window-width 4) 3) 106)
                                   :color     (if amount-error colors/red colors/black)}
             :keyboard-type       :decimal-pad
             :auto-capitalize     :words
             :accessibility-label :amount-input
             :default-value       amount-text
             :editable            (not request?)
             :auto-focus          true
             :on-change-text      #(re-frame/dispatch [:wallet-legacy.send/set-amount-text %])
             :placeholder         "0.0 "}]
           [asset-selector tx window-width]
           (when amount-error
             [tooltip/tooltip
              (if from
                amount-error
                (i18n/label :t/select-account-first))
              {:bottom-value 2
               :font-size    12}])]
          [fiat-value amount-text token prices wallet-currency]
          (when-not (or request? from-chat?)
            [set-max token])
          [components/separator]
          [components.core/list-header (i18n/label :t/from-capitalized)]
          [react/view {:flex-direction :row :flex 1 :align-items :center}
           [react/view {:flex 1}
            [render-account from token :wallet-legacy.send/set-field]]]
          [components.core/list-header
           (i18n/label :t/to-capitalized)]
          [react/view {:flex-direction :row :flex 1 :align-items :center}
           [react/view {:flex 1}
            [render-contact to from-chat?]]]]]
        [toolbar/toolbar
         {:show-border? true
          :right
          [quo/button
           {:accessibility-label :send-transaction-bottom-sheet
            :type                :secondary
            :after               :main-icon/next
            :disabled            (not sign-enabled?)
            :on-press            #(do
                                    (re-frame/dispatch [:navigate-back])
                                    (re-frame/dispatch
                                     [(cond
                                        request?
                                        :wallet-legacy.ui/sign-transaction-button-clicked-from-request
                                        from-chat?
                                        :wallet-legacy.ui/sign-transaction-button-clicked-from-chat
                                        :else
                                        :wallet-legacy.ui/sign-transaction-button-clicked) tx]))}

           (if (and (not request?) from-chat? (not to-norm))
             (i18n/label :t/wallet-send)
             (i18n/label :t/next))]}]]])))
