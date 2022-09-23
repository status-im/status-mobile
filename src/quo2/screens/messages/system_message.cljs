(ns quo2.screens.messages.system-message
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo.previews.preview :as preview]
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
                 {:label "Content Text"
                  :key   :content-text
                  :type  :text}
                 {:label "Content Info"
                  :key   :content-info
                  :type  :text}
                 {:label "Timestamp"
                  :key   :timestamp-str
                  :type  :text}
                 {:label "Unread?"
                  :key   :unread?
                  :type  :boolean}])

(defn preview []
  (let [state (reagent/atom
               {:type          :pinned
                :pinned-by     "Steve"
                :mentions      [{:name  "Alicia Keys"
                                 :image (:alicia-keys resources/images)}
                                {:name  "pedro.eth"
                                 :image (:pedro-eth resources/images)}]
                :content       {:text     nil ; <- content-text
                                :info     nil ; <- content-info
                                :mentions {:name  "Alisher"
                                           :image (:alisher resources/images)}}
                :content-text  "Hello! This is an example of a pinned message!"
                :content-info  "3 photos"
                :timestamp-str "09:41"
                :unread?       true})]
    (fn []
      (let [state2 (cond-> @state
                     (:content-text @state)
                     (assoc-in [:content :text] (:content-text @state))
                     (:content-info @state)
                     (assoc-in [:content :info] (:content-info @state)))]
        [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
         [rn/view {:padding-bottom 150}
          [preview/customizer state descriptor]
          [rn/view {:padding-vertical 60}
           [system-message/system-message state2]]]]))))

(defn preview-system-message []
  [rn/view {:flex 1}
   [rn/flat-list {:flex                      1
                  :header                    [preview]
                  :key-fn                    str
                  :keyboardShouldPersistTaps :always}]])


