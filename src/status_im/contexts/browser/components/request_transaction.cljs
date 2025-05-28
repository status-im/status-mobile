(ns status-im.contexts.browser.components.request-transaction
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.raw-data-block.view :as raw-data-block]
            [utils.re-frame :as rf]
            [utils.transforms :as transforms]))

(defn on-approve
  [{:keys [tx-args] :as request}]
  ;;(rf/dispatch [:browser.rpc/approve-transaction request tx-hash])
  (rf/dispatch [:browser.rpc/reject-transaction request])
  (rf/dispatch [:hide-bottom-sheet]))

(defn on-reject
  [request]
  (rf/dispatch [:browser.rpc/reject-transaction request])
  (rf/dispatch [:hide-bottom-sheet]))

(defn view
  [{:keys [request]}]
  (let [{:keys [icon-url name url tx-args]} request]
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
       "Sign transaction"]
      [raw-data-block/view
       (-> tx-args
           transforms/json->clj
           (transforms/clj->pretty-json 2))]]
     [quo/bottom-actions
      {:actions          :two-actions
       :button-one-label "Approve"
       :button-one-props {:on-press #(on-approve request)}
       :button-two-label "Reject"
       :button-two-props {:type     :outline
                          :on-press #(on-reject request)}}]]))
