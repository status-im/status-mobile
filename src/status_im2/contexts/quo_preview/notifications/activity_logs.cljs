(ns status-im2.contexts.quo-preview.notifications.activity-logs
  (:require [quo2.core :as quo2]
            [quo2.foundations.colors :as colors]
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
  [[quo2/user-avatar-tag
    {:color          :purple
     :override-theme :dark
     :size           :small
     :style          {:background-color colors/white-opa-10}
     :text-style     {:color colors/white}}
    "Name"]
   "did something here."])

(def complex-user-action
  (let [tag-props {:color      :purple
                   :size       :small
                   :style      {:background-color colors/white-opa-10}
                   :text-style {:color colors/white}}]
    [[quo2/user-avatar-tag tag-props "Alice"]
     "from"
     [quo2/user-avatar-tag tag-props "Mainnet"]
     "to"
     [quo2/user-avatar-tag tag-props "Optimism"]
     "on"
     [quo2/user-avatar-tag tag-props "My savings"]]))

(def message-with-mention
  (let [common-text-style {:style {:color colors/white}
                           :size  :paragraph-1}]
    {:body [rn/view
            {:flex           1
             :flex-direction :row}
            [quo2/text common-text-style "Hello"]
            [quo2/text
             {:style {:background-color   colors/primary-50-opa-10
                      :border-radius      6
                      :color              colors/primary-50
                      :margin-horizontal  3
                      :padding-horizontal 3
                      :size               :paragraph-1}}
             "@name"]
            [quo2/text common-text-style "! How are you feeling?"]]}))

(def message-with-title
  {:body  "Your favorite color is Turquoise."
   :title "What's my favorite color?"})

(defn preview
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
        [rn/view {:margin-bottom 50}
         [rn/view
          {:flex    1
           :padding 16}
          [preview/customizer state descriptor]]
         [rn/view
          {:background-color colors/neutral-90
           :flex-direction   :row
           :justify-content  :center
           :padding-vertical 60}
          [quo2/activity-log props]]]))))

(defn preview-activity-logs
  []
  [rn/view {:flex 1}
   [rn/flat-list
    {:flex                         1
     :header                       [preview]
     :key-fn                       str
     :keyboard-should-persist-taps :always}]])
