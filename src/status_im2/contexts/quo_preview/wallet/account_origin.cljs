(ns status-im2.contexts.quo-preview.wallet.account-origin
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
  (let [state (reagent/atom {:type            :default-keypair
                             :stored          :on-keycard
                             :profile-picture (resources/get-mock-image :user-picture-male5)
                             :derivation-path "m / 44’ / 60’ / 0’ / 0’ / 2"
                             :user-name       "Alisher Yakupov"
                             :on-press        #(js/alert "pressed")})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/account-origin @state]])))
