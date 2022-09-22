(ns quo2.screens.info.info-message
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [quo.previews.preview :as preview]
            [quo2.foundations.colors :as colors]
            [quo2.components.info.info-message :as quo2]))

(def descriptor [{:label   "Type:"
                  :key     :type
                  :type    :select
                  :options [{:key   :default
                             :value "Default"}
                            {:key   :success
                             :value "Success"}
                            {:key   :error
                             :value "Error"}]}
                 {:label   "Size:"
                  :key     :size
                  :type    :select
                  :options [{:key   :default
                             :value "Default"}
                            {:key   :tiny
                             :value "Tiny"}]}
                 {:label "Message"
                  :key   :message
                  :type  :text}])

(defn cool-preview []
  (let [state (reagent/atom {:type    :default
                             :size    :default
                             :icon    :main-icons2/placeholder
                             :message "This is a message"})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view {:padding-vertical 60
                  :align-items      :center}
         [quo2/info-message @state (:message @state)]]]])))

(defn preview-info-message []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
