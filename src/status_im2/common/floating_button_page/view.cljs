(ns status-im2.common.floating-button-page.view
  (:require
    [oops.core :as oops]
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [react-native.hooks :as hooks]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.common.floating-button-page.style :as style]))

(def scroll-view-height (reagent/atom 0))
(def content-container-height (reagent/atom 0))

(defn show-button-background
  [keyboard-height keyboard-shown content-scroll-y]
  (let [button-container-height 64
        keyboard-view-height    (+ keyboard-height button-container-height)]
    (when keyboard-shown
      (cond
        platform/android?
        (< (- @scroll-view-height button-container-height) @content-container-height)

        platform/ios?
        (< (- @scroll-view-height keyboard-view-height) (- @content-container-height content-scroll-y))

        :else
        false))))

(defn button-container
  [{:keys [show-keyboard? keyboard-shown show-background? keyboard-height]} children]
  (let [insets (safe-area/get-insets)
        height (reagent/atom 0)]
    (reset! height (if show-keyboard? (if keyboard-shown keyboard-height 0) 0))
    [rn/view {:style {:margin-top :auto}}
     (cond
       (and (> @height 0) show-background?)
       [blur/view
        (when keyboard-shown
          {:blur-amount      34
           :blur-type        :transparent
           :overlay-color    :transparent
           :background-color (if platform/android? colors/neutral-100 colors/neutral-80-opa-1-blur)
           :style            (style/blur-button-container insets)})
        children]

       (and (> @height 0) (not show-background?))
       [rn/view {:style (style/view-button-container true insets)}
        children]

       (not show-keyboard?)
       [rn/view {:style (style/view-button-container false insets)}
        children])]))

(defn f-view
  [{:keys [button-props button-label]} header children]
  (reagent/with-let [show-keyboard?   (reagent/atom false)
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
          show-background?          (show-button-background keyboard-height
                                                            keyboard-shown
                                                            @content-scroll-y)]

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
         {:on-layout (fn [event]
                       (let [height (oops/oget event "nativeEvent.layout.height")]
                         (reset! content-container-height height)))}
         [rn/view
          children]]]
       [rn/keyboard-avoiding-view
        {:style          style/keyboard-view-style
         :pointer-events :box-none}
        [button-container
         {:show-keyboard?   @show-keyboard?
          :keyboard-shown   keyboard-shown
          :show-background? show-background?
          :keyboard-height  keyboard-height}
         [quo/button button-props button-label]]]])
    (finally
     (oops/ocall show-listener "remove")
     (oops/ocall hide-listener "remove"))))

(defn view
  [props header children]
  [:f> f-view props header children])
