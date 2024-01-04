(ns legacy.status-im.ui.screens.wallet.accounts.views
  (:require
    [legacy.status-im.keycard.login :as keycard.login]
    [legacy.status-im.qr-scanner.core :as qr-scanner]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as components.core]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.wallet.account.views :as account.views]
    [legacy.status-im.ui.screens.wallet.accounts.common :as common]
    [legacy.status-im.ui.screens.wallet.accounts.sheets :as sheets]
    [legacy.status-im.ui.screens.wallet.accounts.styles :as styles]
    [legacy.status-im.ui.screens.wallet.buy-crypto.views :as buy-crypto]
    [quo.core :as quo]
    [quo.foundations.colors :as quo.colors]
    [re-frame.core :as re-frame]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :as views]))

(views/defview account-card
  [{:keys [name color address type wallet] :as account} keycard? card-width]
  (views/letsubs [currency        [:wallet-legacy/currency]
                  portfolio-value [:account-portfolio-value address]
                  prices-loading? [:prices-loading?]]
    [react/touchable-highlight
     {:on-press            #(re-frame/dispatch [:navigate-to :wallet-account account])
      :accessibility-label (str "accountcard" name)}
     [react/view {:style (styles/card color card-width)}
      [react/view {:flex-direction :row :align-items :center :justify-content :space-between}
       [react/text {:style styles/card-name} name]
       [components.core/text styles/card-address
        address]]
      [react/view {:flex-direction :row :align-items :center :justify-content :space-between}
       [react/view {:style {:flex-direction :row}}
        (if prices-loading?
          [react/small-loading-indicator :colors/white-persist]
          [react/text
           {:style               styles/card-value
            :accessibility-label "account-total-value"} portfolio-value])
        [react/text {:style styles/card-value-currency} (str " " (:code currency))]]
       (let [icon (cond
                    (= type :watch)
                    :main-icons/show

                    (and (not= type :watch) keycard?)
                    :main-icons/keycard-account)]
         (when icon
           [icons/icon icon
            {:container-style styles/card-icon-type
             :color           color}]))
       [react/touchable-highlight
        {:style    styles/card-icon-more
         :on-press #(re-frame/dispatch
                     [:bottom-sheet/show-sheet-old
                      {:content        (fn [] [sheets/account-card-actions account type wallet])
                       :content-height 130}])}
        [icons/icon :main-icons/more {:color colors/white-persist}]]]]]))

(defn add-card
  [card-width]
  [react/touchable-highlight
   {:on-press            #(re-frame/dispatch [:bottom-sheet/show-sheet-old
                                              {:content        sheets/add-account
                                               :content-height 260}])
    :accessibility-label "add-new-account"}
   [react/view {:style (styles/add-card card-width)}
    [icons/icon :main-icons/add-circle {:color colors/blue}]
    [react/text {:style styles/add-text} (i18n/label :t/add-account)]]])

(views/defview assets
  []
  (views/letsubs [{:keys [tokens]} [:wallet-legacy/all-visible-assets-with-values]
                  currency         [:wallet-legacy/currency]]
    [:<>
     (for [item tokens]
       ^{:key (:name item)}
       [common/render-asset item nil nil (:code currency)])]))

(views/defview send-button
  []
  (views/letsubs [account [:multiaccount/default-account]]
    [react/view styles/send-button-container
     [components.core/button
      {:accessibility-label :send-transaction-button
       :type                :scale
       :on-press            #(re-frame/dispatch [:wallet-legacy/prepare-transaction-from-wallet
                                                 account])}
      [react/view (styles/send-button)
       [icons/icon :main-icons/send {:color colors/white-persist}]]]]))

(defn dot
  []
  (fn [{:keys [selected]}]
    [react/view {:style (styles/dot-style selected)}]))

(defn dots-selector
  [{:keys [n selected]}]
  [react/view {:style (styles/dot-selector)}
   (for [i (range n)]
     ^{:key i}
     [dot {:selected (= selected i)}])])

;;ACCOUNTS OLD
(views/defview accounts-old
  []
  (views/letsubs [accounts     [:multiaccount/visible-accounts]
                  keycard?     [:keycard-multiaccount?]
                  window-width [:dimensions/window-width]
                  index        (reagent/atom 0)]
    (let [card-width (quot window-width 1.1)
          page-width (styles/page-width card-width)]
      [react/view
       {:style {:align-items     :center
                :flex            1
                :justify-content :flex-end}}
       [react/scroll-view
        {:horizontal                        true
         :deceleration-rate                 "fast"
         :snap-to-interval                  page-width
         :shows-horizontal-scroll-indicator false
         :scroll-event-throttle             64
         :on-scroll                         #(let [x (.-nativeEvent.contentOffset.x ^js %)]
                                               (reset! index (Math/max (Math/round (/ x page-width))
                                                                       0)))}
        [react/view styles/dot-container
         (doall
          (for [account accounts]
            ^{:key account}
            [account-card account keycard? card-width]))
         [add-card card-width]]]
       (let [columns    (Math/ceil (/ (inc (count accounts)) 2))
             totalwidth (* (styles/page-width card-width) columns)
             n          (Math/ceil (/ totalwidth window-width))]
         (when (> n 1)
           [dots-selector
            {:selected @index
             :n        n}]))])))

