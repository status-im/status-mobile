(ns status-im2.contexts.quo-preview.browser.browser-input
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [utils.re-frame :as rf]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Show Favicon"
    :key   :favicon?
    :type  :boolean}
   {:label "Locked"
    :key   :locked?
    :type  :boolean}
   {:label "Disabled"
    :key   :disabled?
    :type  :boolean}])

(defn cool-preview
  []
  (reagent/with-let [keyboard-shown?        (reagent/atom false)
                     keyboard-show-listener (.addListener rn/keyboard
                                                          "keyboardWillShow"
                                                          #(reset! keyboard-shown? true))
                     keyboard-hide-listener (.addListener rn/keyboard
                                                          "keyboardWillHide"
                                                          #(reset! keyboard-shown? false))
                     {:keys [bottom top]}   (safe-area/get-insets)
                     state                  (reagent/atom {:blur?       false
                                                           :disabled?   false
                                                           :favicon?    false
                                                           :placeholder "Search or enter dapp domain"
                                                           :locked?     false})]
    [rn/keyboard-avoiding-view {:style {:flex 1 :padding-top top}}
     [quo/page-nav
      {:type      :no-title
       :icon-name :i/arrow-left
       :on-press  #(rf/dispatch [:navigate-back])}]

     [rn/flat-list
      {:header                       [preview/customizer state descriptor]
       :key-fn                       str
       :keyboard-should-persist-taps :always
       :style                        {:flex 1}}]
     [rn/view
      [quo/browser-input
       (assoc @state
              :customization-color :blue
              :favicon             (when (:favicon? @state) :i/verified))]
      [rn/view {:style {:height (if-not @keyboard-shown? bottom 0)}}]]]
    (finally
     (.remove keyboard-show-listener)
     (.remove keyboard-hide-listener))))

(defn preview-browser-input
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white colors/neutral-95)
            :flex             1}}
   [cool-preview]])
