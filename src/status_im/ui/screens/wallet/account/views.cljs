(ns status-im.ui.screens.wallet.account.views
  (:require
    [quo.core :as quo]
    [quo.design-system.colors :as colors]
    [quo.design-system.spacing :as spacing]
    [quo2.core :as quo2]
    [quo2.components.markdown.text :as quo2.text]
    [quo2.foundations.colors :as quo2.colors]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    [status-im.ethereum.core :as ethereum]
    [utils.i18n :as i18n]
    [status-im.ui.components.animation :as animation]
    [status-im.ui.components.icons.icons :as icons]
    [status-im.ui.components.react :as react]
    [status-im.ui.components.tabs :as tabs]
    [status-im.ui.components.topbar :as topbar]
    [status-im.ui.screens.wallet.account.styles :as styles]
    [status-im.ui.screens.wallet.accounts.common :as common]
    [status-im.ui.screens.wallet.accounts.sheets :as sheets]
    [status-im.ui.screens.wallet.buy-crypto.views :as buy-crypto]
    [status-im.ui.screens.wallet.collectibles.views :as collectibles.views]
    [status-im.ui.screens.wallet.transactions.views :as history]
    [status-im2.config :as config]
    [utils.re-frame :as rf])
  (:require-macros [status-im.utils.views :as views]))

(def state (reagent/atom {:tab :assets}))
(def selected-tab (reagent/atom :tokens))

(defn button
  [label icon color handler]
  [react/touchable-highlight {:on-press handler :style {:flex 1}}
   [react/view {:flex 1 :align-items :center :justify-content :center}
    [react/view {:flex-direction :row :align-items :center}
     [icons/icon icon {:color color}]
     [react/text {:style {:margin-left 8 :color color}} label]]]])

(def button-group-height 52)

