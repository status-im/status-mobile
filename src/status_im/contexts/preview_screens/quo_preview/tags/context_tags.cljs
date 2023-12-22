(ns status-im.contexts.preview-screens.quo-preview.tags.context-tags
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def example-pk
  "0x04fcf40c526b09ff9fb22f4a5dbd08490ef9b64af700870f8a0ba2133f4251d5607ed83cd9047b8c2796576bc83fa0de23a13a4dced07654b8ff137fe744047917")

(def avatar-options
  [{:key   nil
    :value "Default avatar"}
   {:key   (resources/mock-images :user-picture-male5)
    :value "User pic male 5"}
   {:key   (resources/mock-images :user-picture-male4)
    :value "User pic male 4"}
   {:key   (resources/mock-images :user-picture-female2)
    :value "User pic female 2"}])

(def size-descriptor
  {:key     :size
   :type    :select
   :options [{:key 24}
             {:key 32}]})

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :default}
              {:key :multiuser}
              {:key :group}
              {:key :channel}
              {:key :community}
              {:key :token}
              {:key :network}
              {:key :multinetwork}
              {:key :account}
              {:key :collectible}
              {:key :address}
              {:key :icon}
              {:key :audio}]}
   {:key  :blur?
    :type :boolean}
   {:key     :state
    :type    :select
    :options [{:key :default}
              {:key :selected}]}
   (preview/customization-color-option)])

(def default-descriptor
  [size-descriptor
   {:key     :profile-picture
    :type    :select
    :options avatar-options}
   {:key  :full-name
    :type :text}])

(def multiuser-descriptor
  (let [users [(resources/mock-images :user-picture-male5)
               (resources/mock-images :user-picture-male4)
               nil
               (resources/mock-images :user-picture-female2)
               nil]]
    [{:key     :users
      :type    :select
      :options (map (fn [idx]
                      {:key   (mapv (fn [picture idx-name]
                                      {:profile-picture     picture
                                       :full-name           (str (inc idx-name))
                                       :customization-color (rand-nth (keys colors/customization))})
                                    (take idx (cycle users))
                                    (range))
                       :value (str idx)})
                    (range 1 10))}]))

(def group-descriptor
  [size-descriptor
   {:key  :group-name
    :type :text}])

(def channel-descriptor
  [size-descriptor
   {:key  :community-name
    :type :text}
   {:key  :channel-name
    :type :text}])

(def community-descriptor
  [size-descriptor
   {:key  :community-name
    :type :text}])

(def token-descriptor
  [size-descriptor
   {:key  :amount
    :type :text}
   {:key     :token
    :type    :select
    :options [{:key "ETH"}
              {:key "SNT"}
              {:key "DAI"}]}])

(def network-descriptor
  [size-descriptor
   {:key  :network-name
    :type :text}])

(def multinetwork-descriptor
  (let [networks (cycle [(resources/mock-images :monkey) (resources/mock-images :diamond)])]
    [{:key     :networks
      :type    :select
      :options (map (fn [size]
                      {:key   (take size networks)
                       :value (str size)})
                    (range 1 10))}]))

(def account-descriptor
  [size-descriptor
   {:key  :account-name
    :type :text}
   {:key     :emoji
    :type    :select
    :options [{:key "🐷" :value "🐷"}
              {:key "🍇" :value "🍇"}
              {:key "🐱" :value "🐱"}]}])

(def collectible-descriptor
  [size-descriptor
   {:key  :collectible-name
    :type :text}
   {:key  :collectible-number
    :type :text}])

(def address-descriptor
  [size-descriptor])

(def icon-descriptor
  [size-descriptor
   {:key  :context
    :type :text}
   {:key     :icon
    :type    :select
    :options [{:key :i/placeholder :value "Placeholder"}
              {:key :i/add :value "Add"}
              {:key :i/alert :value "Alert"}]}])

(def audio-descriptor
  [{:key  :duration
    :type :text}])

(defn f-view
  [state type]
  (rn/use-effect (fn []
                   (when (#{:multiuser :multinetwork :audio} @type)
                     (swap! state assoc :size 24)))
                 [@type])
  [preview/preview-container
   {:state                 state
    :descriptor            (concat descriptor
                                   (case (:type @state)
                                     :default      default-descriptor
                                     :multiuser    multiuser-descriptor
                                     :group        group-descriptor
                                     :channel      channel-descriptor
                                     :community    community-descriptor
                                     :token        token-descriptor
                                     :network      network-descriptor
                                     :multinetwork multinetwork-descriptor
                                     :account      account-descriptor
                                     :collectible  collectible-descriptor
                                     :address      address-descriptor
                                     :icon         icon-descriptor
                                     :audio        audio-descriptor
                                     default-descriptor))
    :blur-height           80
    :blur?                 (:blur? @state)
    :show-blur-background? true}
   [rn/view {:style {:align-items :center}}
    [quo/context-tag @state]]])

(defn view
  []
  (let [state
        (reagent/atom
         {:size                32
          :type                :group
          :blur?               false
          :state               :selected
          :customization-color :army
          :profile-picture     nil
          :full-name           "Full Name"
          :users               [{:profile-picture     (resources/mock-images :user-picture-male5)
                                 :full-name           "1"
                                 :customization-color (rand-nth (keys colors/customization))}
                                {:profile-picture     nil
                                 :full-name           "3"
                                 :customization-color (rand-nth (keys colors/customization))}
                                {:profile-picture     (resources/mock-images :user-picture-male5)
                                 :full-name           "2"
                                 :customization-color (rand-nth (keys colors/customization))}]
          :group-name          "Group"
          :community-logo      (resources/mock-images :coinbase)
          :community-name      "Community"
          :channel-name        "my channel"
          :token               "ETH"
          :amount              "10"
          :network-logo        (resources/mock-images :monkey)
          :network-name        "Network"
          :networks            [(resources/mock-images :monkey)
                                (resources/mock-images :diamond)]
          :account-name        "Account name"
          :emoji               "😝"
          :collectible         (resources/mock-images :collectible)
          :collectible-name    "Collectible"
          :collectible-number  "123"
          :address             example-pk
          :icon                :i/placeholder
          :context             "Context"
          :duration            "00:32"})
        type (reagent/cursor state [:type])]
    [:f> f-view state type]))
