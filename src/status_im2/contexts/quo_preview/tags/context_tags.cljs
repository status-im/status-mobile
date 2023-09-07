(ns status-im2.contexts.quo-preview.tags.context-tags
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

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
  {:label   "Size"
   :key     :size
   :type    :select
   :options [{:key 24}
             {:key 32}]})

(def descriptor
  [{:label   "Type"
    :key     :type
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
    [{:label   "users"
      :key     :users
      :type    :select
      :options (map (fn [idx]
                      {:key   (mapv (fn [picture idx-name]
                                      {:profile-picture picture
                                       :full-name       (str (inc idx-name))})
                                    (take idx (cycle users))
                                    (range))
                       :value (str idx)})
                    (range 1 10))}]))

(def group-descriptor
  [size-descriptor
   {:label "Group"
    :key   :group-name
    :type  :text}])

(def channel-descriptor
  [size-descriptor
   {:label "Community name"
    :key   :community-name
    :type  :text}
   {:label "Channel name"
    :key   :channel-name
    :type  :text}])

(def community-descriptor
  [size-descriptor
   {:label "Community name"
    :key   :community-name
    :type  :text}])

(def token-descriptor
  [size-descriptor
   {:label "Amount"
    :key   :amount
    :type  :text}
   {:label "Token name"
    :key   :token-name
    :type  :text}])

(def network-descriptor
  [size-descriptor
   {:label "Network name"
    :key   :network-name
    :type  :text}])

(def multinetwork-descriptor
  (let [networks (cycle [(resources/mock-images :monkey) (resources/mock-images :diamond)])]
    [{:label   "Networks"
      :key     :networks
      :type    :select
      :options (map (fn [size]
                      {:key   (take size networks)
                       :value (str size)})
                    (range 1 10))}]))

(def account-descriptor
  [size-descriptor
   {:label "Account name"
    :key   :account-name
    :type  :text}
   {:label   "Emoji"
    :key     :emoji
    :type    :select
    :options [{:key "🐷" :value "🐷"}
              {:key "🍇" :value "🍇"}
              {:key "🐱" :value "🐱"}]}])

(def collectible-descriptor
  [size-descriptor
   {:label "Collectible name"
    :key   :collectible-name
    :type  :text}
   {:label "Collectible number"
    :key   :collectible-number
    :type  :text}])

(def address-descriptor
  [size-descriptor])

(def icon-descriptor
  [size-descriptor
   {:label "Context"
    :key   :context
    :type  :text}
   {:label   "Icon"
    :key     :icon
    :type    :select
    :options [{:key :i/placeholder :value "Placeholder"}
              {:key :i/add :value "Add"}
              {:key :i/alert :value "Alert"}]}])

(def audio-descriptor
  [{:label "Duration"
    :key   :duration
    :type  :text}])

(defn preview-context-tags
  []
  (let [state
        (reagent/atom
         {:label               "Name"
          :size                32
          :type                :group
          :blur?               false
          :state               :selected
          :customization-color :army
          :profile-picture     nil
          :full-name           "Full Name"
          :users               [{:profile-picture (resources/mock-images :user-picture-male5)
                                 :full-name       "1"}
                                {:profile-picture nil
                                 :full-name       "3"}
                                {:profile-picture (resources/mock-images :user-picture-male5)
                                 :full-name       "2"}]
          :group-name          "Group"
          :community-logo      (resources/mock-images :coinbase)
          :community-name      "Community"
          :channel-name        "my channel"
          :token-logo          (resources/mock-images :diamond)
          :token-name          "ETH"
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
          :duration            "00:32"})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor (concat descriptor
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
                              default-descriptor))}
       [rn/view {:style {:padding-bottom 150}}
        [rn/view {:style {:padding-vertical 60}}
         [preview/blur-view
          {:style                 {:flex              1
                                   :margin-vertical   20
                                   :margin-horizontal 40}
           :show-blur-background? (:blur? @state)}
          [quo/context-tag @state]]]]])))
