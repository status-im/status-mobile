(ns status-im2.contexts.quo-preview.inputs.browser-input
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Show Label"
    :key   :input-label?
    :type  :boolean}
   {:label "Show Favicon"
    :key   :favicon?
    :type  :boolean}
   {:label "Use SSL"
    :key   :use-ssl?
    :type  :boolean}
   {:label "Blur"
    :key   :blur?
    :type  :boolean}
   {:label "Disabled"
    :key   :disabled?
    :type  :boolean}])

(defn cool-preview
  []
  (let [input-ref (atom nil)
        state     (reagent/atom {:blur?        false
                                 :disabled?    false
                                 :favicon?     false
                                 :input-label? false
                                 :on-clear     #(some-> ^js @input-ref
                                                        (.clear))
                                 :placeholder  "Search or enter dapp domain"
                                 :use-ssl?     false})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:style {:padding-bottom 150}}
        [rn/view {:style {:flex 1}}
         [preview/customizer state descriptor]]
        [preview/blur-view
         {:style                 {:align-items     :center
                                  :margin-vertical 20
                                  :width           "100%"}
          :show-blur-background? (:blur? @state)
          :height                150}
         [rn/view {:style {:width "100%"}}
          [quo/browser-input
           (assoc @state
                  :favicon (when (:favicon? @state) :i/verified)
                  :get-ref #(reset! input-ref %)
                  :label   (when (:input-label? @state) "rarible.com"))]]]]])))

(defn preview-browser-input
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white colors/neutral-95)
            :flex             1}}
   [rn/flat-list
    {:header                    [cool-preview]
     :key-fn                    str
     :keyboardShouldPersistTaps :always
     :style                     {:flex 1}}]])
