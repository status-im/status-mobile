(ns status-im.contexts.wallet.send.select-address.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.item-types :as types]
    [status-im.contexts.wallet.send.select-address.style :as style]
    [status-im.contexts.wallet.send.select-address.tabs.view :as tabs]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def ^:private tabs-data
  [{:id :tab/recent :label (i18n/label :t/recent) :accessibility-label :recent-tab}
   {:id :tab/saved :label (i18n/label :t/saved) :accessibility-label :saved-tab}
   {:id :tab/contacts :label (i18n/label :t/contacts) :accessibility-label :contacts-tab}
   {:id :tab/my-accounts :label (i18n/label :t/my-accounts) :accessibility-label :my-accounts-tab}])

(defn- address-input
  [input-value input-focused?]
  (fn []
    (let [current-screen-id        (rf/sub [:navigation/current-screen-id])
          scanned-address          (rf/sub [:wallet/scanned-address])
          send-address             (rf/sub [:wallet/wallet-send-to-address])
          recipient                (rf/sub [:wallet/wallet-send-recipient])
          recipient-plain-address? (= send-address recipient)
          valid-ens-or-address?    (rf/sub [:wallet/valid-ens-or-address?])
          chain-id                 (rf/sub [:chain-id])
          contacts                 (rf/sub [:contacts/active])]
      [quo/address-input
       {:on-focus              #(reset! input-focused? true)
        :on-blur               #(reset! input-focused? false)
        :on-scan               (fn []
                                 (rn/dismiss-keyboard!)
                                 (rf/dispatch [:wallet/clean-scanned-address])
                                 (rf/dispatch [:open-modal :screen/wallet.scan-address]))
        :ens-regex             constants/regx-ens
        :scanned-value         (or (when recipient-plain-address? send-address) scanned-address)
        :address-regex         constants/regx-multichain-address
        :on-detect-address     #(when (or (= current-screen-id :screen/wallet.select-address)
                                          (= current-screen-id :screen/wallet.scan-address))
                                  ; ^ this check is to prevent effect being triggered when screen is
                                  ; loaded but not being shown to the user (deep in the navigation
                                  ; stack) and avoid undesired behaviors
                                  (debounce/debounce-and-dispatch
                                   [:wallet/validate-address %]
                                   300))
        :on-detect-ens         (fn [text cb]
                                 (when (or (= current-screen-id :screen/wallet.select-address)
                                           (= current-screen-id :screen/wallet.scan-address))
                                   ; ^ this check is to prevent effect being triggered when screen
                                   ; is loaded but not being shown to the user (deep in the
                                   ; navigation stack) and avoid undesired behaviors
                                   (debounce/debounce-and-dispatch
                                    [:wallet/find-ens text contacts chain-id cb]
                                    300)))
        :on-change-text        (fn [text]
                                 (when (empty? text)
                                   (rf/dispatch [:wallet/clean-local-suggestions]))
                                 (reset! input-value text))
        :valid-ens-or-address? valid-ens-or-address?}])))

(defn- ens-linked-address
  [{:keys [address networks theme]}]
  [quo/text
   {:size  :paragraph-2
    :style {:padding-horizontal 12
            :padding-top        4}}
   (map (fn [network]
          ^{:key (str network)}
          [quo/text
           {:size  :paragraph-2
            :style {:color (colors/resolve-color network theme)}}
           (str (subs (name network) 0 3) ":")])
        networks)
   [quo/text
    {:size   :paragraph-2
     :weight :monospace
     :style  {:color (colors/theme-colors colors/neutral-100 colors/white theme)}}
    address]])

(defn- suggestion-component
  []
  (fn [{:keys [type ens address accounts primary-name public-key ens-name color] :as local-suggestion} _
       _ _]
    (let [props {:on-press      (fn []
                                  (let [address (if accounts (:address (first accounts)) address)]
                                    (when-not ens
                                      (rf/dispatch [:wallet/select-send-address
                                                    {:address   address
                                                     :token?    false
                                                     :recipient local-suggestion
                                                     :stack-id  :screen/wallet.select-address}]))))
                 :active-state? false}]
      (cond
        (= type types/saved-address)
        [quo/saved-address
         (merge props
                {:user-props {:name                primary-name
                              :address             public-key
                              :ens                 ens-name
                              :customization-color color}})]
        (= type types/saved-contact-address)
        [quo/saved-contact-address (merge props local-suggestion)]
        (and (not ens) (= type types/address))
        [quo/address (merge props local-suggestion)]
        (and ens (= type types/address))
        [ens-linked-address local-suggestion]
        :else nil))))

(defn- local-suggestions-list
  []
  (let [local-suggestion (rf/sub [:wallet/local-suggestions])]
    [rn/view {:style {:flex 1}}
     [rn/flat-list
      {:data                         local-suggestion
       :content-container-style      {:flex-grow 1}
       :key-fn                       :id
       :on-scroll-to-index-failed    identity
       :keyboard-should-persist-taps :handled
       :render-fn                    suggestion-component}]]))

(defn- f-view
  []
  (let [on-close       (fn []
                         (rf/dispatch [:wallet/clean-scanned-address])
                         (rf/dispatch [:wallet/clean-local-suggestions])
                         (rf/dispatch [:wallet/clean-selected-token])
                         (rf/dispatch [:wallet/clean-selected-collectible])
                         (rf/dispatch [:wallet/clean-send-address])
                         (rf/dispatch [:wallet/select-address-tab nil])
                         (rf/dispatch [:navigate-back]))
        on-change-tab  #(rf/dispatch [:wallet/select-address-tab %])
        input-value    (reagent/atom "")
        input-focused? (reagent/atom false)]
    (fn []
      (let [selected-tab          (or (rf/sub [:wallet/send-tab]) (:id (first tabs-data)))
            token                 (rf/sub [:wallet/wallet-send-token])
            valid-ens-or-address? (boolean (rf/sub [:wallet/valid-ens-or-address?]))
            {:keys [color]}       (rf/sub [:wallet/current-viewing-account])]
        [floating-button-page/view
         {:footer-container-padding 0
          :header                   [account-switcher/view
                                     {:on-press      on-close
                                      :margin-top    (safe-area/get-top)
                                      :switcher-type :select-account}]
          :footer                   (when (> (count @input-value) 0)
                                      [quo/button
                                       {:accessibility-label :continue-button
                                        :type                :primary
                                        :disabled?           (not valid-ens-or-address?)
                                        :on-press            #(rf/dispatch
                                                               [:wallet/select-send-address
                                                                {:address @input-value
                                                                 :token? (some? token)
                                                                 :stack-id
                                                                 :screen/wallet.select-address}])
                                        :customization-color color}
                                       (i18n/label :t/continue)])}
         [quo/page-top
          {:title                     (i18n/label :t/send-to)
           :title-accessibility-label :title-label}]
         [address-input input-value input-focused?]
         [quo/divider-line]
         (if (or @input-focused? (> (count @input-value) 0))
           [rn/keyboard-avoiding-view
            {:style                    {:flex 1}
             :keyboard-vertical-offset 26}
            [rn/view
             {:style {:flex    1
                      :padding 8}}
             [local-suggestions-list]]]
           [:<>
            [quo/tabs
             {:style           style/tabs
              :container-style style/tabs-content
              :size            32
              :default-active  selected-tab
              :data            tabs-data
              :scrollable?     true
              :on-change       on-change-tab}]
            [tabs/view {:selected-tab selected-tab}]])]))))

(defn view
  []
  [:f> f-view])
