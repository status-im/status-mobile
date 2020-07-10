(ns status-im.ui.screens.wallet.accounts.views
  (:require [quo.animated :as reanimated]
            [quo.core :as quo]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.accounts.sheets :as sheets]
            [status-im.ui.screens.wallet.accounts.styles :as styles]
            [status-im.utils.utils :as utils.utils]
            [status-im.wallet.utils :as wallet.utils]
            [status-im.keycard.login :as keycard.login])
  (:require-macros [status-im.utils.views :as views]))

(views/defview account-card [{:keys [name color address type] :as account}]
  (views/letsubs [currency        [:wallet/currency]
                  portfolio-value [:account-portfolio-value address]
                  prices-loading? [:prices-loading?]]
    [react/touchable-highlight
     {:on-press      #(re-frame/dispatch [:navigate-to :wallet-account account])
      :on-long-press #(re-frame/dispatch [:bottom-sheet/show-sheet
                                          {:content        (fn [] [sheets/send-receive account type])
                                           :content-height 130}])}
     [react/view {:style (styles/card color)}
      [react/view {:flex-direction :row :align-items :center :justify-content :space-between}
       [react/view {:style {:flex-direction :row}}
        (if prices-loading?
          [react/small-loading-indicator :colors/white-persist]
          [react/text {:style                   {:color colors/white-persist :font-weight "500"}
                       :accessibility-label     "account-total-value"} portfolio-value])
        [react/text {:style {:color colors/white-transparent-persist :font-weight "500"}} (str " " (:code currency))]]
       [react/touchable-highlight
        {:on-press #(re-frame/dispatch [:show-popover
                                        {:view :share-account :address address}])}
        [icons/icon :main-icons/share {:color colors/white-persist}]]]
      [react/view
       [react/text {:style {:color colors/white-persist :font-weight "500" :line-height 22}} name]
       [react/text {:number-of-lines 1 :ellipsize-mode :middle
                    :style {:line-height 22 :font-size 13
                            :font-family "monospace"
                            :color colors/white-transparent-70-persist}}
        address]]]]))

(defn add-card []
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                             {:content        sheets/add-account
                                                              :content-height 260}])}
   [react/view {:style (styles/add-card)}
    [react/view {:width       40 :height 40 :justify-content :center :border-radius 20
                 :align-items :center :background-color colors/blue-transparent-10 :margin-bottom 8}
     [icons/icon :main-icons/add {:color colors/blue}]]
    [react/text {:style {:color colors/blue}} (i18n/label :t/add-account)]]])

(defn tab-title [state key label active?]
  [react/view {:align-items :center}
   [react/touchable-highlight {:on-press #(swap! state assoc :tab key)
                               :underlay-color colors/gray-lighter
                               :style {:border-radius 8}}
    [react/view {:padding-horizontal 12 :padding-vertical 9}
     [react/text {:style {:font-weight "500" :color (if active? colors/black colors/gray) :line-height 22}}
      label]]]
   (when active?
     [react/view {:width 24 :height 3 :border-radius 4 :background-color colors/blue}])])

