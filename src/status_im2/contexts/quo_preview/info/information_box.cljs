(ns status-im2.contexts.quo-preview.info.information-box
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Type:"
    :key     :type
    :type    :select
    :options [{:key   :default
               :value "Default"}
              {:key   :informative
               :value "Informative"}
              {:key   :error
               :value "Error"}]}
   {:label "Closable?"
    :key   :closable?
    :type  :boolean}
   {:label "Message"
    :key   :message
    :type  :text}
   {:label "Button Label"
    :key   :button-label
    :type  :text}])

(defn cool-preview
  []
  (let [state    (reagent/atom
                  {:type         :default
                   :closable?    true
                   :icon         :i/info
                   :message      (str "If you registered a stateofus.eth name "
                                      "you might be eligible to connect $ENS")
                   :button-label "Button"
                   :style        {:width 335}})
        closed?  (reagent/cursor state [:closed?])
        on-close (fn []
                   (reset! closed? true)
                   (js/setTimeout (fn [] (reset! closed? false))
                                  2000))]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view
        [preview/customizer state descriptor]
        [rn/view
         {:style {:padding-vertical 20
                  :align-items      :center}}
         [quo/information-box (merge @state {:on-close on-close}) (:message @state)]]]])))

(defn preview-information-box
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
