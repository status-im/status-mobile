(ns status-im2.contexts.quo-preview.wallet.account-origin
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.common.resources :as resources]))

(def descriptor
  [{:label   "Type:"
    :type    :select
    :key     :type
    :options [{:key   :default-keypair
               :value "Default Keypair"}
              {:key   :recovery-phrase
               :value "Recovery Phrase"}
              {:key   :private-key
               :value "Private Key"}]}
   {:label   "Stored:"
    :type    :select
    :key     :stored
    :options [{:key   :on-device
               :value "On Device"}
              {:key   :on-keycard
               :value "On Keycard"}]}])

(defn cool-preview
  []
  (let [state (reagent/atom {:type            :default-keypair
                             :stored          :on-keycard
                             :profile-picture (resources/get-mock-image :user-picture-male5)
                             :derivation-path "m / 44’ / 60’ / 0’ / 0’ / 2"
                             :user-name       "Alisher Yakupov"
                             :on-press        #(js/alert "pressed")})]
    (fn []
      [rn/view
       [rn/view {:style {:flex 1}}
        [preview/customizer state descriptor]]
       [rn/view
        {:style {:padding-vertical  10
                 :margin-horizontal 20}}
        [quo/account-origin @state]]])))

(defn preview
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}}
   [rn/flat-list
    {:style                        {:flex 1}
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
