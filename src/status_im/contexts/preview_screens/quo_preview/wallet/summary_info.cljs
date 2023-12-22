(ns status-im.contexts.preview-screens.quo-preview.wallet.summary-info
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))


(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :status-account}
              {:key :user}
              {:key :saved-account}
              {:key :account}]}
   {:key :networks? :type :boolean}])


(defn view
  []
  (let [state                (reagent/atom {:type      :status-account
                                            :networks? true
                                            :values    {:ethereum 150
                                                        :optimism 50
                                                        :arbitrum 25}})
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
