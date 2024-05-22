(ns status-im.contexts.preview.quo.wallet.approval-label
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:type    :select
    :key     :status
    :options [{:key :approve}
              {:key :approving}
              {:key :approved}]}
   {:type :text
    :key  :token-value}
   {:type    :select
    :key     :token-symbol
    :options [{:key "SNT"}
              {:key "DAI"}
              {:key "ETH"}]}
   (preview/customization-color-option)])

(defn view
  []
  (let [[state set-state] (rn/use-state {:status              :approve
                                         :customization-color :blue
                                         :token-value         "100"
                                         :token-symbol        "SNT"})]
    [preview/preview-container
     {:state                     state
      :set-state                 set-state
      :descriptor                descriptor
      :component-container-style {:padding-top 50}}
     [quo/approval-label
      (assoc state
             :button-props
             {:on-press
              #(js/alert "Pressed")})]]))
