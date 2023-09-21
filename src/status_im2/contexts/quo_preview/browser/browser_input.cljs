(ns status-im2.contexts.quo-preview.browser.browser-input
  (:require [quo2.core :as quo]
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

(defn preview-browser-input
  []
  (reagent/with-let [keyboard-shown?        (reagent/atom false)
                     keyboard-show-listener (.addListener rn/keyboard
                                                          "keyboardWillShow"
                                                          #(reset! keyboard-shown? true))
                     keyboard-hide-listener (.addListener rn/keyboard
                                                          "keyboardWillHide"
                                                          #(reset! keyboard-shown? false))
                     {:keys [bottom]}       (safe-area/get-insets)
                     state                  (reagent/atom {:blur?       false
                                                           :disabled?   false
                                                           :favicon?    false
                                                           :placeholder "Search or enter dapp domain"
                                                           :locked?     false})]
    [preview/preview-container
     {:state      state
      :descriptor descriptor}
     [quo/page-nav
      {:type      :no-title
       :icon-name :i/arrow-left
       :on-press  #(rf/dispatch [:navigate-back])}]

     [rn/flat-list
      {:key-fn                       str
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
