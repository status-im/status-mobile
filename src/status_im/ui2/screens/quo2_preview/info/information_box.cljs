(ns status-im.ui2.screens.quo2-preview.info.information-box
  (:require [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im.ui2.screens.quo2-preview.preview :as preview]
            [quo2.foundations.colors :as colors]
            [quo2.components.info.information-box :as quo2]))

(def descriptor [{:label   "Type:"
                  :key     :type
                  :type    :select
                  :options [{:key   :default
                             :value "Default"}
                            {:key   :informative
                             :value "Informative"}
                            {:key   :error
                             :value "Error"}]}
                 {:label "Closable?:"
                  :key   :closable?
                  :type  :boolean}
                 {:label "Message"
                  :key   :message
                  :type  :text}
                 {:label "Button Label"
                  :key   :button-label
                  :type  :text}])

(defn cool-preview []
  (let [state    (reagent/atom {:type        :default
                                :closable?    true
                                :icon         :i/placeholder
                                :message      "This is an information box This is an information"
                                :button-label "Press Me"
                                :style         {:width 335}
                                :id            (keyword (str "id-" (rand-int 10000)))})
        closed?  (reagent/cursor state [:closed?])
        on-close #(reset! closed? true)]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view {:padding-vertical 60
                  :align-items      :center}
         [quo2/information-box (merge @state {:on-close on-close}) (:message @state)]]]])))

(defn preview-information-box []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
