(ns quo2.screens.messages.system-message
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [quo2.foundations.colors :as colors]
            [quo2.components.messages.system-message :as system-message]
            [quo2.screens.messages.resources :as resources]))

(def descriptor [{:label   "Message Type"
                  :key     :type
                  :type    :select
                  :options [{:value "Message pinned"
                             :key   :pinned}
                            {:value "User added"
                             :key   :added}
                            {:value "Message deleted"
                             :key   :deleted}]}
                 {:label   "Message State"
                  :key     :state
                  :type    :select
                  :options [{:value "Default"
                             :key   :default}
                            {:value "Pressed"
                             :key   :pressed}
                            {:value "Landed"
                             :key   :landed}]}
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
                      :image (:alicia-keys resources/images)}
                     {:name  "pedro.eth"
                      :image (:pedro-eth resources/images)}]
          :content  {:text     (:content-text @state)
                     :info     (:content-info @state)
                     :mentions {:name  "Alisher"
                                :image (:alisher resources/images)}}}))

(defn preview []
  (let [state (reagent/atom {:type          :pinned
                             :state         :default
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
