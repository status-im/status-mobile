(ns status-im2.contexts.quo-preview.messages.author
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key :primary-name :type :text :limit 24}
   {:key :secondary-name :type :text}
   {:key :short-chat-key :type :text}
   {:key :time-str :type :text :limit 5}
   {:key :contact? :type :boolean}
   {:key :verified? :type :boolean}
   {:key :muted? :type :boolean}
   {:key :untrustworthy? :type :boolean}
   {:key     :size
    :type    :select
    :options [{:key 13 :value "13"}
              {:key 15 :value "15"}]}])

(defn view
  []
  (let [state (reagent/atom {:primary-name "Alisher Yakupov"
                             :time-str     "09:30"
                             :size         13})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical   50
                                    :padding-horizontal 20}}
       [quo/author @state]])))
