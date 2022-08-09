(ns quo2.screens.activity-logs
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [quo2.foundations.colors :as colors]
            [quo2.screens.status-tags :as status-tags]
            [quo2.components.activity-logs :as activity-logs]))

(def descriptor [{:label "Unread:"
                  :key   :unread
                  :type  :boolean}
                 {:label "Icon"
                  :key   :icon
                  :type  :select
                  :options [{:key :placeholder
                             :value :placeholder}]}
                 {:label "Title"
                  :key   :title
                  :type  :text}
                 {:label :message
                  :key   :message
                  :type  :text}
                 {:label "Timestamp"
                  :key :timestamp
                  :type :text}
                 {:label "Button 1 type"
                  :key :button-1-type
                  :type :select
                  :options [{:value "Danger"
                             :key :danger}
                            {:value "Primary"
                             :key :primary}
                            {:value "Success"
                             :key :success}]}
                 {:label "Button 1 label"
                  :key :button-1-label
                  :type :text}
                 {:label "Button 2 type"
                  :key :button-2-type
                  :type :select
                  :options [{:value "Danger"
                             :key :danger}
                            {:value "Primary"
                             :key :primary}
                            {:value "Success"
                             :key :success}]}
                 {:label "Button 2 label"
                  :key :button-2-label
                  :type :text}
                 status-tags/status-tags-options])

(defn preview []
  (let [state  (reagent/atom {:title "Activity Title"
                              :timestamp "Yesterday ∙ 10:41"
                              :message "Hello Alisher! Do you remember me from the web 3.0 conference in Porto?"
                              :icon  :placeholder})]
    (fn []
      (let [{:keys [button-1-type
                    button-1-label
                    button-2-type
                    button-2-label]} @state
            props (cond-> @state
                    (and (seq button-1-label)
                         button-1-type)
                    (assoc :button-1 {:label button-1-label
                                      :type button-1-type})

                    (and (seq button-2-label)
                         button-2-type)
                    (assoc :button-2 {:label button-2-label
                                      :type button-2-type}))]
        [rn/view {:margin-bottom 50
                  :padding       16}
         [rn/view {:flex 1}
          [preview/customizer state descriptor]]
         [rn/view {:padding-vertical 60
                   :background-color colors/neutral-95-opa-80
                   :flex-direction   :row
                   :justify-content  :center}
          [activity-logs/activity-logs props]]]))))

(defn preview-activity-logs []
  [rn/view {:flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [preview]
                  :key-fn                    str}]])
