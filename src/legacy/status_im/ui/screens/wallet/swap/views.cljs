(ns legacy.status-im.ui.screens.wallet.swap.views
  (:require
    [clojure.string :as string]
    [legacy.status-im.ethereum.tokens :as tokens]
    [legacy.status-im.ui.components.chat-icon.screen :as chat-icon]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.keyboard-avoid-presentation :as kb-presentation]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.search-input.view :as search-input]
    [legacy.status-im.ui.components.slider :as slider]
    [legacy.status-im.ui.components.toolbar :as toolbar]
    [legacy.status-im.ui.components.topbar :as topbar]
    [legacy.status-im.ui.screens.wallet.components.views :as wallet.components]
    [legacy.status-im.wallet.swap.core :as wallet-legacy.swap]
    [legacy.status-im.wallet.utils :as wallet.utils]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn render-asset
  [{{:keys
     [icon decimals amount color value]
     :as token}
    :token
    currency :currency
    on-press :on-press}]
  [list.item/list-item
   {:title [quo/text {:weight :medium}
            [quo/text {:weight :inherit}
             (str (if amount
                    (wallet.utils/format-amount amount decimals)
                    "...")
                  " ")]
            [quo/text
             {:color  :secondary
              :weight :inherit}
             (wallet.utils/display-symbol token)]]
    :on-press on-press
    :subtitle (str (if value value "...") " " currency)
    :accessibility-label
    (str (:symbol token) "-asset-value")
    :icon (if icon
            [wallet.components/token-icon icon]
            [chat-icon/custom-icon-view-list (:name token) color])}])

(defn asset-selector
  []
  (let [{:keys [address]} (rf/sub [:multiaccount/current-account])
        {:keys [tokens]}  (rf/sub [:wallet-legacy/visible-assets-with-values address])
        source?           (rf/sub [:wallet-legacy/modal-selecting-source-token?])
        currency          (rf/sub [:wallet-legacy/currency])]
    [:<>
     [topbar/topbar
      {:title  (if source?
                 (i18n/label :t/select-token-to-swap)
                 (i18n/label :t/select-token-to-receive))
       :modal? true}]

     [search-input/search-input-old
      {:search-active? true}]

     [react/scroll-view
      (for [token tokens]
        ^{:key (:name token)}
        [render-asset
         {:token    token
          :on-press #(re-frame/dispatch
                      [(if source?
                         ::wallet-legacy.swap/set-from-token
                         ::wallet-legacy.swap/set-to-token)
                       (:symbol token)])
          :currency (:code currency)}])]]))

(defn pill-button
  [{:keys [on-press label margin-left]}]
  [react/touchable-opacity
   {:on-press on-press
    :style    {:background-color   colors/blue-light
               :padding-horizontal 12
               :padding-vertical   2
               :border-radius      24
               :margin-left        (or margin-left 8)}}
   [quo/text
    {:color  :link
     :weight :medium} label]])

(defn token-display
  "Show token and act as an anchor to open selector."
  [{:keys [token source?]}]
  (let [token-icon-source (-> token :icon :source)]
    [react/touchable-highlight
     {:on-press #(re-frame/dispatch [::wallet-legacy.swap/open-asset-selector-modal source?])}
     [react/view
      {:style {:flex-direction     :row
               :align-items        :center
               :border-width       1
               :border-color       colors/gray-lighter
               :border-radius      8
               :margin-left        16
               :padding-horizontal 8
               :padding-vertical   2}
       :accessibility-label
       :choose-asset-button}
      [quo/text {:style {:margin-right 8}}
       (-> token :symbol name)]
      [react/image
       {:source (if (fn? token-icon-source)
                  (token-icon-source)
                  token-icon-source)}]]]))

(defn token-input
  "Component to get the amount and type of tokens"
  [{:keys [amount error label token max-from source?]}]
  (let [window-width (rf/sub [:dimensions/window-width])]
    [react/view
     {:style {:justify-content :space-between
              :flex-direction  :row
              :align-items     :center}}
     [react/view {:flex 2}
      [react/view
       {:flex-direction :row
        :align-items    :center}
       [quo/text label]
       (when max-from
         [pill-button
          {:on-press #()
           :label    "Max 0.043"}])]
      [react/text-input
       {:style               {:font-size 38
                              :max-width (- (* (/ window-width 4) 3) 106)
                              :color     (if error colors/red colors/black)}
        :keyboard-type       :decimal-pad
        :auto-capitalize     :words
        :accessibility-label :amount-input
        :default-value       amount
        :editable            true
        :auto-focus          true
        :on-change-text      #(re-frame/dispatch [(when source?
                                                    ::wallet-legacy.swap/set-from-token-amount)
                                                  %])
        :placeholder         "0.0"}]]
     [token-display
      {:token   token
       :source? source?}]]))

