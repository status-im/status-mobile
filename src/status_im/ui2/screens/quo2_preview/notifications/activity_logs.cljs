(ns status-im.ui2.screens.quo2-preview.notifications.activity-logs
  (:require [status-im.ui2.screens.quo2-preview.preview :as preview]
            [react-native.core :as rn]
            [quo2.components.markdown.text :as text]
            [quo2.components.notifications.activity-logs :as activity-logs]
            [quo2.components.tags.context-tags :as context-tags]
            [quo2.foundations.colors :as colors]
            [status-im.ui2.screens.quo2-preview.tags.status-tags :as status-tags]
            [reagent.core :as reagent]))

(def descriptor [{:label "Unread?"
                  :key   :unread?
                  :type  :boolean}
                 {:label "Replying?"
                  :key   :replying?
                  :type  :boolean}
                 {:label   "Icon"
                  :key     :icon
                  :type    :select
                  :options [{:key   :placeholder
                             :value :placeholder}]}
                 {:label "Title"
                  :key   :title
                  :type  :text}
                 {:label   "Context"
                  :key     :context
                  :type    :select
                  :options [{:key   :basic-user-action
                             :value "Basic user action"}
                            {:key   :complex-user-action
                             :value "Complex user action"}]}
                 {:label   "Message"
                  :key     :message
                  :type    :select
                  :options [{:value "Simple"
                             :key   :simple}
                            {:value "With mention"
                             :key   :with-mention}
                            {:value "With title"
                             :key   :with-title}]}
                 {:label "Timestamp"
                  :key   :timestamp
                  :type  :text}
                 {:label   "Button 1 type"
                  :key     :button-1-type
                  :type    :select
                  :options [{:value "Danger"
                             :key   :danger}
                            {:value "Primary"
                             :key   :primary}
                            {:value "Success"
                             :key   :success}]}
                 {:label "Button 1 label"
                  :key   :button-1-label
                  :type  :text}
                 {:label   "Button 2 type"
                  :key     :button-2-type
                  :type    :select
                  :options [{:value "Danger"
                             :key   :danger}
                            {:value "Primary"
                             :key   :primary}
                            {:value "Success"
                             :key   :success}]}
                 {:label "Button 2 label"
                  :key   :button-2-label
                  :type  :text}
                 status-tags/status-tags-options])

(def basic-user-action
  [[context-tags/group-avatar-tag "Name" {:color          :purple
                                          :override-theme :dark
                                          :size           :small
                                          :style          {:background-color colors/white-opa-10}
                                          :text-style     {:color colors/white}}]
   [rn/text {:style {:color colors/white}} "did something here."]])

(def complex-user-action
  (let [tag-props {:color          :purple
                   :override-theme :dark
                   :size           :small
                   :style          {:background-color colors/white-opa-10}
                   :text-style     {:color colors/white}}]
    [[context-tags/group-avatar-tag "250,000 SNT" tag-props]
     [rn/text {:style {:color colors/white}} "from"]
     [context-tags/group-avatar-tag "Mainnet" tag-props]
     [rn/text {:style {:color colors/white}} "to"]
     [context-tags/group-avatar-tag "Optimism" tag-props]
     [rn/text {:style {:color colors/white}} "on"]
     [context-tags/group-avatar-tag "My savings" tag-props]]))

(def message-with-mention
  (let [common-text-style {:style {:color colors/white}
                           :size  :paragraph-1}]
    {:body [rn/view {:flex           1
                     :flex-direction :row}
            [text/text common-text-style "Hello"]
            [text/text {:style {:background-color   colors/primary-50-opa-10
                                :border-radius      6
                                :color              colors/primary-50
                                :margin-horizontal  3
                                :padding-horizontal 3
                                :size               :paragraph-1}}
             "@name"]
            [text/text common-text-style "! How are you feeling?"]]}))

(def message-with-title
  {:body  "Your favorite color is Turquoise."
   :title "What's my favorite color?"})

(defn preview []
  (let [state (reagent/atom {:button-1-label "Decline"
                             :button-1-type  :danger
                             :button-2-label "Accept"
                             :button-2-type  :primary
                             :context        :complex-user-action
                             :icon           :placeholder
                             :message        :with-title
                             :timestamp      "Today 00:00"
                             :title          "Activity Title"
                             :unread?        true})]
    (fn []
      (let [{:keys [button-1-type
                    button-1-label
                    button-2-type
                    button-2-label]} @state
            props                    (cond-> @state
                                       (and (seq button-1-label)
                                            button-1-type)
                                       (assoc :button-1 {:label button-1-label
                                                         :type  button-1-type})

                                       (and (seq button-2-label)
                                            button-2-type)
                                       (assoc :button-2 {:label button-2-label
                                                         :type  button-2-type})

                                       (= (:message @state) :simple)
                                       (assoc :message {:body "The quick brown fox forgot to jump."})

                                       (= (:message @state) :with-mention)
                                       (assoc :message message-with-mention)

                                       (some? (:status @state))
                                       (update :status (fn [status]
                                                         {:label (name status) :type status}))

                                       (= (:message @state) :with-title)
                                       (assoc :message message-with-title)

                                       (= (:context @state) :basic-user-action)
                                       (assoc :context basic-user-action)

                                       (= (:context @state) :complex-user-action)
                                       (assoc :context complex-user-action))]
        [rn/view {:margin-bottom 50}
         [rn/view {:flex    1
                   :padding 16}
          [preview/customizer state descriptor]]
         [rn/view {:background-color colors/neutral-90
                   :flex-direction   :row
                   :justify-content  :center
                   :padding-vertical 60}
          [activity-logs/activity-log props]]]))))

(defn preview-activity-logs []
  [rn/view {:flex 1}
   [rn/flat-list {:flex                      1
                  :header                    [preview]
                  :key-fn                    str
                  :keyboardShouldPersistTaps :always}]])
