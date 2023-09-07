(ns status-im2.contexts.quo-preview.messages.system-message
  (:require [quo2.components.messages.system-message :as system-message]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Message Type"
    :key     :type
    :type    :select
    :options [{:value "Message pinned"
               :key   :pinned}
              {:value "Contact request"
               :key   :contact-request}
              {:value "User added"
               :key   :added}
              {:value "User removed"
               :key   :removed}
              {:value "Message deleted"
               :key   :deleted}]}
   {:label   "Action"
    :key     :action
    :type    :select
    :options [{:value "none"
               :key   nil}
              {:value "Undo"
               :key   :undo}]}
   {:label "Pinned By"
    :key   :pinned-by
    :type  :text}
   {:label "Content Text"
    :key   :content-text
    :type  :text}
   {:label "Content Info"
    :key   :content-info
    :type  :text}])

(defn finalize-state
  [state]
  (merge @state
         {:child        (when (= (:type @state) :pinned) [rn/text "Message content"])
          :display-name (:pinned-by @state)}))

(defn preview-system-message
  []
  (let [state (reagent/atom {:type         :pinned
                             :pinned-by    "Steve"
                             :timestamp    "09:41"
                             :content-text "Hello! This is an example of a pinned message!"
                             :content-info "3 photos"})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view {:padding-bottom 150}
        [rn/view
         {:padding-vertical 60 :flex 1}
         [system-message/system-message (finalize-state state)]]]])))
