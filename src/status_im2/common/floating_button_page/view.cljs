(ns status-im2.common.floating-button-page.view
  (:require
    [oops.core :as oops]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.common.floating-button-page.floating-container.view :as floating-container]
    [status-im2.common.floating-button-page.style :as style]))

(defn- show-background
  [{:keys [window-height keyboard-height footer-container-height content-scroll-y
           content-container-height header-height keyboard-shown?]}]
  (let [available-space    (- window-height
                              (safe-area/get-top)
                              header-height
                              keyboard-height ; Already contains the bottom safe area value
                              footer-container-height)
        scroll-view-height (- content-container-height content-scroll-y)
        overlap?           (< available-space scroll-view-height)]
    (and keyboard-shown? overlap?)))

(defn- set-height-on-layout
  [ratom]
  (fn [event]
    (let [height (oops/oget event "nativeEvent.layout.height")]
      (reset! ratom height))))

(defn view
  [{:keys [header footer]} & children]
  (reagent/with-let [window-height                (:height (rn/get-window))
                     footer-container-height      (reagent/atom 0)
                     header-height                (reagent/atom 0)
                     content-container-height     (reagent/atom 0)
                     content-scroll-y             (reagent/atom 0)
                     keyboard-height              (reagent/atom 0)
                     keyboard-will-show?          (reagent/atom false)
                     keyboard-did-show?           (reagent/atom false)
                     will-show-listener           (oops/ocall rn/keyboard
                                                              "addListener"
                                                              "keyboardWillShow"
                                                              #(reset! keyboard-will-show? true))
                     did-show-listener            (oops/ocall rn/keyboard
                                                              "addListener"
                                                              "keyboardDidShow"
                                                              (fn [e]
                                                                (reset! keyboard-height
                                                                  (oops/oget e "endCoordinates.height"))
                                                                (reset! keyboard-did-show? true)))
                     will-hide-listener           (oops/ocall rn/keyboard
                                                              "addListener"
                                                              "keyboardWillHide"
                                                              #(reset! keyboard-will-show? false))
                     did-hide-listener            (oops/ocall rn/keyboard
                                                              "addListener"
                                                              "keyboardDidHide"
                                                              #(reset! keyboard-did-show? false))
                     set-header-height            (set-height-on-layout header-height)
                     set-content-container-height (set-height-on-layout content-container-height)
                     set-footer-container-height  (set-height-on-layout footer-container-height)
                     set-content-y-scroll         (fn [event]
                                                    (reset! content-scroll-y
                                                      (oops/oget event "nativeEvent.contentOffset.y")))]
    (let [keyboard-shown?  (if platform/ios? @keyboard-will-show? @keyboard-did-show?)
          show-background? (show-background {:window-height            window-height
                                             :footer-container-height  @footer-container-height
                                             :keyboard-height          @keyboard-height
                                             :content-scroll-y         @content-scroll-y
                                             :content-container-height @content-container-height
                                             :header-height            @header-height
                                             :keyboard-shown?          keyboard-shown?})]

      [rn/view {:style style/page-container}
       [rn/view {:on-layout set-header-height}
        header]
       [rn/scroll-view
        {:on-scroll               set-content-y-scroll
         :scroll-event-throttle   64
         :content-container-style {:flex-grow 1}}
        (into [rn/view {:on-layout set-content-container-height}]
              children)]
       [rn/keyboard-avoiding-view
        {:style                    style/keyboard-avoiding-view
         :keyboard-vertical-offset (if platform/ios? (safe-area/get-top) 0)
         :pointer-events           :box-none}
        [floating-container/view
         {:on-layout       set-footer-container-height
          :keyboard-shown? keyboard-shown?
          :blur?           show-background?}
         footer]]])
    (finally
     (doseq [listener [will-show-listener will-hide-listener did-show-listener did-hide-listener]]
       (oops/ocall listener "remove")))))
