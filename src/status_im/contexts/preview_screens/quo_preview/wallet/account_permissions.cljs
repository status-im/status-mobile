(ns status-im.contexts.preview-screens.quo-preview.wallet.account-permissions
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def ^:private descriptor
  [{:key  :name
    :type :text}
   {:key  :address
    :type :text}
   {:key  :emoji
    :type :text}
   (preview/customization-color-option {:key :account-color})
   {:key  :keycard?
    :type :boolean}
   {:key  :checked?
    :type :boolean}
   {:key  :disabled?
    :type :boolean}
   {:key     :token-details
    :type    :select
    :options [{:key :no-tokens}
              {:key :empty-token-list}
              {:key :1-token}
              {:key :3-tokens}
              {:key :5-tokens}]}])

(def ^:private token-details
  {:no-tokens        nil
   :empty-token-list []
   :1-token          [{:token  "SNT"
                       :amount "100"}]
   :3-tokens         [{:token  "SNT"
                       :amount "100"}
                      {:token  "ETH"
                       :amount "18"}
                      {:token  "BTM"
                       :amount "1000"}]
   :5-tokens         [{:token  "SNT"
                       :amount "100"}
                      {:token  "ETH"
                       :amount "18"}
                      {:token  "BTM"
                       :amount "1000"}
                      {:token  "CFI"
                       :amount "1"}
                      {:token  "CK"
                       :amount "18"}]})

(defn view
  []
  (let [state (reagent/atom {:name          "Trip to Vegas"
                             :address       "0x2f0fbf0a93c5999e9b4410848403a02b38983eb2"
                             :emoji         "😊"
                             :account-color :blue
                             :token-details :no-tokens
                             :keycard?      true
                             :checked?      true
                             :disabled?     false})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/account-permissions
        {:account       {:name                (:name @state)
                         :address             (:address @state)
                         :emoji               (:emoji @state)
                         :customization-color (:account-color @state)}
         :token-details (token-details (:token-details @state))
         :keycard?      (:keycard? @state)
         :checked?      (:checked? @state)
         :disabled?     (:disabled? @state)
         :on-change     (fn [checked?] (swap! state assoc :checked? checked?))}]])))