(defn separator-with-icon
  []
  [react/view
   {:margin-vertical 8}
   [quo/separator]
   [react/touchable-opacity
    {:on-press #(re-frame/dispatch [::wallet-legacy.swap/switch-from-token-with-to])}
    [react/view
     {:style {:background-color colors/gray-lighter
              :width            40
              :height           40
              :border-radius    40
              :border-width     4
              :border-color     colors/white
              :margin-top       -20
              :margin-bottom    -20
              :align-self       :center
              :align-items      :center
              :justify-content  :center}}
     [react/image
      {:source (icons/icon-source :main-icons/change)
       :style  {:tint-color colors/gray
                :transform  [{:rotate "90deg"}]}}]]]])

(defn floating-card
  [{:keys [icon title body on-press]}]
  [react/view
   {:style {:border-width  1
            :padding       2 ;; need a padding because border breaks otherwise
            :border-radius 12
            :margin-top    12
            :border-color  colors/gray-lighter}}
   [list.item/list-item
    {:title title
     :subtitle body
     :active-background-enabled
     false
     :on-press on-press
     :theme :main
     :chevron true
     :icon [react/view
            {:style {:background-color colors/blue-light
                     :padding          8
                     :border-radius    4}}
            (icons/icon icon {:color :dark})]}]])

(defn card-body-row
  [k value primary?]
  [react/view {:flex-direction :row}
   [quo/text {:color (when-not primary? :secondary)} k]
   [quo/text
    {:style {:margin-right 4}
     :color (when-not primary? :secondary)} ":"]
   [quo/text
    {:ellipsize-mode  :middle
     :number-of-lines 1
     :style           {:width "50%"}
     :color           (when-not primary? :secondary)
     :weight          :semi-bold} value]])

(defn transaction-fee-card
  [{:keys [gas-amount price-limit tip-limit gas-in-eth gas-in-usd]}]
  [floating-card
   {:title    (i18n/label :t/transaction-fee)
    :icon     :main-icons/gas
    :on-press #(re-frame/dispatch [:open-modal :token-swap-advanced-transaction-fee])
    :body     [react/view
               [card-body-row (i18n/label :t/gas-amount-limit) gas-amount]
               [card-body-row (i18n/label :t/per-gas-price-limit) price-limit]
               [card-body-row (i18n/label :t/per-gas-tip-limit) tip-limit]
               [card-body-row (i18n/label :t/total-gas)
                (str gas-in-eth " • $" gas-in-usd)
                true]]}])

(defn swap-details-card
  [{:keys [slippage price-impact]}]
  [floating-card
   {:title    (i18n/label :t/swap-details)
    :icon     :main-icons/change
    :on-press #(re-frame/dispatch [:open-modal :token-swap-advanced-swap-details])
    :body     [react/view
               [card-body-row (i18n/label :t/slippage) (str slippage " %")]
               [card-body-row (i18n/label :t/price-impact) (str price-impact " %")]]}])

(defn nonce-card
  [{:keys [nonce]}]
  [floating-card
   {:title    (i18n/label :t/nonce)
    :icon     :main-icons/channel
    :on-press #(re-frame/dispatch [:open-modal :token-swap-advanced-nonce])
    :body     [react/view
               [card-body-row (i18n/label :t/nonce) nonce]]}])

(defn approve-token-card
  [{:keys [token contract-address approve-limit]}]
  [floating-card
   {:title    (i18n/label :t/approve)
    :icon     :main-icons/check
    :on-press #(re-frame/dispatch [:open-modal :token-swap-advanced-approve-token])
    :body     [react/view
               [card-body-row (i18n/label :t/token) token]
               [card-body-row (i18n/label :t/contract-address) contract-address]
               [card-body-row (i18n/label :t/approve-limit) approve-limit]]}])

(defn advanced-input
  [{:keys [label label-help value on-change after]}]
  [react/view
   {:style {:padding-horizontal 16
            :margin-bottom      16}}
   [react/view
    {:style {:flex-direction  :row
             :justify-content :space-between}}
    [quo/text {} label]
    label-help]

   [quo/text-input
    {:default-value  (str value)
     :show-cancel    false
     :style          {:margin-top    12
                      :border-radius 8}
     :after          {:component after}
     :on-change-text on-change}]])

(defn help-label-kv
  [{k :key v :value}]
  [react/view {:style {:flex-direction :row}}
   [quo/text {:color :secondary} k]
   [quo/text
    {:color :secondary
     :style {:margin-right 4}} ":"]
   [quo/text {:color :secondary} v]])

(defn nonce-modal
  []
  (let [last-txn-nonce 22
        nonce          23]
    [kb-presentation/keyboard-avoiding-view
     {:style         {:flex       1
                      :margin-top 16}
      :ignore-offset true}
     [advanced-input
      {:label      (i18n/label :t/nonce)
       :label-help [help-label-kv
                    {:key   (i18n/label :t/last-transaction)
                     :value last-txn-nonce}]
       :value      nonce
       :on-change  #()
       :on-save    #()}]
     [toolbar/toolbar
      {:show-border? true
       :left         [quo/button {:type :secondary}
                      (i18n/label :t/cancel)]
       :right        [quo/button {:theme :accent}
                      (i18n/label :t/save)]}]]))

(defn approve-token-modal
  []
  [quo/text "modal"])

(defn transaction-fee-modal
  []
  (let [gas-amount-limit                21000
        gas-amount                      21000
        per-gas-price-limit             7.3
        current-avg-per-gas-price-limit 7.3
        per-gas-tip-limit               150
        current-avg-per-gas-tip-limit   150]
    [kb-presentation/keyboard-avoiding-view
     {:style         {:flex       1
                      :margin-top 16}
      :ignore-offset true}
     [react/view {:flex 1}
      [advanced-input
       {:label      (i18n/label :t/gas-amount-limit)
        :label-help [help-label-kv
                     {:key   (i18n/label :t/limit)
                      :value gas-amount-limit}]
        :value      gas-amount
        :after      [quo/text {:color :secondary} (i18n/label :t/gwei)]
        :on-change  #()
        :on-save    #()}]

      [advanced-input
       {:label      (i18n/label :t/per-gas-price-limit)
        :label-help [help-label-kv
                     {:key   (i18n/label :t/current-average)
                      :value current-avg-per-gas-price-limit}]
        :value      per-gas-price-limit
        :after      [quo/text {:color :secondary} (i18n/label :t/gwei)]
        :on-change  #()
        :on-save    #()}]

      [advanced-input
       {:label      (i18n/label :t/per-gas-price-limit)
        :label-help [help-label-kv
                     {:key   (i18n/label :t/current-average)
                      :value current-avg-per-gas-tip-limit}]
        :value      per-gas-tip-limit
        :after      [quo/text {:color :secondary} (i18n/label :t/gwei)]
        :on-change  #()
        :on-save    #()}]

      [list.item/list-item
       {:title             (i18n/label :t/maximum-fee)
        :text-size         :base
        :title-text-weight :regular
        :accessory         :text
        :accessory-text    [quo/text "0.3 ETH"]
        :container-style   {:margin-top 16}}]
      [quo/text
       {:color :secondary
        :style {:padding-horizontal 16}}
       (i18n/label :t/maximum-fee-desc)]]
     [toolbar/toolbar
      {:show-border? true
       :left         [quo/button {:type :secondary}
                      (i18n/label :t/cancel)]
       :right        [quo/button {:theme :accent}
                      (i18n/label :t/save)]}]]))

(defn swap-details-modal
  []
  (let [slippage-limit 20
        slippage       0.5
        price-impact   4]
    [kb-presentation/keyboard-avoiding-view
     {:style         {:flex       1
                      :margin-top 16}
      :ignore-offset true}
     [react/view {:flex 1}
      [advanced-input
       {:label      (str (i18n/label :t/slippage) " %")
        :label-help [help-label-kv
                     {:key   (i18n/label :t/limit)
                      :value (str slippage-limit "%")}]
        :value      slippage
        :on-change  #()
        :on-save    #()}]

      [list.item/list-item
       {:title             (i18n/label :t/price-impact)
        :text-size         :base
        :title-text-weight :regular
        :accessory         :text
        :accessory-text    (str price-impact "%")
        :container-style   {:margin-top 16}}]
      [quo/text
       {:color :secondary
        :style {:padding-horizontal 16}}
       (i18n/label :t/price-impact-desc)]]
     [toolbar/toolbar
      {:show-border? true
       :left         [quo/button {:type :secondary}
                      (i18n/label :t/cancel)]
       :right        [quo/button {:theme :accent}
                      (i18n/label :t/save)]}]]))

(defn advanced-settings
  []
  [react/view
   {:style {:flex               1
            :padding-horizontal 16}}
   [react/view {:align-self :flex-start}
    [pill-button
     {:label       (i18n/label :t/switch-to-simple-interface)
      :margin-left 0
      :on-press    #(re-frame/dispatch [::wallet-legacy.swap/set-advanced-mode false])}]]
   [transaction-fee-card
    {:gas-amount  21000
     :price-limit 74
     :tip-limit   21
     :gas-in-eth  0.0031
     :gas-in-usd  34.28}]
   [swap-details-card
    {:slippage     0.5
     :price-impact 4}]
   [approve-token-card
    {:token            "USDC"
     :contract-address "0x9f8f72aa9304c8b593d555f12ef6589cc3a579a2"
     :approve-limit    "unlimited"}]
   [nonce-card {:nonce 22}]])

(defn swap
  []
  (let [{:keys [name]}
        (rf/sub [:multiaccount/current-account])
        all-tokens (rf/sub [:wallet-legacy/all-tokens])
        from-symbol (rf/sub [:wallet-legacy/swap-from-token])
        to-symbol (rf/sub [:wallet-legacy/swap-to-token])
        advanced-mode? (rf/sub [:wallet-legacy/swap-advanced-mode?])
        amount "0.02"
        from-token (tokens/symbol->token all-tokens (or from-symbol :DGX))
        to-token (tokens/symbol->token all-tokens (or to-symbol :SNT))]

    [kb-presentation/keyboard-avoiding-view
     {:style         (merge
                      {:flex 1})
      :ignore-offset true}
     [topbar/topbar
      {:title    name
       :subtitle (string/upper-case (i18n/label :t/powered-by-paraswap))
       :modal?   true}]

     [react/view
      (merge {:padding-horizontal 16
              :margin-vertical    32}
             (when-not advanced-mode?
               {:flex 1}))
      [token-input
       {:amount   amount
        :error    nil
        :label    (i18n/label :t/amount)
        :token    from-token
        :source?  true
        :max-from 67.28}]

      [separator-with-icon]

      [token-input
       {:amount  "0.01"
        :error   nil
        :label   (i18n/label :t/minimum-received)
        :source? false
        :token   to-token}]]

     (when-not advanced-mode?
       [react/view
        {:style {:flex-direction     :row
                 :justify-content    :space-between
                 :padding-horizontal 16
                 :align-items        :center}}
        [react/view {:style {:flex-direction :row}}
         [quo/text {} (i18n/label :t/priority)]
         [pill-button
          {:label    (i18n/label :t/advanced)
           :on-press #(re-frame/dispatch [::wallet-legacy.swap/set-advanced-mode true])}]]

        [quo/text {:color :secondary} "0.0034 ETH/ $ 8.09"]])

     (comment
       (re-frame/dispatch [::wallet-legacy.swap/set-advanced-mode false]))

     (when-not advanced-mode?
       [react/view {:style {:padding-horizontal 16}}
        [slider/animated-slider
         {:minimum-value 0
          :maximum-value 100
          :style         {:margin-vertical 8}}]])

     (when advanced-mode?
       [quo/text "here"]
       [advanced-settings])

     [toolbar/toolbar
      {:show-border? true
       :right        [quo/button {:theme :accent}
                      (i18n/label :t/swap)]}]]))
