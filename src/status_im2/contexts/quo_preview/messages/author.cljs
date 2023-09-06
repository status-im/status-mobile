(ns status-im2.contexts.quo-preview.messages.author
  (:require [quo2.components.markdown.text :as text]
            [quo2.components.messages.author.view :as quo2]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

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
   {:label "Time"
    :key   :time-str
    :type  :text
    :limit 5}
   {:label "Is contact?"
    :key   :contact?
    :type  :boolean}
   {:label "Is verified?"
    :key   :verified?
    :type  :boolean}
   {:label "Is untrustworthy?"
    :key   :untrustworthy?
    :type  :boolean}])

(defn preview-author
  []
  (let [state (reagent/atom {:primary-name    "Alisher Yakupov"
                             :seconadary-name ""
                             :time-str        "09:30"
                             :contact?        false
                             :verified?       false
                             :untrustworthy?  false})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view {:padding-bottom 150}
        [rn/view
         {:padding-vertical    60
          :padding--horizontal 15
          :justify-content     :center}
         [rn/view
          [text/text "Author:"]
          [quo2/author @state]]]]])))
