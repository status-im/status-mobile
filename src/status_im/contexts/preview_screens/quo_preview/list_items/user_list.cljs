(ns status-im.contexts.preview-screens.quo-preview.list-items.user-list
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]
    [utils.address :as address]))

(def descriptor
  [{:key   :primary-name
    :type  :text
    :limit 24}
   {:key :secondary-name :type :text}
   {:key :contact? :type :boolean}
   {:key :verified? :type :boolean}
   {:key :untrustworthy? :type :boolean}
   {:key :online? :type :boolean}
   {:key     :accessory
    :type    :select
    :options [{:key   {:type :options}
               :value "Options"}
              {:key   {:type :checkbox}
               :value "Checkbox"}
              {:key   {:type :close}
               :value "Close"}]}])

(defn view
  []
  (let [state (reagent/atom {:primary-name   "Alisher Yakupov"
                             :short-chat-key (address/get-shortened-compressed-key
                                              "zQ3ssgRy5TtB47MMiMKMKaGyaawkCgMqqbrnAUYrZJ1sgt5N")
                             :photo-path     (resources/mock-images :user-picture-female2)
                             :ens-verified   true
                             :contact?       false
                             :verified?      false
                             :untrustworthy? false
                             :online?        false})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical   30
                                    :padding-horizontal 15}}
       [quo/user-list @state]])))
