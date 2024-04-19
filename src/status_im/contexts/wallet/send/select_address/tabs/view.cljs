(ns status-im.contexts.wallet.send.select-address.tabs.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im.common.resources :as resources]
    [status-im.contexts.wallet.send.select-address.tabs.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn my-accounts
  [theme]
  (let [other-accounts (rf/sub [:wallet/accounts-without-current-viewing-account])]
    (if (zero? (count other-accounts))
      [quo/empty-state
       {:title           (i18n/label :t/no-other-accounts)
        :description     (i18n/label :t/here-is-a-cat-in-a-box-instead)
        :image           (resources/get-themed-image :cat-in-box theme)
        :container-style style/empty-container-style}]
      (into [rn/view {:style style/my-accounts-container}]
            (map (fn [{:keys [color address] :as account}]
                   [quo/account-item
                    {:account-props (assoc account :customization-color color)
                     :on-press      #(rf/dispatch [:wallet/select-send-address
                                                   {:address   address
                                                    :recipient account
                                                    :stack-id  :screen/wallet.select-address}])}]))
            other-accounts))))

(defn view
  [{:keys [selected-tab]}]
  (let [theme (quo.theme/use-theme)]
    (case selected-tab
      :tab/recent      [quo/empty-state
                        {:title           (i18n/label :t/no-recent-transactions)
                         :description     (i18n/label :t/make-one-it-is-easy-we-promise)
                         :image           (resources/get-themed-image :angry-man theme)
                         :container-style style/empty-container-style}]
      :tab/saved       [quo/empty-state
                        {:title           (i18n/label :t/no-saved-addresses)
                         :description     (i18n/label :t/you-like-to-type-43-characters)
                         :image           (resources/get-themed-image :sweating-man theme)
                         :container-style style/empty-container-style}]
      :tab/contacts    [quo/empty-state
                        {:title           (i18n/label :t/no-contacts)
                         :description     (i18n/label :t/no-contacts-description)
                         :image           (resources/get-themed-image :no-contacts theme)
                         :container-style style/empty-container-style}]
      :tab/my-accounts [my-accounts theme])))
