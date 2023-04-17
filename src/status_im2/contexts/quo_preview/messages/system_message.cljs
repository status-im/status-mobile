(ns status-im2.contexts.quo-preview.messages.system-message
  (:require [quo2.components.messages.system-message :as system-message]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Message Type"
    :key     :type
    :type    :select
    :options [{:value "Message pinned"
               :key   :pinned}
              {:value "User added"
               :key   :added}
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
    :type  :text}
   {:label "Timestamp"
    :key   :timestamp-str
    :type  :text}])

(defn finalize-state
  [state]
  (merge @state
         {:mentions [{:name  "Alicia Keys"
                      :image (resources/get-mock-image :user-picture-female2)}
                     {:name  "pedro.eth"
                      :image (resources/get-mock-image :user-picture-male4)}]
          :content  {:text     (:content-text @state)
                     :info     (:content-info @state)
                     :mentions {:name  "Alisher"
                                :image (resources/get-mock-image :user-picture-male5)}}}))
(defn preview
  []
  (let [state (reagent/atom {:type          :deleted
                             :pinned-by     "Steve"
                             :content-text  "Hello! This is an example of a pinned message!"
                             :content-info  "3 photos"
                             :timestamp-str "09:41"
                             :labels        {:pinned-a-message (i18n/label :pinned-a-message)
                                             :message-deleted  (i18n/label :message-deleted)
                                             :added            (i18n/label :added)}})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
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
