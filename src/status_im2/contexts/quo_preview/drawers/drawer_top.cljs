(ns status-im2.contexts.quo-preview.drawers.drawer-top
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]
            [utils.re-frame :as rf]
            [react-native.core :as rn]
            [status-im.multiaccounts.core :as multiaccounts]))

(def descriptor
  [{:type    :select
    :key     :type
    :options [{:key :default}
              {:key :default-keypair}
              {:key :account}
              {:key :keypair}
              {:key :info}
              {:key :context-tag}
              {:key :label}]}
   {:type    :select
    :key     :button-icon
    :options [{:key :i/placeholder}
              {:key   nil
               :value "null"}]}
   {:key  :blur?
    :type :boolean}
   {:key  :keycard?
    :type :boolean}
   {:key  :title
    :type :text}
   {:key  :description
    :type :text}
   {:key  :community-name
    :type :text}
   {:key  :label
    :type :text}])

(defn view
  []
  (let [account (rf/sub [:profile/multiaccount])
        state   (reagent/atom
                 {:blur?                false
                  :title                "Title"
                  :type                 :default
                  :label                "Drawer label"
                  :keycard?             true
                  :networks             [:ethereum]
                  :description          "0x62b...0a5"
                  :button-icon          :i/placeholder
                  :community-name       "Coinbase"
                  :community-logo       (resources/mock-images :coinbase)
                  :account-avatar-emoji "🍿"
                  :customization-color  :purple
                  :icon-avatar          :i/placeholder
                  :on-button-press      #(js/alert "on press")
                  :on-button-long-press #(js/alert "on long press")
                  :profile-picture      (multiaccounts/displayed-photo account)})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true}
       [quo/button
        {:container-style {:margin-horizontal 40}
         :on-press        #(rf/dispatch [:show-bottom-sheet
                                         {:content (fn [] [quo/drawer-top @state])
                                          :theme   (:theme @state)}])}
        "See in bottom sheet"]
       [rn/view {:style {:margin-top 20}}
        [quo/drawer-top @state]]])))
