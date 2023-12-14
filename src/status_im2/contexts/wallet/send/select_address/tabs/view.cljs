(ns status-im2.contexts.wallet.send.select-address.tabs.view
  (:require
    [quo.core :as quo]
    [react-native.gesture :as gesture]
    [status-im2.contexts.wallet.send.select-address.tabs.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- render-account-item
  [{:keys [color address] :as account}]
  [quo/account-item
   {:account-props (assoc account :customization-color color)
    :on-press      (fn []
                     (rf/dispatch [:wallet/select-send-account-address address])
                     (rf/dispatch [:navigate-to-within-stack
                                   [:wallet-select-asset :wallet-select-address]]))}])

(def data
  [{:id :tab/recent :label (i18n/label :t/recent) :accessibility-label :recent-tab}
   {:id :tab/saved :label (i18n/label :t/saved) :accessibility-label :saved-tab}
   {:id :tab/contacts :label (i18n/label :t/contacts) :accessibility-label :contacts-tab}
   {:id :tab/my-accounts :label (i18n/label :t/my-accounts) :accessibility-label :my-accounts-tab}])

(defn my-accounts
  []
  (let [other-accounts (rf/sub [:wallet/accounts-without-current-viewing-account])]
    (if (zero? (count other-accounts))
      [quo/empty-state
       {:title           (i18n/label :t/no-other-accounts)
        :description     (i18n/label :t/here-is-a-cat-in-a-box-instead)
        :placeholder?    true
        :container-style style/empty-container-style}]
      [gesture/flat-list
       {:data                            other-accounts
        :render-fn                       render-account-item
        :content-container-style         style/my-accounts-container
        :shows-vertical-scroll-indicator false}])))

(defn view
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
    :tab/my-accounts [my-accounts]))
