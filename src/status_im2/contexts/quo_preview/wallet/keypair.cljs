(ns status-im2.contexts.quo-preview.wallet.keypair
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def accounts
  [{:account-props {:customization-color :turquoise
                    :size                32
                    :emoji               "\uD83C\uDFB2"
                    :type                :default
                    :name                "Trip to Vegas"
                    :address             "0x0ah...71a"}
    :networks      [:ethereum :optimism]
    :state         :default
    :action        :none}
   {:account-props {:customization-color :purple
                    :size                32
                    :emoji               "\uD83C\uDF7F"
                    :type                :default
                    :name                "My savings"
                    :address             "0x0ah...72b"}
    :networks      [:ethereum :optimism]
    :state         :default
    :action        :none}
   {:account-props {:customization-color :army
                    :size                32
                    :emoji               "\uD83D\uDCC8"
                    :type                :default
                    :name                "Coin vault"
                    :address             "0x0ah...73c"}
    :networks      [:ethereum :optimism]
    :state         :default
    :action        :none}
   {:account-props {:customization-color :orange
                    :size                32
                    :emoji               "\uD83C\uDFF0"
                    :type                :default
                    :name                "Crypto fortress"
                    :address             "0x0ah...74e"}
    :networks      [:ethereum :optimism]
    :state         :default
    :action        :none}
   {:account-props {:customization-color :yellow
                    :size                32
                    :emoji               "\uD83C\uDFDD️"
                    :type                :default
                    :name                "Block treasure"
                    :address             "0x0ah...75f"}
    :networks      [:ethereum :optimism]
    :state         :default
    :action        :none}])

(def descriptor
  [{:label   "Stored:"
    :key     :stored
    :type    :select
    :options [{:key   :on-device
               :value "On device"}
              {:key   :on-keycard
               :value "On Keycard"}]}
   {:label   "Action:"
    :key     :action
    :type    :select
    :options [{:key   :selector
               :value "Selector"}
              {:key   :options
               :value "Options"}]}
   {:label   "Type:"
    :key     :type
    :type    :select
    :options [{:key   :default-keypair
               :value "Default keypair"}
              {:key   :other
               :value "Other"}]}
   (preview/customization-color-option)])

(def default-details
  {:full-name "John Doe"
   :address   "zQ3...6fBd2"})

(def other-details {:full-name "Metamask"})

(defn preview
  []
  (let [state (reagent/atom {:accounts            accounts
                             :customization-color :blue
                             :type                :default-keypair
                             :stored              :on-device
                             :on-options-press    #(js/alert "Options pressed")
                             :action              :selector})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view
        {:style {:padding-vertical 30
                 :flex-direction   :row
                 :justify-content  :center}}
        [quo/keypair
         (merge
          @state
          {:details (if (= (:type @state) :default-keypair) default-details other-details)})]]])))
