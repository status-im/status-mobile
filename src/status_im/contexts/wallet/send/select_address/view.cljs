(ns status-im.contexts.wallet.send.select-address.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
    [status-im.contexts.wallet.item-types :as types]
    [status-im.contexts.wallet.send.select-address.style :as style]
    [status-im.contexts.wallet.send.select-address.tabs.view :as tabs]
    [status-im.feature-flags :as ff]
    [status-im.setup.hot-reload :as hot-reload]
    [utils.address :as utils-address]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def ^:private tabs-data
  [{:id :tab/recent :label (i18n/label :t/recent) :accessibility-label :recent-tab}
   {:id :tab/saved :label (i18n/label :t/saved) :accessibility-label :saved-tab}
   (when (ff/enabled? ::wallet.contacts)
     {:id :tab/contacts :label (i18n/label :t/contacts) :accessibility-label :contacts-tab})
   {:id :tab/my-accounts :label (i18n/label :t/my-accounts) :accessibility-label :my-accounts-tab}])

(defn- validate-address
  [address]
  (debounce/debounce-and-dispatch
   (cond
     (<= (count address) 0)                    [:wallet/address-validation-failed address]
     (utils-address/eip-3770-address? address) [:wallet/address-validation-success address]
     :else                                     [:wallet/address-validation-failed address])
   300))

(defn- address-input
  [input-value input-focused?]
  (fn []
    (let [current-screen-id        (rf/sub [:view-id])
          scanned-address          (rf/sub [:wallet/scanned-address])
          send-address             (rf/sub [:wallet/wallet-send-to-address])
          recipient                (rf/sub [:wallet/send-recipient])
          recipient-plain-address? (= send-address recipient)
          valid-ens-or-address?    (rf/sub [:wallet/valid-ens-or-address?])
          contacts                 (rf/sub [:contacts/active])]
      [quo/address-input
       {:on-focus              #(reset! input-focused? true)
        :on-blur               #(reset! input-focused? false)
        :on-scan               (fn [on-result]
                                 (rn/dismiss-keyboard!)
                                 (rf/dispatch [:wallet/clean-scanned-address])
                                 (rf/dispatch [:open-modal :screen/wallet.scan-address
                                               {:on-result on-result}]))
        :ens-regex             constants/regx-ens
        :scanned-value         (or (when recipient-plain-address? send-address) scanned-address)
        :address-regex         utils-address/regx-eip-3770-address
        :on-detect-address     (fn [address]
                                 (when (or (= current-screen-id :screen/wallet.select-address)
                                           (= current-screen-id :screen/wallet.scan-address))
                                   ; ^ this check is to prevent effect being triggered when screen
                                   ; is loaded but not being shown to the user (deep in the
                                   ; navigation stack) and avoid undesired behaviors
                                   (validate-address address)))
        :on-detect-ens         (fn [text cb]
                                 (when (or (= current-screen-id :screen/wallet.select-address)
                                           (= current-screen-id :screen/wallet.scan-address))
                                   ; ^ this check is to prevent effect being triggered when screen
                                   ; is loaded but not being shown to the user (deep in the
                                   ; navigation stack) and avoid undesired behaviors
                                   (debounce/debounce-and-dispatch
                                    [:wallet/find-ens text contacts cb]
                                    300)))
        :on-change-text        (fn [text]
                                 (rf/dispatch [:wallet/clean-local-suggestions])
                                 (rf/dispatch [:wallet/searching-address])
                                 (validate-address text)
                                 (reset! input-value text))
        :valid-ens-or-address? valid-ens-or-address?}])))

(defn- ens-linked-address
  [{:keys [address networks]}]
  (let [theme (quo.theme/use-theme)]
    [quo/text
     {:size  :paragraph-2
      :style style/network-text-container}
     (map (fn [network]
            ^{:key (str network)}
            [quo/text
             {:size  :paragraph-2
              :style {:color (colors/resolve-color network theme)}}
             (str (subs (name network) 0 3) ":")])
          networks)
     [quo/text
      {:size   :paragraph-2
       :weight :monospace}
      address]]))

