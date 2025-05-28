(ns status-im.contexts.browser.components.request-accounts-sheet
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn on-approve
  [request address]
  (rf/dispatch [:browser.rpc/approve-request-accounts request address])
  (rf/dispatch [:hide-bottom-sheet]))

(defn on-reject
  [request]
  (rf/dispatch [:browser.rpc/reject-request-accounts request])
  (rf/dispatch [:hide-bottom-sheet]))

(defn view
  [{:keys [request]}]
  (let [{:keys [icon-url name url]}             request
        accounts                                (rf/sub [:wallet/operable-accounts])
        addresses                               (map :address accounts)
        [selected-address set-selected-address] (rn/use-state (first addresses))]
    (println :selected selected-address)
    [rn/view {:style {:padding-top 20}}
     [rn/view {:style {:margin-bottom 20}}
      [rn/view
       {:style {:margin-bottom      12
                :padding-horizontal 20}}
       [quo/user-avatar
        {:profile-picture icon-url
         :size            :big
         :full-name       name}]]
      [quo/page-top
       {:title       name
        :description :context-tag
        :context-tag {:type    :icon
                      :size    32
                      :icon    :i/link
                      :context url}}]]
     [rn/view {:style {:padding-horizontal 20}}
      [quo/text
       {:size                :heading-2
        :weight              :semi-bold
        :accessibility-label "select-account-title"}
       (i18n/label :t/select-account)]
      [rn/view {:style {:margin-top 12 :margin-bottom 20}}
       (for [{:keys [address] :as account} accounts]
         ^{:key (str address)}
         [quo/account-item
          {:type          :default
           :state         (if (= address selected-address)
                            :selected
                            :default)
           :account-props account
           :on-press      (fn []
                            (set-selected-address (:address account)))}])]]
     [quo/bottom-actions
      {:actions          :two-actions
       :button-one-label "Approve"
       :button-one-props {:on-press #(on-approve request selected-address)}
       :button-two-label "Reject"
       :button-two-props {:type     :outline
                          :on-press #(on-reject request)}}]]))
