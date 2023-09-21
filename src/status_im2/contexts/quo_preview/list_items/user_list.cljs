(ns status-im2.contexts.quo-preview.list-items.user-list
  (:require [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [quo2.components.list-items.user-list :as user-list]
            [utils.address :as address]))

(def descriptor
  [{:label "Primary name"
    :key   :primary-name
    :type  :text
    :limit 24}
   {:label "Secondary name"
    :key   :secondary-name
    :type  :text}
   {:label "Chat key"
    :key   :chat-key
    :type  :text}
   {:label "Is contact?"
    :key   :contact?
    :type  :boolean}
   {:label "Is verified?"
    :key   :verified?
    :type  :boolean}
   {:label "Is untrustworthy?"
    :key   :untrustworthy?
    :type  :boolean}
   {:label "Online?"
    :key   :online?
    :type  :boolean}
   {:label   "Accessory:"
    :key     :accessory
    :type    :select
    :options [{:key   {:type :options}
               :value "Options"}
              {:key   {:type :checkbox}
               :value "Checkbox"}
              {:key   {:type :close}
               :value "Close"}]}])

(defn preview-user-list
  []
  (let [state (reagent/atom {:primary-name   "Alisher Yakupov"
                             :short-chat-key (address/get-shortened-compressed-key
                                              "zQ3ssgRy5TtB47MMiMKMKaGyaawkCgMqqbrnAUYrZJ1sgt5N")
                             :ens-verified   true
                             :contact?       false
                             :verified?      false
                             :untrustworthy? false
                             :online?        false})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view {:padding-bottom 150}
        [rn/view
         {:padding-vertical    60
          :padding--horizontal 15
          :justify-content     :center}
         [user-list/user-list @state]]]])))