(defn render-asset [currency & [on-press]]
  (fn [{:keys [icon decimals amount color value] :as token}]
    [quo/list-item
     (merge {:title               [quo/text {:weight :medium}
                                   [quo/text {:weight :inherit}
                                    (str (wallet.utils/format-amount amount decimals)
                                         " ")]
                                   [quo/text {:color  :secondary
                                              :weight :inherit}
                                    (wallet.utils/display-symbol token)]]
             :subtitle            (str (if value value "0.00") " " currency)
             :accessibility-label (str (:symbol token)  "-asset-value")
             :icon                (if icon
                                    [list/item-image icon]
                                    [chat-icon/custom-icon-view-list (:name token) color])}
            (when on-press
              {:on-press #(on-press token)}))]))

(views/defview assets []
  (views/letsubs [{:keys [tokens]} [:wallet/all-visible-assets-with-values]
                  currency [:wallet/currency]]
    [list/flat-list {:data               tokens
                     :default-separator? false
                     :key-fn             :name
                     :render-fn          (render-asset (:code currency))}]))

(defn- request-camera-permissions []
  (let [options {:handler :wallet.send/qr-scanner-result}]
    (re-frame/dispatch
     [:request-permissions
      {:permissions [:camera]
       :on-allowed
       #(re-frame/dispatch [:wallet.send/qr-scanner-allowed options])
       :on-denied
       #(utils.utils/set-timeout
         (fn []
           (utils.utils/show-popup (i18n/label :t/error)
                                   (i18n/label :t/camera-access-error)))
         50)}])))

(views/defview send-button []
  (views/letsubs [account [:multiaccount/default-account]]
    [react/view styles/send-button-container
     [quo/button
      {:accessibility-label :send-transaction-button
       :type                :scale
       :on-press            #(re-frame/dispatch [:wallet/prepare-transaction-from-wallet account])}
      [react/view (styles/send-button)
       [icons/icon :main-icons/send {:color colors/white-persist}]]]]))

(views/defview accounts []
  (views/letsubs [accounts [:multiaccount/accounts]]
    [react/scroll-view {:horizontal                        true
                        :shows-horizontal-scroll-indicator false}
     [react/view {:flex-direction     :row
                  :padding-horizontal 8}
      (for [account accounts]
        ^{:key account}
        [account-card account])
      [add-card]]]))

(views/defview total-value [{:keys [animation minimized]}]
  (views/letsubs [currency           [:wallet/currency]
                  portfolio-value    [:portfolio-value]
                  empty-balances?    [:empty-balances?]
                  frozen-card?       [:keycard/frozen-card?]
                  {:keys [mnemonic]} [:multiaccount]]
    [reanimated/view {:style (styles/container {:minimized minimized})}
     (when (or
            (and frozen-card? minimized)
            (and mnemonic minimized (not empty-balances?)))
       [reanimated/view {:style (styles/accounts-mnemonic {:animation animation})}
        [react/touchable-highlight
         {:on-press #(re-frame/dispatch
                      (if frozen-card?
                        [::keycard.login/reset-pin]
                        [:navigate-to :profile-stack {:screen :backup-seed
                                                      :initial false}]))}
         [react/view {:flex-direction :row
                      :align-items    :center}
          [react/view {:width            14
                       :height           14
                       :background-color colors/gray
                       :border-radius    7
                       :align-items      :center
                       :justify-content  :center
                       :margin-right     9}
           [react/text {:style {:color       colors/white
                                :font-size   13
                                :font-weight "700"}}
            "!"]]
          [react/text {:style               {:color colors/gray}
                       :accessibility-label :back-up-your-seed-phrase-warning}
           (if frozen-card?
             (i18n/label :t/your-card-is-frozen)
             (i18n/label :t/back-up-your-seed-phrase))]]]])

     [reanimated/view {:style (styles/value-container {:minimized minimized
                                                       :animation animation})
                       :pointer-events :none}
      [reanimated/view {:style {:justify-content :center}}
       [quo/text {:animated? true
                  :weight    :semi-bold
                  :style     (styles/value-text {:minimized minimized})}
        portfolio-value
        [quo/text {:animated? true
                   :size      :inherit
                   :weight    :inherit
                   :color     :secondary}
         (str " " (:code currency))]]]]
     (when-not minimized
       [reanimated/view
        [quo/text {:color :secondary}
         (i18n/label :t/wallet-total-value)]])]))

(defn accounts-overview []
  (fn []
    (let [{:keys [mnemonic]} @(re-frame/subscribe [:multiaccount])]
      [react/view {:flex 1}
       [quo/animated-header
        {:extended-header   total-value
         :use-insets        true
         :right-accessories [{:on-press            #(request-camera-permissions)
                              :icon                :main-icons/qr
                              :accessibility-label :accounts-qr-code}
                             {:on-press            #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                                        {:content (sheets/accounts-options mnemonic)}])
                              :icon                :main-icons/more
                              :accessibility-label :accounts-more-options}]}
        [accounts]
        [assets]
        [react/view {:height 68}]]
       [send-button]])))
