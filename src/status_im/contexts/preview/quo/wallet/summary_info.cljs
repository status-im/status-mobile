(ns status-im.contexts.preview.quo.wallet.summary-info
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.preview :as preview]))


(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :status-account}
              {:key :user}
              {:key :saved-account}
              {:key :account}]}])


(defn view
  []
  (let [state                (reagent/atom {:type             :status-account
                                            :networks-to-show {:ethereum {:amount       150
                                                                          :token-symbol "ETH"}
                                                               :optimism {:amount       50
                                                                          :token-symbol "ETH"}
                                                               :arbitrum {:amount       25
                                                                          :token-symbol "ETH"}}})
        status-account-props {:customization-color :purple
                              :size                32
                              :emoji               "🍑"
                              :type                :default
                              :name                "Collectibles vault"
                              :address             "0x0ah...78b"}
        user-props           {:full-name           "M L"
                              :status-indicator?   false
                              :size                :small
                              :ring-background     (resources/get-mock-image :ring)
                              :customization-color :blue
                              :name                "Mark Libot"
                              :address             "0x0ah...78b"
                              :status-account      (merge status-account-props {:size 16})}]
    (fn []
      (let [account-props (if (= (:type @state) :status-account) status-account-props user-props)]
        [preview/preview-container
         {:state                     state
          :descriptor                descriptor
          :component-container-style {:padding-horizontal 20}}
         [quo/summary-info (merge @state {:account-props account-props})]]))))
