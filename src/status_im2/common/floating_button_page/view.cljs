(ns status-im2.common.floating-button-page.view
  (:require
    [oops.core :as oops]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.common.floating-button-page.floating-container.view :as floating-container]
    [status-im2.common.floating-button-page.style :as style]))

(defn- show-background-android
  [{:keys [window-height keyboard-height floating-container-height
           content-scroll-y content-container-height header-height]} keyboard-did-show?]
  (let [available-space (- window-height
                           keyboard-height
                           floating-container-height
                           header-height)
        content-height  (+ content-container-height
                           content-scroll-y)]
    (and keyboard-did-show? (< available-space content-height))))

(defn- show-background-ios
  [{:keys [window-height keyboard-height floating-container-height
           content-scroll-y content-container-height header-height]} keyboard-did-show?]
  (let [available-space (- window-height
                           keyboard-height
                           floating-container-height
                           (safe-area/get-top))
        content-height  (+ (safe-area/get-bottom)
                           header-height
                           content-scroll-y
                           content-container-height)]
    (and keyboard-did-show? (< content-height available-space))))

(defn set-height-on-layout
  [ratom]
  (fn [event]
    (let [height (oops/oget event "nativeEvent.layout.height")]
      (reset! ratom height))))

(defn show-background
  [props keyboard-did-show?]
  (if platform/android?
    (show-background-android props keyboard-did-show?)
    (show-background-ios props keyboard-did-show?)))

(defn view
  [{:keys [header footer]}
   page-content]
  (reagent/with-let [window-height                       (:height (rn/get-window))
                     floating-container-height           (reagent/atom 0)
                     header-height                       (reagent/atom 0)
                     content-container-height            (reagent/atom 0)
                     content-scroll-y                    (reagent/atom 0)
                     keyboard-height                     (reagent/atom 0)
                     keyboard-will-show?                 (reagent/atom false)
                     keyboard-did-show?                  (reagent/atom false)
                     will-show-listener                  (oops/ocall rn/keyboard
                                                                     "addListener"
                                                                     "keyboardWillShow"
                                                                     #(reset! keyboard-will-show? true))
                     did-show-listener                   (oops/ocall
                                                          rn/keyboard
                                                          "addListener"
                                                          "keyboardDidShow"
                                                          (fn [value]
                                                            (reset! keyboard-height
                                                              (oops/oget value "endCoordinates.height"))
                                                            (reset! keyboard-did-show? true)))

                     will-hide-listener                  (oops/ocall rn/keyboard
                                                                     "addListener"
                                                                     "keyboardWillHide"
                                                                     #(reset! keyboard-will-show? false))
                     did-hide-listener                   (oops/ocall rn/keyboard
                                                                     "addListener"
                                                                     "keyboardDidHide"
                                                                     #(reset! keyboard-did-show? false))
                     heider-height-on-layout             (set-height-on-layout header-height)
                     content-container-height-on-layout  (set-height-on-layout content-container-height)
                     floating-container-height-on-layout (set-height-on-layout floating-container-height)
                     content-y-on-scroll                 (fn [event]
                                                           (let [y (oops/oget
                                                                    event
                                                                    "nativeEvent.contentOffset.y")]
                                                             (reset! content-scroll-y y)))]
    (let [show-background? (show-background {:window-height window-height
                                             :floating-container-height
                                             @floating-container-height
                                             :keyboard-height @keyboard-height
                                             :content-scroll-y @content-scroll-y
                                             :content-container-height @content-container-height
                                             :header-height @header-height}
                                            @keyboard-did-show?)]
      [rn/view {:style style/page-container}
       [rn/view
        {:on-layout heider-height-on-layout}
        header]
       [rn/scroll-view
        {:on-scroll               content-y-on-scroll
         :scroll-event-throttle   64
         :content-container-style {:flexGrow 1}}
        [rn/view
         {:on-layout content-container-height-on-layout}
         page-content]]
       [rn/keyboard-avoiding-view
        {:style          style/keyboard-avoiding-view
         :pointer-events :box-none}
        [floating-container/view
         {:keyboard-will-show? @keyboard-will-show?
          :show-background?    show-background?
          :on-layout           floating-container-height-on-layout}
         footer]]])
    (finally
     (oops/ocall will-show-listener "remove")
     (oops/ocall will-hide-listener "remove")
     (oops/ocall did-show-listener "remove")
     (oops/ocall did-hide-listener "remove"))))