;;TOTAL VALUE OLD
(views/defview total-value-old
  [{:keys [animation minimized]}]
  (views/letsubs [currency           [:wallet-legacy/currency]
                  portfolio-value    [:portfolio-value]
                  empty-balances?    [:empty-balances?]
                  frozen-card?       [:keycard/frozen-card?]
                  {:keys [mnemonic]} [:profile/profile]]
    [reanimated/view {:style (styles/container {:minimized minimized})}
     (when (or
            (and frozen-card? minimized)
            (and mnemonic minimized (not empty-balances?)))
       [reanimated/view {:style (styles/accounts-mnemonic {:animation animation})}
        [react/touchable-highlight
         {:on-press #(re-frame/dispatch
                      (if frozen-card?
                        [::keycard.login/reset-pin]
                        [:navigate-to :backup-seed]))}
         [react/view
          {:flex-direction :row
           :align-items    :center}
          [react/view
           {:width            14
            :height           14
            :background-color colors/gray
            :border-radius    7
            :align-items      :center
            :justify-content  :center
            :margin-right     9}
           [react/text
            {:style {:color       colors/white
                     :font-size   13
                     :font-weight "700"}}
            "!"]]
          [react/text
           {:style               {:color colors/gray}
            :accessibility-label :back-up-your-seed-phrase-warning}
           (if frozen-card?
             (i18n/label :t/your-card-is-frozen)
             (i18n/label :t/back-up-your-seed-phrase))]]]])

     [reanimated/view
      {:style          (styles/value-container {:minimized minimized
                                                :animation animation})
       :pointer-events :none}
      [reanimated/view {:style {:justify-content :center}}
       [components.core/text
        {:animated? true
         :weight    :semi-bold
         :style     (styles/value-text {:minimized minimized})}
        portfolio-value
        [components.core/text
         {:animated? true
          :size      :inherit
          :weight    :inherit
          :color     :secondary}
         (str " " (:code currency))]]]]
     (when-not minimized
       [reanimated/view
        [components.core/text {:color :secondary}
         (i18n/label :t/wallet-total-value)]])]))

(views/defview total-value
  []
  (views/letsubs [currency        [:wallet-legacy/currency]
                  portfolio-value [:portfolio-value]]
    [react/view {:padding-vertical 12}
     [quo/text (i18n/label :t/wallet-total-value)]
     [quo/text {:size :heading-1 :weight :semi-bold}
      (str portfolio-value " " (:code currency))]
     [react/scroll-view {:horizontal true}]]))

(views/defview accounts
  [selected-account-atom]
  (views/letsubs [visible-accounts [:multiaccount/visible-accounts]]
    (do
      (reset! selected-account-atom (:address (first visible-accounts)))
      (let [accounts-data (for [account visible-accounts]
                            {:label (:name account)
                             :id    (:address account)})]
        [react/scroll-view
         {:horizontal                        true
          :shows-horizontal-scroll-indicator false
          :scroll-event-throttle             64
          :margin-top                        12
          :margin-bottom                     20}
         [react/view {:flex-direction :row}
          [quo/tabs
           {:default-active (:address (first visible-accounts))
            :on-change      #(reset! selected-account-atom %)
            :data           accounts-data}]
          [quo/button
           {:type     :grey
            :size     32
            :on-press #(re-frame/dispatch [:bottom-sheet/show-sheet-old
                                           {:content        sheets/add-account
                                            :content-height 260}])}
           "Add account"]]]))))

(defn accounts-overview
  []
  (let [mnemonic              @(re-frame/subscribe [:mnemonic])
        selected-account-atom (reagent/atom nil)]
    (fn []
      [react/view
       {:style {:flex             1
                :padding-top      (safe-area/get-top)
                :background-color (quo.colors/theme-colors quo.colors/neutral-5
                                                           quo.colors/neutral-95)}}
       [react/view {:padding-horizontal 20}
        [react/view {:flex-direction :row :height 56 :align-items :center :justify-content :flex-end}
         [quo/button
          {:icon                true
           :size                32
           :type                :grey
           :accessibility-label :accounts-qr-code
           :on-press            #(re-frame/dispatch
                                  [::qr-scanner/scan-code
                                   {:handler :wallet-legacy.send/qr-scanner-result}])}
          :i/placeholder]
         [react/view {:width 12}]
         [quo/button
          {:icon                true
           :size                32
           :type                :grey
           :on-press            #(re-frame/dispatch [:bottom-sheet/show-sheet-old
                                                     {:content (sheets/accounts-options mnemonic)}])
           :accessibility-label :accounts-more-options}
          :i/placeholder]]
        [total-value]
        [accounts selected-account-atom]]
       [account.views/account-new @selected-account-atom]])))

(defn accounts-overview-old
  []
  (let [mnemonic @(re-frame/subscribe [:mnemonic])
        mainnet? @(re-frame/subscribe [:mainnet?])]
    [react/view
     {:style {:flex 1}}
     [components.core/animated-header
      {:extended-header    total-value-old
       :refresh-control    common/refresh-control
       :refreshing-sub     (re-frame/subscribe [:wallet-legacy/refreshing-history?])
       :refreshing-counter common/updates-counter
       :use-insets         true
       :right-accessories  [{:on-press            #(re-frame/dispatch
                                                    [::qr-scanner/scan-code
                                                     {:handler :wallet-legacy.send/qr-scanner-result}])
                             :icon                :main-icons/qr
                             :accessibility-label :accounts-qr-code}
                            {:on-press            #(re-frame/dispatch [:bottom-sheet/show-sheet-old
                                                                       {:content (sheets/accounts-options
                                                                                  mnemonic)}])
                             :icon                :main-icons/more
                             :accessibility-label :accounts-more-options}]}
      [accounts-old]
      (when mainnet?
        [buy-crypto/banner])
      [assets]
      [react/view {:height 68}]]
     [send-button]]))
