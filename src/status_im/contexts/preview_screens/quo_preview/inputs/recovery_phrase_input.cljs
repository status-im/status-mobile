(ns status-im.contexts.preview-screens.quo-preview.inputs.recovery-phrase-input
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :text
    :type :text}
   {:key  :placeholder
    :type :text}
   {:key  :blur?
    :type :boolean}
   {:key  :mark-errors?
    :type :boolean}
   (preview/customization-color-option)
   {:key     :word-limit
    :type    :select
    :options [{:key nil :value "No limit"}
              {:key 5 :value "5 words"}
              {:key 10 :value "10 words"}
              {:key 20 :value "20 words"}]}])

(defn view
  []
  (let [state (reagent/atom {:text                ""
                             :placeholder         "Type or paste your recovery phrase"
                             :customization-color :blue
                             :word-limit          20
                             :blur?               false
                             :mark-errors?        true})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true}
       [quo/text {:size :paragraph-2}
        "(Any word with at least 6 chars is marked as error)"]
       [rn/view {:style {:height 150}}
        [quo/recovery-phrase-input
         {:mark-errors?        (:mark-errors? @state)
          :error-pred          #(> (count %) 5)
          :on-change-text      #(swap! state assoc :text %)
          :placeholder         (:placeholder @state)
          :customization-color (:customization-color @state)
          :word-limit          (:word-limit @state)}
         (:text @state)]]])))
