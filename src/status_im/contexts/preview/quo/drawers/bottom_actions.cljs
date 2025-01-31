(ns status-im.contexts.preview.quo.drawers.bottom-actions
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.preview.quo.preview :as preview]))

(def button-two "Cancel")
(def button-one "Request to join")
(def description "Joining the community will reveal your public addresses to the node owner")
(def button-options
  [{:key :primary}
   {:key :grey}
   {:key :danger}
   {:key :positive}])
(defn button-press
  [id]
  #(js/alert (str "Button " id " Pressed")))

(def descriptor
  [{:type    :select
    :key     :actions
    :options [{:key :one-action}
              {:key :two-actions}]}
   {:key  :button-one-label
    :type :text}
   {:label   "Button 1 type"
    :type    :select
    :key     :type
    :options button-options
    :path    [:button-one-props]}
   {:label "Button 1 disabled?"
    :type  :boolean
    :key   :disabled?
    :path  [:button-one-props]}
   {:key  :button-two-label
    :type :text}
   {:label   "Button 2 type"
    :type    :select
    :key     :type
    :options button-options
    :path    [:button-two-props]}
   {:label "Button 2 disabled?"
    :type  :boolean
    :key   :disabled?
    :path  [:button-two-props]}
   {:key  :blur?
    :type :boolean}
   {:key  :scroll?
    :type :boolean}
   {:key     :description
    :type    :select
    :options [{:key   nil
               :value :none}
              {:key :top}
              {:key :bottom}
              {:key :top-error}]}])

(def role-descriptor
  {:key     :role
   :type    :select
   :options [{:key   nil
              :value :none}
             {:key :owner}
             {:key :token-master}
             {:key :admin}
             {:key :member}]})

(def description-top-descriptor
  {:key  :description-top-text
   :type :text})

(def description-descriptor
  {:key  :description-text
   :type :text})

(def error-descriptor
  {:key  :error-message
   :type :text})

(defn view
  []
  (let [[state set-state] (rn/use-state
                           {:actions              :two-actions
                            :description          :bottom
                            :description-text     description
                            :description-top-text "Eligible to join as"
                            :error-message        "Error message"
                            :button-one-label     button-one
                            :button-two-label     button-two
                            :button-one-props     {:on-press (button-press 1)
                                                   :type     :primary}
                            :button-two-props     {:on-press (button-press 2)
                                                   :type     :grey}
                            :blur?                false
                            :role                 nil
                            :context-tag-props    {:size   24
                                                   :type   :token
                                                   :token  "USDT"
                                                   :amount "99.97"}
                            :scroll?              false})]
    [preview/preview-container
     {:state                     state
      :set-state                 set-state
      :descriptor                (cond-> descriptor
                                   (= (:description state) :top)
                                   (conj role-descriptor description-top-descriptor)

                                   (= (:description state) :bottom)
                                   (conj description-descriptor)

                                   (= (:description state) :top-error)
                                   (conj error-descriptor))
      :blur?                     (:blur? state)
      :show-blur-background?     true
      :blur-dark-only?           true
      :component-container-style {:margin-top         40
                                  :padding-horizontal 0}}
     [quo/bottom-actions state]]))
