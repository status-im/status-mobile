(ns status-im2.contexts.wallet.send.select-address.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.constants :as constants]
    [status-im2.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im2.contexts.wallet.item-types :as types]
    [status-im2.contexts.wallet.send.select-address.style :as style]
    [status-im2.contexts.wallet.send.select-address.tabs.view :as tabs]
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
    (let [scanned-address       (rf/sub [:wallet/scanned-address])
          send-address          (rf/sub [:wallet/wallet-send-to-address])
          valid-ens-or-address? (rf/sub [:wallet/valid-ens-or-address?])
          chain-id              (rf/sub [:chain-id])
          contacts              (rf/sub [:contacts/active])]
      [quo/address-input
       {:on-focus              #(reset! input-focused? true)
        :on-blur               #(reset! input-focused? false)
        :on-scan               (fn []
                                 (rn/dismiss-keyboard!)
                                 (rf/dispatch [:open-modal :scan-address]))
        :ens-regex             constants/regx-ens
        :address-regex         constants/regx-address
        :scanned-value         (or send-address scanned-address)
        :on-detect-address     #(debounce/debounce-and-dispatch
                                 [:wallet/validate-address %]
                                 300)
        :on-detect-ens         (fn [text cb]
                                 (debounce/debounce-and-dispatch
                                  [:wallet/find-ens text contacts chain-id cb]
                                  300))
        :on-change-text        (fn [text]
                                 (when-not (= scanned-address text)
                                   (rf/dispatch [:wallet/clean-scanned-address]))
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
                                                    {:address  address
                                                     :stack-id :wallet-select-address}]))))
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
  (fn []
    (let [local-suggestion (rf/sub [:wallet/local-suggestions])]
      [rn/view {:style {:flex 1}}
       [rn/flat-list
        {:data                         local-suggestion
         :content-container-style      {:flex-grow 1}
         :key-fn                       :id
         :on-scroll-to-index-failed    identity
         :keyboard-should-persist-taps :handled
         :render-fn                    suggestion-component}]])))

(defn- f-view
  []
  (let [on-close       (fn []
                         (rf/dispatch [:wallet/clean-scanned-address])
                         (rf/dispatch [:wallet/clean-local-suggestions])
                         (rf/dispatch [:wallet/select-address-tab nil])
                         (rf/dispatch [:navigate-back]))
        on-change-tab  #(rf/dispatch [:wallet/select-address-tab %])
        input-value    (reagent/atom "")
        input-focused? (reagent/atom false)]
    (fn []
      (let [selected-tab          (or (rf/sub [:wallet/send-tab]) (:id (first tabs-data)))
            valid-ens-or-address? (boolean (rf/sub [:wallet/valid-ens-or-address?]))]
        (rn/use-effect (fn []
                         (fn []
                           (rf/dispatch [:wallet/clean-scanned-address])
                           (rf/dispatch [:wallet/clean-local-suggestions]))))
        [rn/scroll-view
         {:content-container-style      style/container
          :keyboard-should-persist-taps :handled
          :scroll-enabled               false}
         [account-switcher/view
          {:on-press      on-close
           :switcher-type :select-account}]
         [quo/text-combinations
          {:title                     (i18n/label :t/send-to)
           :container-style           style/title-container
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
             [local-suggestions-list]]
            (when (> (count @input-value) 0)
              [quo/button
               {:accessibility-label :continue-button
                :type                :primary
                :disabled?           (not valid-ens-or-address?)
                :container-style     style/button
                :on-press            #(rf/dispatch [:wallet/select-send-address
                                                    {:address  @input-value
                                                     :stack-id :wallet-select-address}])}
               (i18n/label :t/continue)])]
           [:<>
            [quo/tabs
             {:style            style/tabs
              :container-style  style/tabs-content
              :size             32
              :default-active   selected-tab
              :data             tabs-data
              :scrollable?      true
              :scroll-on-press? true
              :on-change        on-change-tab}]
            [tabs/view {:selected-tab selected-tab}]])]))))

(defn view
  []
  [:f> f-view])