(defn- suggestion-component
  []
  (fn [{:keys [type ens address accounts primary-name public-key ens-name color]
        :as   local-suggestion}
       _ _ _]
    (let [props {:on-press      (fn []
                                  (let [address (if accounts (:address (first accounts)) address)]
                                    (when-not ens
                                      (rf/dispatch [:wallet/select-send-address
                                                    {:address   address
                                                     :recipient local-suggestion
                                                     :stack-id  :screen/wallet.select-address}]))))
                 :active-state? false}]
      (cond
        (= type types/saved-address)
        [quo/saved-address
         (assoc props
                :user-props
                {:name                primary-name
                 :address             public-key
                 :ens                 ens-name
                 :customization-color color})]
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

(defn- footer
  [input-value]
  (let [local-suggestion-address (rf/sub [:wallet/local-suggestions->full-address])
        color                    (rf/sub [:wallet/current-viewing-account-color])
        valid-ens-or-address?    (boolean (rf/sub [:wallet/valid-ens-or-address?]))]
    [quo/button
     {:accessibility-label :continue-button
      :type                :primary
      :disabled?           (not valid-ens-or-address?)
      :on-press            (fn []
                             (let [address              (or
                                                         local-suggestion-address
                                                         input-value)
                                   [_ splitted-address] (network-utils/split-network-full-address
                                                         address)]
                               (rf/dispatch
                                [:wallet/select-send-address
                                 {:address address
                                  :recipient {:label
                                              (utils/get-shortened-address
                                               splitted-address)
                                              :recipient-type :address}
                                  :stack-id
                                  :screen/wallet.select-address}])))
      :customization-color color}
     (i18n/label :t/continue)]))

(defn view
  []
  (let [on-close       (fn []
                         (rf/dispatch [:wallet/clean-scanned-address])
                         (rf/dispatch [:wallet/clean-local-suggestions])
                         (rf/dispatch [:wallet/clean-selected-collectible])
                         (rf/dispatch [:wallet/clean-send-address])
                         (rf/dispatch [:wallet/select-address-tab nil])
                         (rf/dispatch [:wallet/clean-current-viewing-account
                                       :ignore-just-complete-transaction]))
        on-change-tab  #(rf/dispatch [:wallet/select-address-tab %])
        input-value    (reagent/atom "")
        input-focused? (reagent/atom false)]
    (fn []
      (let [selected-tab          (or (rf/sub [:wallet/send-tab]) (:id (first tabs-data)))
            valid-ens-or-address? (boolean (rf/sub [:wallet/valid-ens-or-address?]))
            searching-address?    (rf/sub [:wallet/searching-address?])]
        (hot-reload/use-safe-unmount on-close)
        [floating-button-page/view
         {:content-container-style      {:flex 1}
          :footer-container-padding     0
          :keyboard-should-persist-taps :always
          :header                       [account-switcher/view
                                         {:on-press      #(rf/dispatch [:navigate-back])
                                          :margin-top    (safe-area/get-top)
                                          :switcher-type :select-account}]
          :footer                       (when-not (string/blank? @input-value)
                                          [footer @input-value])}
         [quo/page-top
          {:title                     (i18n/label :t/send-to)
           :title-accessibility-label :title-label}]
         [address-input input-value input-focused?]
         [quo/divider-line]
         (when (and (not valid-ens-or-address?) (> (count @input-value) 0) (not searching-address?))
           [rn/view {:style {:padding 20}}
            [quo/info-message
             {:status :error
              :icon   :i/info
              :size   :default}
             (i18n/label :t/invalid-address)]])
         (if (or @input-focused? (> (count @input-value) 0))
           [rn/keyboard-avoiding-view
            {:style                    {:flex 1}
             :keyboard-vertical-offset 26}
            [rn/view {:style {:flex 1 :padding 8}}
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
