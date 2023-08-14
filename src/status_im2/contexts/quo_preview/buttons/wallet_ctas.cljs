(ns status-im2.contexts.quo-preview.buttons.wallet-ctas
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(defn cool-preview
  []
  (let [state (reagent/atom {:buy-action     #(js/alert "Buy button pressed")
                             :send-action    #(js/alert "Send button pressed")
                             :receive-action #(js/alert "Receive button pressed")
                             :bridge-action  #(js/alert "Bridge button pressed")})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:style {:padding-bottom 150}}
        [rn/view
         {:style {:padding-vertical 60
                  :flex-direction   :row
                  :justify-content  :center}}
         [quo/wallet-ctas @state]]]])))

(defn preview
  []
  [rn/view
   {:style {:flex 1}}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
