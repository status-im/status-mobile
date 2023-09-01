(ns status-im2.contexts.quo-preview.drawers.drawer-top 
  (:require [quo2.core :as quo]
             [reagent.core :as reagent]
             [status-im2.common.resources :as resources]
             [status-im2.contexts.quo-preview.preview :as preview]))
  
  (def descriptor
    [{:type    :select
      :key     :type
      :options [{:key :default-keypair}
                {:key :recovery-phrase}
                {:key :private-key}]}
     {:type    :select
      :key     :stored
      :options [{:key :on-device}
                {:key :on-keycard}]}])
  
  (defn view
    []
    (let [state (reagent/atom {:blur? false
                               :title "Title"
                               :type :default-keypair
                              ;;  :description "Some description goes here"
                               :button-icon :i/placeholder
                               :community-name "Coinbase" 
                               :community-logo (resources/mock-images :coinbase)
                               :account-avatar-emoji "🍿" 
                               :account-avatar-customization-color :purple
                               :icon-avatar :i/placeholder
                               :profile-picture (resources/get-mock-image :user-picture-male5)})]
      (fn []
        [preview/preview-container {:state state :descriptor descriptor}
         [quo/drawer-top @state]])))