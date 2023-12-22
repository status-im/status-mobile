(ns status-im.contexts.preview-screens.quo-preview.wallet.keypair
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def accounts
  [{:account-props {:customization-color :turquoise
                    :size                32
                    :emoji               "\uD83C\uDFB2"
                    :type                :default
                    :name                "Trip to Vegas"
                    :address             "0x0ah...71a"}
    :networks      [{:name :ethereum :short-name "eth"}
                    {:name :optimism :short-name "opt"}]
    :state         :default
    :action        :none}
   {:account-props {:customization-color :purple
                    :size                32
                    :emoji               "\uD83C\uDF7F"
                    :type                :default
                    :name                "My savings"
                    :address             "0x0ah...72b"}
    :networks      [{:name :ethereum :short-name "eth"}
                    {:name :optimism :short-name "opt"}]
    :state         :default
    :action        :none}
   {:account-props {:customization-color :army
                    :size                32
                    :emoji               "\uD83D\uDCC8"
                    :type                :default
                    :name                "Coin vault"
                    :address             "0x0ah...73c"}
    :networks      [{:name :ethereum :short-name "eth"}
                    {:name :optimism :short-name "opt"}]
    :state         :default
    :action        :none}
   {:account-props {:customization-color :orange
                    :size                32
                    :emoji               "\uD83C\uDFF0"
                    :type                :default
                    :name                "Crypto fortress"
                    :address             "0x0ah...74e"}
    :networks      [{:name :ethereum :short-name "eth"}
                    {:name :optimism :short-name "opt"}]
    :state         :default
    :action        :none}
   {:account-props {:customization-color :yellow
                    :size                32
                    :emoji               "\uD83C\uDFDD️"
                    :type                :default
                    :name                "Block treasure"
                    :address             "0x0ah...75f"}
    :networks      [{:name :ethereum :short-name "eth"}
                    {:name :optimism :short-name "opt"}]
    :state         :default
    :action        :none}])

(defn get-accounts
  [blur?]
  (map (fn [account] (assoc account :blur? blur?)) accounts))

(def descriptor
  [{:key     :stored
    :type    :select
    :options [{:key :on-device}
              {:key :on-keycard}]}
   {:key     :action
    :type    :select
    :options [{:key :selector}
              {:key :options}]}
   {:key     :type
    :type    :select
    :options [{:key :default-keypair}
              {:key :other}]}
   (preview/customization-color-option)
   {:key :blur? :type :boolean}])

(def default-details
  {:full-name "John Doe"
   :address   "zQ3...6fBd2"})

(def other-details {:full-name "Metamask"})

(defn view
  []
  (let [state (reagent/atom {:customization-color :blue
                             :type                :default-keypair
                             :stored              :on-device
                             :on-options-press    #(js/alert "Options pressed")
                             :action              :selector
                             :blur?               false})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :blur?                     (:blur? @state)
        :show-blur-background?     true
        :blur-dark-only?           true
        :blur-height               400
        :component-container-style {:padding-vertical 30
                                    :flex-direction   :row
                                    :justify-content  :center}}
       [rn/view {:style {:flex 1}}
        [quo/keypair
         (merge
          @state
          {:details  (if (= (:type @state) :default-keypair) default-details other-details)
           :accounts (get-accounts (:blur? @state))})]]])))
