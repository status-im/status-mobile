(ns status-im2.contexts.quo-preview.notifications.activity-logs
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo.theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.contexts.quo-preview.tags.status-tags :as status-tags]))

(def descriptor
  [{:label "Unread?"
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
              {:value "Positive"
               :key   :positive}]}
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
              {:value "Positive"
               :key   :positive}]}
   {:label "Button 2 label"
    :key   :button-2-label
    :type  :text}
   status-tags/status-tags-options])

(def basic-user-action
  [[quo/context-tag
    {:size      24
     :full-name "Name"
     :blur?     true}]
   "did something here."])

(def complex-user-action
  [[quo/context-tag {:size 24 :full-name "Alice" :blur? true}]
   "from"
   [quo/context-tag {:size 24 :full-name "Mainnet" :blur? true}]
   "to"
   [quo/context-tag {:size 24 :full-name "Optimism" :blur? true}]
   "on"
   [quo/context-tag {:size 24 :full-name "My savings" :blur? true}]])

(def message-with-mention
  (let [common-text-style {:style {:color colors/white}
                           :size  :paragraph-1}]
    {:body [rn/view
            {:flex           1
             :flex-direction :row}
            [quo/text common-text-style "Hello"]
            [quo/text
             {:style {:background-color   colors/primary-50-opa-10
                      :border-radius      6
                      :color              colors/primary-50
                      :margin-horizontal  3
                      :padding-horizontal 3
                      :size               :paragraph-1}}
             "@name"]
            [quo/text common-text-style "! How are you feeling?"]]}))

(def message-with-title
  {:body  "Your favorite color is Turquoise."
   :title "What's my favorite color?"})

(defn preview-activity-logs
  []
  (let [state (reagent/atom {:button-1-label "Decline"
                             :button-1-type  :danger
                             :button-2-label "Accept"
                             :button-2-type  :primary
                             :context        :complex-user-action
                             :icon           :placeholder
                             :message        :with-title
                             :timestamp      "Today 00:00"
                             :title          "Activity Title"
                             :unread?        true
                             :items          []})]
    (fn []
      (let [{:keys [button-1-type
                    button-1-label
                    button-2-type
                    button-2-label
                    status]}
            @state
            props (cond-> @state
                    (and (seq button-1-label)
                         button-1-type)
                    (update :items
                            conj
                            {:type     :button
                             :label    button-1-label
                             :subtype  button-1-type
                             :on-press #(js/alert "Button 1 Clicked")})

                    (and (seq button-2-label)
                         button-2-type)
                    (update :items
                            conj
                            {:type     :button
                             :label    button-2-label
                             :subtype  button-2-type
                             :on-press #(js/alert "Button 2 Clicked")})

                    (= (:message @state) :simple)
                    (assoc :message {:body "The quick brown fox forgot to jump."})

                    (= (:message @state) :with-mention)
                    (assoc :message message-with-mention)

                    (some? status)
                    (update :items
                            conj
                            {:type    :status
                             :subtype status
                             :blur?   true
                             :label   (name status)})

                    (= (:message @state) :with-title)
                    (assoc :message message-with-title)

                    (= (:context @state) :basic-user-action)
                    (assoc :context basic-user-action)

                    (= (:context @state) :complex-user-action)
                    (assoc :context complex-user-action))]
        [preview/preview-container
         {:state      state
          :descriptor descriptor}
         [rn/view
          {:flex    1
           :padding 16}
          [preview/customizer state descriptor]]
         [quo.theme/provider {:theme :dark}
          [rn/view
           {:background-color colors/neutral-90
            :flex-direction   :row
            :justify-content  :center
            :padding-vertical 60}
           [quo/activity-log props]]]]))))
