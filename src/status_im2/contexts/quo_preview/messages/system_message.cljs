(ns status-im2.contexts.quo-preview.messages.system-message
  (:require [quo2.components.messages.system-message :as system-message]
            [quo2.foundations.colors :as colors]
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

(defn preview
  []
  (let [state (reagent/atom {:type         :pinned
                             :pinned-by    "Steve"
                             :timestamp    "09:41"
                             :content-text "Hello! This is an example of a pinned message!"
                             :content-info "3 photos"})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60 :flex 1}
         [system-message/system-message (finalize-state state)]]]])))

(defn preview-system-message
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :header                       [preview]
     :key-fn                       str
     :keyboard-should-persist-taps :always}]])
