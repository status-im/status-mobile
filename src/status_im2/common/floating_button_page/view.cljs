(ns status-im2.common.floating-button-page.view
  (:require
    [oops.core :as oops]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.hooks :as hooks]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.common.floating-button-page.floating-container.view :as floating-container]
    [status-im2.common.floating-button-page.style :as style]))

(defn show-background
  [{:keys [keyboard-shown? available-space content-height] :as props}]
  (prn "show-background? "
       (< available-space content-height)
       props)

  (when keyboard-shown?
    (< available-space content-height)))

#_(when keyboard-shown
    (cond
      platform/android?
      (< (- @scroll-view-height button-container-height) @content-container-height)

      platform/ios?
      (< (- @scroll-view-height keyboard-view-height) (- @content-container-height content-scroll-y))

      :else
      false))

(defn f-view
  [{:keys [header footer blur?]}
   page-content]
  (reagent/with-let [theme                     (quo.theme/use-theme-value)
                     window-height             (:height (rn/get-window))

                     floating-container-height (reagent/atom 0)
                     header-height             (reagent/atom 0)
                     content-container-height  (reagent/atom 0)
                     show-keyboard?            (reagent/atom false)
                     content-scroll-y          (reagent/atom 0)

                     show-listener             (oops/ocall rn/keyboard
                                                           "addListener"
                                                           (if platform/android?
                                                             "keyboardDidShow"
                                                             "keyboardWillShow")
                                                           #(reset! show-keyboard? true))
                     hide-listener             (oops/ocall rn/keyboard
                                                           "addListener"
                                                           (if platform/android?
                                                             "keyboardDidHide"
                                                             "keyboardWillHide")
                                                           #(reset! show-keyboard? false))]
    (let [{:keys [keyboard-shown
                  keyboard-height]} (hooks/use-keyboard)
          show-background?          (show-background
                                     {:keyboard-shown? keyboard-shown
                                      :available-space (- window-height
                                                          keyboard-height
                                                          @floating-container-height
                                                          (safe-area/get-top)
                                                          50)
                                      :content-height  (+ @content-scroll-y
                                                          @content-container-height
                                                          @header-height)})]

      [rn/view {:style style/page-container}

       [rn/view
        {:on-layout (fn [event]
                      (let [height (oops/oget event "nativeEvent.layout.height")]
                        (reset! header-height height)))}
        header]
       [rn/scroll-view
        {:on-scroll               (fn [event]
                                    (let [y (oops/oget event "nativeEvent.contentOffset.y")]
                                      (reset! content-scroll-y y)))
         :scroll-event-throttle   64
         :content-container-style {:flexGrow 1}}
        [rn/view
         {:style     {:border-width 1
                      :border-color :red}
          :on-layout (fn [event]
                       (let [height (oops/oget event "nativeEvent.layout.height")]
                         (reset! content-container-height height)))}
         page-content]]

       [rn/keyboard-avoiding-view
        {:style          (style/keyboard-view-style keyboard-shown)
         :pointer-events :box-none}
        [floating-container/view
         {:theme            theme
          :blur?            blur?
          :floating?        keyboard-shown
          :show-background? show-background?
          :on-layout        (fn [event]
                              (let [height (oops/oget event "nativeEvent.layout.height")]
                                (prn height "height height height")
                                (reset! floating-container-height height)))}
         footer
         ;[ button-props button-label]
        ]]])
    (finally
     (oops/ocall show-listener "remove")
     (oops/ocall hide-listener "remove"))))

(defn view
  [props header page-content button-component]
  [:f> f-view props header page-content button-component])