(views/defview account-card
  [{:keys [address color type] :as account}]
  (views/letsubs [currency        [:wallet/currency]
                  portfolio-value [:account-portfolio-value address]
                  window-width    [:dimensions/window-width]
                  prices-loading? [:prices-loading?]]
    [react/view {:style (styles/card window-width color)}
     [react/view {:padding 16 :padding-bottom 12 :flex 1 :justify-content :space-between}
      [react/view {:style {:flex-direction :row}}
       (if prices-loading?
         [react/small-loading-indicator :colors/white-persist]
         [react/text {:style {:font-size 32 :color colors/white-persist :font-weight "600"}}
          portfolio-value])
       [react/text {:style {:font-size 32 :color colors/white-transparent-persist :font-weight "600"}}
        (str " " (:code currency))]]
      [quo/text
       {:number-of-lines 1
        :ellipsize-mode  :middle
        :monospace       true
        :size            :small
        :style           {:width       (/ window-width 3)
                          :line-height 22
                          :color       colors/white-transparent-70-persist}}
       (ethereum/normalized-hex address)]]
     [react/view {:position :absolute :top 12 :right 12}
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:wallet/share-popover address])}
       [icons/icon :main-icons/share
        {:color               colors/white-persist
         :accessibility-label :share-wallet-address-icon}]]]
     [react/view
      {:height                     button-group-height
       :background-color           colors/black-transparent-20
       :border-bottom-right-radius 8
       :border-bottom-left-radius  8
       :flex-direction             :row}
      (if (= type :watch)
        [react/view {:flex 1 :align-items :center :justify-content :center}
         [react/text {:style {:margin-left 8 :color colors/white-persist}}
          (i18n/label :t/watch-only)]]
        [button
         (i18n/label :t/wallet-send)
         :main-icons/send
         colors/white-persist
         #(re-frame/dispatch [:wallet/prepare-transaction-from-wallet account])])
      [react/view {:style (styles/divider)}]
      [button
       (i18n/label :t/receive)
       :main-icons/receive
       colors/white-persist
       #(re-frame/dispatch [:wallet/share-popover address])]]]))

(views/defview transactions
  [address]
  (views/letsubs [data [:wallet.transactions.history/screen address]]
    [history/history-list data address]))

(defn opensea-link
  [address]
  [react/touchable-highlight
   {:on-press #(re-frame/dispatch [:browser.ui/open-url (str "https://opensea.io/" address)])}
   [react/view
    {:style {:flex               1
             :padding-horizontal 14
             :flex-direction     :row
             :align-items        :center
             :background-color   colors/blue-light
             :height             52}}
    [icons/tiny-icon
     :tiny-icons/tiny-external
     {:color           colors/blue
      :container-style {:margin-right 5}}]
    [react/text
     {:style {:color colors/blue}}
     (i18n/label :t/check-on-opensea)]]])

(views/defview assets-and-collections-new
  [address]
  (views/letsubs [{:keys [tokens]}       [:wallet/visible-assets-with-values address]
                  currency               [:wallet/currency]
                  opensea-enabled?       [:opensea-enabled?]
                  collectible-collection [:wallet/collectible-collection address]]
    ;ethereum-network? [:ethereum-network?]]
    (let [tab @selected-tab]
      [react/view {:flex 1}
       [react/view {:padding-horizontal 20 :padding-bottom 20}
        [quo2/tabs
         {:size           24
          :on-change      #(reset! selected-tab %)
          :default-active :tokens
          :data           [{:id :tokens :label "Tokens"}
                           {:id :nft :label "NFTs"}
                           {:id :activity :label "Activity"}]}]]
       (cond
         (= tab :tokens)
         [react/scroll-view
          (for [item tokens]
            ^{:key (:name item)}
            [common/render-asset-new item nil nil (:code currency)])]
         (= tab :nft)
         [:<>
          [opensea-link address]
          ;; Hide collectibles behind a feature flag
          (when config/collectibles-enabled?
            (cond
              (not opensea-enabled?)
              [collectibles.views/enable-opensea-view]

              (and opensea-enabled? (seq collectible-collection))
              [collectibles.views/nft-collections address]

              :else
              [react/view {:align-items :center :margin-top 32}
               [react/text {:style {:color colors/gray}}
                (i18n/label :t/no-collectibles)]]))]
         (= tab :activity)
         [transactions address])])))

(views/defview bottom-send-recv-buttons
  [{:keys [address type] :as account} anim-y]
  [react/animated-view
   {:style {:background-color colors/white
            :bottom           0
            :flex-direction   :row
            :height           button-group-height
            :position         :absolute
            :shadow-offset    {:width 0 :height 1}
            :shadow-opacity   0.75
            :shadow-radius    1
            :transform        [{:translateY anim-y}]
            :width            "100%"}}
   (if (= type :watch)
     [react/view {:flex 1 :align-items :center :justify-content :center}
      [react/text {:style {:margin-left 8 :color colors/blue-persist}}
       (i18n/label :t/watch-only)]]
     [button
      (i18n/label :t/wallet-send)
      :main-icons/send
      colors/blue-persist
      #(re-frame/dispatch [:wallet/prepare-transaction-from-wallet account])])
   [button
    (i18n/label :t/receive)
    :main-icons/receive
    colors/blue-persist
    #(re-frame/dispatch [:wallet/share-popover address])]])

(defn anim-listener
  [anim-y scroll-y]
  (let [to-show (atom false)]
    (animation/add-listener
     scroll-y
     (fn [anim]
       (let [y-trigger 149]
         (cond
           (and (>= (.-value anim) y-trigger) (not @to-show))
           (animation/start
            (styles/bottom-send-recv-buttons-raise anim-y)
            #(reset! to-show true))

           (and (< (.-value anim) y-trigger) @to-show)
           (animation/start
            (styles/bottom-send-recv-buttons-lower anim-y button-group-height)
            #(reset! to-show false))))))))

(defn round-action-button
  [{:keys [icon title on-press]}]
  [react/view
   {:style {:flex            1
            :align-items     :center
            :margin-vertical (:large spacing/spacing)}}
   [react/touchable-opacity
    {:style    styles/round-action-button
     :on-press on-press}
    (icons/icon icon {:color colors/white})]
   [quo/text
    {:color :secondary
     :size  :small
     :style {:margin-top (:tiny spacing/spacing)}}
    title]])

(defn top-actions
  []
  [react/view {:style styles/top-actions}
   [round-action-button
    {:icon     :main-icons/add
     :title    (i18n/label :t/buy-crypto)
     :on-press #(re-frame/dispatch [:buy-crypto.ui/open-screen])}]
   [round-action-button
    {:icon     :main-icons/change
     :title    (i18n/label :t/swap)
     :on-press #(re-frame/dispatch [:open-modal :token-swap])}]])

(views/defview assets-and-collections
  [address]
  (views/letsubs [{:keys [tokens]}       [:wallet/visible-assets-with-values address]
                  currency               [:wallet/currency]
                  opensea-enabled?       [:opensea-enabled?]
                  collectible-collection [:wallet/collectible-collection address]
                  ethereum-network?      [:ethereum-network?]]
    (let [{:keys [tab]} @state]
      [react/view {:flex 1}
       [react/view {:flex-direction :row :margin-bottom 8 :padding-horizontal 4}
        [tabs/tab-title state :assets (i18n/label :t/wallet-assets) (= tab :assets)]
        (when ethereum-network?
          [tabs/tab-title state :nft (i18n/label :t/wallet-collectibles) (= tab :nft)])
        [tabs/tab-title state :history (i18n/label :t/history) (= tab :history)]]
       [quo/separator {:style {:margin-top -8}}]
       (cond
         (= tab :assets)
         [:<>
          (for [item tokens]
            ^{:key (:name item)}
            [common/render-asset item nil nil (:code currency)])]
         (= tab :nft)
         [:<>
          [opensea-link address]
          ;; Hide collectibles behind a feature flag
          (when config/collectibles-enabled?
            (cond
              (not opensea-enabled?)
              [collectibles.views/enable-opensea-view]

              (and opensea-enabled? (seq collectible-collection))
              [collectibles.views/nft-collections address]

              :else
              [react/view {:align-items :center :margin-top 32}
               [react/text {:style {:color colors/gray}}
                (i18n/label :t/no-collectibles)]]))]
         (= tab :history)
         [transactions address])])))

(defn account-new
  [selected-account]
  (let [;{:keys [name address] :as account} (rf/sub [:account-by-address selected-account])
        currency        (rf/sub [:wallet/currency])
        portfolio-value (rf/sub [:account-portfolio-value selected-account])
        width           (rf/sub [:dimensions/window-width])
        button-width    (/ (- width 40 (* 2 12)) 3)]
    ;fetching-error (rf/sub [:wallet/fetching-error])]
    [react/view
     {:flex                    1
      :background-color        (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-90)
      :border-top-left-radius  20
      :border-top-right-radius 20
      :elevation               4
      :shadow-opacity          1
      :shadow-radius           20
      :shadow-color            (:shadow-01 @colors/theme)
      :shadow-offset           {:width 0 :height 4}}
     [react/view {:padding 20}
      [quo2.text/text {:size :heading-2 :weight :semi-bold} (str portfolio-value " " (:code currency))]]
     [react/view
      [react/scroll-view {:horizontal true :margin-bottom 32 :showsHorizontalScrollIndicator false}
       [react/view {:width 20}]
       [quo2/button {:size 56 :width button-width :above :i/placeholder} "Buy"]
       [react/view {:width 12}]
       [quo2/button {:size 56 :width button-width :type :secondary :above :i/placeholder} "Send"]
       [react/view {:width 12}]
       [quo2/button {:size 56 :width button-width :type :secondary :above :i/placeholder}
        "Receive"]
       [react/view {:width 12}]
       [quo2/button {:size 56 :width button-width :type :secondary :above :i/placeholder} "Swap"]
       [react/view {:width 20}]]]
     [assets-and-collections-new selected-account]]))

(views/defview account
  []
  (views/letsubs [{:keys [name address] :as current-account} [:multiaccount/current-account]
                  fetching-error                             [:wallet/fetching-error]]
    (let [anim-y   (animation/create-value button-group-height)
          scroll-y (animation/create-value 0)]
      (anim-listener anim-y scroll-y)
      [:<>
       [topbar/topbar
        {:title name
         :right-accessories
         [{:icon     :main-icons/more
           :on-press #(re-frame/dispatch [:bottom-sheet/show-sheet-old
                                          {:content        sheets/account-settings
                                           :content-height 60}])}]}]
       [react/animated-scroll-view
        {:contentContainerStyle {:padding-bottom button-group-height}
         :on-scroll             (animation/event
                                 [{:nativeEvent {:contentOffset {:y scroll-y}}}]
                                 {:useNativeDriver true})
         :scrollEventThrottle   1
         :refreshControl        (common/refresh-control
                                 (and
                                  @common/updates-counter
                                  @(re-frame/subscribe [:wallet/refreshing-history?])))}
        (when fetching-error
          [react/view
           {:style {:flex        1
                    :align-items :center
                    :margin      8}}
           [icons/icon
            :main-icons/warning
            {:color           :red
             :container-style {:background-color (colors/get-color :negative-02)
                               :height           40
                               :width            40
                               :border-radius    20
                               :align-items      :center
                               :justify-content  :center}}]
           [react/view
            {:style {:justify-content   :center
                     :align-items       :center
                     :margin-top        8
                     :margin-horizontal 67.5
                     :text-align        :center}}
            [quo/text
             {:color :secondary
              :size  :small
              :style {:text-align :center}}
             (i18n/label :t/transfers-fetching-failure)]]])
        [react/view {:padding-left 16}
         [react/scroll-view {:horizontal true}
          [react/view {:flex-direction :row :padding-top 8 :padding-bottom 12}
           [account-card current-account]]]]
        (if config/swap-enabled?
          [top-actions]
          [buy-crypto/banner])
        [assets-and-collections address]]
       [bottom-send-recv-buttons current-account anim-y]])))
