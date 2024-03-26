(ns status-im.common.floating-button-page.view
  (:require
    [oops.core :as oops]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.floating-button-page.floating-container.view :as floating-container]
    [status-im.common.floating-button-page.style :as style]))

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

(defn- init-keyboard-listeners
  [{:keys [on-did-show]}]
  (let [keyboard-will-show? (reagent/atom false)
        keyboard-did-show?  (reagent/atom false)
        add-listener        (fn [listener callback]
                              (oops/ocall rn/keyboard "addListener" listener callback))
        will-show-listener  (add-listener "keyboardWillShow"
                                          #(reset! keyboard-will-show? true))
        did-show-listener   (add-listener "keyboardDidShow"
                                          (fn [e]
                                            (reset! keyboard-did-show? true)
                                            (when on-did-show (on-did-show e))))
        will-hide-listener  (add-listener "keyboardWillHide"
                                          #(reset! keyboard-will-show? false))
        did-hide-listener   (add-listener "keyboardDidHide"
                                          #(reset! keyboard-did-show? false))
        remove-listeners    (fn []
                              (doseq [listener [will-show-listener will-hide-listener
                                                did-show-listener did-hide-listener]]
                                (oops/ocall listener "remove")))]
    {:keyboard-will-show? keyboard-will-show?
     :keyboard-did-show?  keyboard-did-show?
     :remove-listeners    remove-listeners}))

(defn view
  [{:keys [header footer customization-color footer-container-padding header-container-style
           gradient-cover?]
    :or   {footer-container-padding (safe-area/get-top)}} &
   children]
  (reagent/with-let [window-height                (:height (rn/get-window))
                     footer-container-height      (reagent/atom 0)
                     header-height                (reagent/atom 0)
                     content-container-height     (reagent/atom 0)
                     content-scroll-y             (reagent/atom 0)
                     keyboard-height              (reagent/atom 0)
                     {:keys [keyboard-will-show?
                             keyboard-did-show?
                             remove-listeners]}   (init-keyboard-listeners
                                                   {:on-did-show
                                                    (fn [e]
                                                      (reset! keyboard-height
                                                        (oops/oget e "endCoordinates.height")))})
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
      [:<>
       (when gradient-cover? [quo/gradient-cover {:customization-color customization-color}])
       [rn/view {:style style/page-container}
        [rn/view
         {:on-layout set-header-height
          :style     header-container-style}
         header]
        [gesture/scroll-view
         {:on-scroll               set-content-y-scroll
          :scroll-event-throttle   64
          :content-container-style {:flex-grow 1}}
         (into [rn/view {:on-layout set-content-container-height}]
               children)]
        [rn/keyboard-avoiding-view
         {:style                    style/keyboard-avoiding-view
          :keyboard-vertical-offset (if platform/ios? footer-container-padding 0)
          :pointer-events           :box-none}
         [floating-container/view
          {:on-layout       set-footer-container-height
           :keyboard-shown? keyboard-shown?
           :blur?           show-background?}
          footer]]]])
    (finally
     (remove-listeners))))
