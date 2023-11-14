(ns status-im2.contexts.wallet.send.select-address.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.constants :as constants]
    [status-im2.contexts.wallet.item-types :as types]
    [status-im2.contexts.wallet.send.select-address.style :as style]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def tabs-data
  [{:id :tab/recent :label (i18n/label :t/recent) :accessibility-label :recent-tab}
   {:id :tab/saved :label (i18n/label :t/saved) :accessibility-label :saved-tab}
   {:id :tab/contacts :label (i18n/label :t/contacts) :accessibility-label :contacts-tab}
   {:id :tab/my-accounts :label (i18n/label :t/my-accounts) :accessibility-label :my-accounts-tab}])

(defn- tab-view
  [selected-tab]
  (case selected-tab
    :tab/recent      [quo/empty-state
                      {:title           (i18n/label :t/no-recent-transactions)
                       :description     (i18n/label :t/make-one-it-is-easy-we-promise)
                       :placeholder?    true
                       :container-style style/empty-container-style}]
    :tab/saved       [quo/empty-state
                      {:title           (i18n/label :t/no-saved-addresses)
                       :description     (i18n/label :t/you-like-to-type-43-characters)
                       :placeholder?    true
                       :container-style style/empty-container-style}]
    :tab/contacts    [quo/empty-state
                      {:title           (i18n/label :t/no-contacts)
                       :description     (i18n/label :t/no-contacts-description)
                       :placeholder?    true
                       :container-style style/empty-container-style}]
    :tab/my-accounts [quo/empty-state
                      {:title           (i18n/label :t/no-other-accounts)
                       :description     (i18n/label :t/here-is-a-cat-in-a-box-instead)
                       :placeholder?    true
                       :container-style style/empty-container-style}]))

(defn- address-input
  [input-value input-focused?]
  (fn []
    (let [scanned-address       (rf/sub [:wallet/scanned-address])
          send-address          (rf/sub [:wallet/send-address])
          valid-ens-or-address? (boolean (rf/sub [:wallet/valid-ens-or-address?]))]
      [quo/address-input
       {:on-focus              #(reset! input-focused? true)
        :on-blur               #(reset! input-focused? false)
        :on-scan               (fn []
                                 (rn/dismiss-keyboard!)
                                 (rf/dispatch [:open-modal :scan-address]))
        :ens-regex             constants/regx-ens
        :address-regex         constants/regx-address
        :scanned-value         (or send-address scanned-address)
        :on-detect-ens         #(debounce/debounce-and-dispatch
                                 [:wallet/validate-ens %]
                                 300)
        :on-detect-address     #(debounce/debounce-and-dispatch
                                 [:wallet/validate-address %]
                                 300)
        :on-change-text        (fn [text]
                                 (let [starts-like-eth-address (re-matches
                                                                constants/regx-address-fragment
                                                                text)]
                                   (when-not (= scanned-address text)
                                     (rf/dispatch [:wallet/clean-scanned-address]))
                                   (if starts-like-eth-address
                                     (rf/dispatch [:wallet/fetch-address-suggestions text])
                                     (rf/dispatch [:wallet/clean-local-suggestions]))
                                   (reset! input-value text)))
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
  (fn [{:keys [type ens address accounts] :as local-suggestion} _ _ _]
    (let [props {:on-press      (fn []
                                  (let [address (if accounts (:address (first accounts)) address)]
                                    (when-not ens (rf/dispatch [:wallet/select-send-address address]))))
                 :active-state? false}]
      (cond
        (= type types/saved-address)
        [quo/saved-address (merge props {:user-props local-suggestion})]
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

(defn- f-view-internal
  []
  (let [margin-top     (safe-area/get-top)
        selected-tab   (reagent/atom (:id (first tabs-data)))
        on-close       #(rf/dispatch [:navigate-back])
        on-change-tab  #(reset! selected-tab %)
        input-value    (reagent/atom "")
        input-focused? (reagent/atom false)]
    (fn []
      (let [valid-ens-or-address? (boolean (rf/sub [:wallet/valid-ens-or-address?]))]
        (rn/use-effect (fn []
                         (fn []
                           (rf/dispatch [:wallet/clean-scanned-address])
                           (rf/dispatch [:wallet/clean-local-suggestions]))))
        [rn/scroll-view
         {:content-container-style      (style/container margin-top)
          :keyboard-should-persist-taps :handled
          :scroll-enabled               false}
         [quo/page-nav
          {:icon-name           :i/close
           :on-press            on-close
           :accessibility-label :top-bar
           :right-side          :account-switcher
           :account-switcher    {:customization-color :purple
                                 :on-press            #(js/alert "Not implemented yet")
                                 :state               :default
                                 :emoji               "🍑"}}]
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
                :on-press            #(js/alert "Not implemented yet")}
               (i18n/label :t/continue)])]
           [:<>
            [quo/tabs
             {:style            style/tabs
              :container-style  style/tabs-content
              :size             32
              :default-active   @selected-tab
              :data             tabs-data
              :scrollable?      true
              :scroll-on-press? true
              :on-change        on-change-tab}]
            [tab-view @selected-tab]])]))))

(defn- view-internal
  []
  [:f> f-view-internal])

(def view (quo.theme/with-theme view-internal))
