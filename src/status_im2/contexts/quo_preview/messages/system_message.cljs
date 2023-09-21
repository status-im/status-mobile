(ns status-im2.contexts.quo-preview.messages.system-message
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Message Content"
    :key   :content
    :type  :text}
   {:label   "Message Type"
    :key     :type
    :type    :select
    :options [{:value "Message pinned"
               :key   :pinned}
              {:key :contact-request}
              {:value "User added"
               :key   :added}
              {:value "User removed"
               :key   :removed}
              {:value "Message deleted"
               :key   :deleted}]}
   {:key :pinned-by :type :text}
   {:key :incoming? :type :boolean}
   (preview/customization-color-option)])

(defn finalize-state
  [state]
  (merge @state
         {:child        (when (= (:type @state) :pinned) [rn/text (:content @state)])
          :display-name (:pinned-by @state)}))

(defn preview-system-message
  []
  (let [state (reagent/atom {:type      :pinned
                             :pinned-by "Steve"
                             :timestamp "09:41"
                             :content   "Hello! This is an example of a content!"})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical 60}}
       [quo/system-message (finalize-state state)]])))
