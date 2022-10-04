(ns quo2.screens.messages.system-message
  (:require [reagent.core :as reagent]
            [status-im.react-native.resources :as resources]
            [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [quo2.components.messages.system-message :as system-message]
            [quo2.foundations.colors :as colors]))

(def descriptor [{:label   "Message Type"
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

(defn finalize-state [state]
  (merge @state
         {:mentions [{:name  "Alicia Keys"
                      :image (resources/get-mock-image :user-picture-female2)}
                     {:name  "pedro.eth"
                      :image (resources/get-mock-image :user-picture-male4)}]
          :content  {:text     (:content-text @state)
                     :info     (:content-info @state)
                     :mentions {:name  "Alisher"
                                :image (resources/get-mock-image :user-picture-male5)}}}))
(defn preview []
  (let [state (reagent/atom {:type          :deleted
                             :pinned-by     "Steve"
                             :content-text  "Hello! This is an example of a pinned message!"
                             :content-info  "3 photos"
                             :timestamp-str "09:41"})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view {:padding-vertical 60
                  :align-items      :center}
         [system-message/system-message (finalize-state state)]]]])))

(defn preview-system-message []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :header                    [preview]
                  :key-fn                    str
                  :keyboardShouldPersistTaps :always}]])
