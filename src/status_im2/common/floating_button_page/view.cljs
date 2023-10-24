(ns status-im2.common.floating-button-page.view
  (:require
   [oops.core :as oops]
   [quo.theme :as quo.theme]
   [react-native.core :as rn]
   [react-native.hooks :as hooks]
   [react-native.platform :as platform]
   [reagent.core :as reagent]
   [status-im2.common.floating-button-page.constants :as constants]
   [status-im2.common.floating-button-page.floating-container.view :as floating-container]
   [status-im2.common.floating-button-page.style :as style]))

(defn show-background
  [{:keys [keyboard-shown? available-space content-height]}]
  (when keyboard-shown?
    (< content-height available-space)))

(defn f-view
  [{:keys [blur? button-props button-label button-height] :or
    {button-height constants/button-height}} header page-content button-component]
  (reagent/with-let [theme (quo.theme/use-theme-value)
                     window-height           (:height (rn/get-window))

                     floating-container-height (reagent/atom button-height)
                     scroll-view-height       (reagent/atom 0)
                     content-container-height (reagent/atom 0)
                     show-keyboard?   (reagent/atom false)
                     content-scroll-y (reagent/atom 0)

                     show-listener    (oops/ocall rn/keyboard
                                                  "addListener"
                                                  (if platform/android?
                                                    "keyboardDidShow"
                                                    "keyboardWillShow")
                                                  #(reset! show-keyboard? true))
                     hide-listener    (oops/ocall rn/keyboard
                                                  "addListener"
                                                  (if platform/android?
                                                    "keyboardDidHide"
                                                    "keyboardWillHide")
                                                  #(reset! show-keyboard? false))]
    (let [{:keys [keyboard-shown
                  keyboard-height]} (hooks/use-keyboard)
          show-background? (show-background
                            {:keyboard-shown? keyboard-shown
                             :available-space   (- window-height keyboard-height @floating-container-height)
                             :content-height     (+ @content-scroll-y @content-container-height)})]
      [rn/view {:style style/page-container}
       header
       [rn/scroll-view
        {:on-layout               (fn [event]
                                    (let [height (oops/oget event "nativeEvent.layout.height")]
                                      (reset! scroll-view-height height)
                                      (reset! content-scroll-y 0)))
         :on-scroll               (fn [event]
                                    (let [y (oops/oget event "nativeEvent.contentOffset.y")]
                                      (reset! content-scroll-y y)))
         :scroll-event-throttle   64
         :content-container-style {:flexGrow 1}}
        [rn/view
         {:style {:border-width 1
                  :border-color :red}
          :on-layout (fn [event]
                       (let [height (oops/oget event "nativeEvent.layout.height")]
                         (reset! content-container-height height)))}
         page-content]]
       [rn/keyboard-avoiding-view
        {:style          style/keyboard-view-style
         :pointer-events :box-none}
        [floating-container/view
         {:theme theme
          :blur? blur?
          :child-height button-height
          :floating? keyboard-shown
          :show-background? show-background?
          :on-layout (fn [event]
                       (let [height (oops/oget event "nativeEvent.layout.height")]
                         (prn height "height height height")
                         (reset! floating-container-height height)))}
         [button-component button-props button-label]]]])
    (finally
      (oops/ocall show-listener "remove")
      (oops/ocall hide-listener "remove"))))

(defn view
  [props header page-content button-component]
  [:f> f-view props header page-content button-component])
