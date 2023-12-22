(ns status-im.contexts.preview-screens.quo-preview.wallet.network-amount
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))


(def descriptor
  [{:key :amount :type :text}
   {:key     :token
    :type    :select
    :options [{:key :eth}
              {:key :snt}]}])

(defn view
  []
  (let [state (reagent/atom {:amount "5.123456"
                             :token  :eth})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-horizontal 20}}
       [quo/network-amount @state]])))
