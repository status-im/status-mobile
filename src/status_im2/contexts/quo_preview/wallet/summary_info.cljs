(ns status-im2.contexts.quo-preview.wallet.summary-info
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.quo-preview.preview :as preview]))


(def descriptor
  [{:label   "Type:"
    :key     :type
    :type    :select
    :options [{:key   :status-account
               :value "Status Account"}
              {:key   :user
               :value "User"}
              {:key   :saved-account
               :value "Saved Account"}
              {:key   :account
               :value "Account"}]}
   {:label "Networks?:"
    :key   :networks?
    :type  :boolean}])


(defn preview
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
         {:state      state
          :descriptor descriptor}
         [rn/view
          {:style {:flex               1
                   :padding-horizontal 20}}
          [quo/summary-info (merge @state {:account-props account-props})]]]))))
