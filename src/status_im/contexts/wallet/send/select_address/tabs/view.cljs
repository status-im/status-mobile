(ns status-im.contexts.wallet.send.select-address.tabs.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im.common.resources :as resources]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.select-address.tabs.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- my-accounts
  [theme]
  (let [other-accounts (rf/sub [:wallet/accounts-without-current-viewing-account])]
    (if (zero? (count other-accounts))
      [quo/empty-state
       {:title           (i18n/label :t/no-other-accounts)
        :description     (i18n/label :t/here-is-a-cat-in-a-box-instead)
        :image           (resources/get-themed-image :cat-in-box theme)
        :container-style style/empty-container-style}]
      [rn/view {:style style/my-accounts-container}
       (doall
        (for [{:keys [color address] :as account} other-accounts]
          ^{:key (str address)}
          (let [transformed-address (rf/sub [:wallet/account-address address
                                             (:network-preferences-names account)])]
            [quo/account-item
             {:account-props (assoc account
                                    :customization-color color
                                    :address             transformed-address
                                    :full-address?       true)
              :on-press      #(rf/dispatch [:wallet/select-send-address
                                            {:address   address
                                             :recipient {:recipient-type      :account
                                                         :label               (:name account)
                                                         :customization-color (:color account)
                                                         :emoji               (:emoji account)}
                                             :stack-id  :screen/wallet.select-address}])}])))])))

(defn- recent-transactions
  [theme]
  (let [recent-recipients (rf/sub [:wallet/recent-recipients])]
    (if (zero? (count recent-recipients))
      [quo/empty-state
       {:title           (i18n/label :t/no-recent-transactions)
        :description     (i18n/label :t/make-one-it-is-easy-we-promise)
        :image           (resources/get-themed-image :angry-man theme)
        :container-style style/empty-container-style}]
      (into [rn/view {:style style/my-accounts-container}]
            (map (fn [address]
                   [quo/address
                    {:address  address
                     :on-press #(rf/dispatch [:wallet/select-send-address
                                              {:address   address
                                               :recipient {:label          (utils/get-shortened-address
                                                                            address)
                                                           :recipient-type :address}
                                               :stack-id  :screen/wallet.select-address}])}]))
            recent-recipients))))

(defn- saved-address
  [{:keys [name address chain-short-names customization-color ens? ens]}]
  (let [full-address           (str chain-short-names address)
        on-press-saved-address (rn/use-callback
                                #(rf/dispatch
                                  [:wallet/select-send-address
                                   {:address   full-address
                                    :recipient {:label               name
                                                :customization-color customization-color
                                                :recipient-type      :saved-address}
                                    :stack-id  :screen/wallet.select-address}])
                                [full-address])]
    [quo/saved-address
     {:user-props      {:name                name
                        :address             full-address
                        :ens                 (when ens? ens)
                        :customization-color customization-color}
      :container-style {:margin-horizontal 8}
      :on-press        on-press-saved-address}]))

(defn- saved-addresses
  [theme]
  (let [group-saved-addresses (rf/sub [:wallet/grouped-saved-addresses])
        section-header        (rn/use-callback
                               (fn [{:keys [title index]}]
                                 [quo/divider-label
                                  {:tight?          true
                                   :container-style (when (pos? index) {:margin-top 8})}
                                  title]))
        empty-state-component (rn/use-memo
                               (fn []
                                 [quo/empty-state
                                  {:title           (i18n/label :t/no-saved-addresses)
                                   :description     (i18n/label
                                                     :t/you-like-to-type-43-characters)
                                   :container-style style/empty-container-style
                                   :image           (resources/get-themed-image :sweating-man
                                                                                theme)}])
                               [theme])]
    [rn/section-list
     {:key-fn                          :title
      :shows-vertical-scroll-indicator false
      :render-section-header-fn        section-header
      :sections                        group-saved-addresses
      :render-fn                       saved-address
      :content-container-style         {:flex 1}
      :empty-component                 empty-state-component}]))

(defn view
  [{:keys [selected-tab]}]
  (let [theme (quo.theme/use-theme)]
    (case selected-tab
      :tab/recent      [recent-transactions theme]
      :tab/saved       [saved-addresses theme]
      :tab/contacts    [quo/empty-state
                        {:title           (i18n/label :t/no-contacts)
                         :description     (i18n/label :t/no-contacts-description)
                         :image           (resources/get-themed-image :no-contacts theme)
                         :container-style style/empty-container-style}]
      :tab/my-accounts [my-accounts theme])))
