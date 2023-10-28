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

(defn show-button-background?
  [{:keys [keyboard-shown? available-space content-height] :as props}]
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

(defonce scroll-view-ref (atom nil))

(defn f-view
  [{:keys [blur?]} header page-content footer]
  (reagent/with-let [theme                     (quo.theme/use-theme-value)
                     window-height             (:height (rn/get-window))
                     header-height             (reagent/atom 0)
                     footer-height             (reagent/atom 0)
                     floating-container-height (reagent/atom 0)
                     content-container-height  (reagent/atom 0)
                     content-scroll-y          (reagent/atom 0)
                     ;;

                     #_#_show-listener             (oops/ocall rn/keyboard
                                                           "addListener"
                                                           "keyboardDidShow"
                                                           (fn [e]
                                                             (prn e)
                                                             (js/setTimeout
                                                              (fn []
                                                                (println "LAST SCROLL POSITION: " @content-scroll-y)
                                                                (println "FINAL SHOULD BE:" (+ @content-scroll-y @footer-height))

                                                                (some-> @scroll-view-ref
                                                                  (.scrollTo #js {:x        0
                                                                                  :y        (+ @content-scroll-y @footer-height)
                                                                                  :animated true})))
                                                              (+ (oops/oget e "duration") 300))))]
    (let [{:keys [keyboard-shown
                  keyboard-height]} (hooks/use-keyboard)
          show-background? (show-button-background?
                            {:keyboard-shown? keyboard-shown
                             :available-space (- window-height
                                                 keyboard-height
                                                 @floating-container-height
                                                 (safe-area/get-top)
                                                 50)
                             :content-height  (+ @content-scroll-y
                                                 @content-container-height
                                                 @header-height)})]
      [rn/view {:style {:flex 1} ; style/page-container
                }
       [rn/keyboard-avoiding-view
        {:behavior                :position
         :style                   {:background-color :goldenrod
                                   :flex             1}
         :content-container-style {:position :absolute
                                   :top      0
                                   :bottom   0
                                   :left     0
                                   :right    0}}
        [rn/view {:style     {:position     :absolute
                              :top          0
                              :bottom       0
                              :left         0
                              :right        0
                              :flex         1
                              :height       700
                              :border-width 1
                              :border-color :red}
                  :on-layout (fn [event]
                               (let [height (oops/oget event "nativeEvent.layout.height")]
                                 (reset! content-container-height height)))}

         [rn/view
          {:on-layout (fn [event]
                        (let [height (oops/oget event "nativeEvent.layout.height")]
                          (reset! header-height height)))}
          header]

         [rn/scroll-view {:ref                     #(reset! scroll-view-ref %)
                          :on-scroll               (fn [event]
                                                     (let [y (oops/oget event "nativeEvent.contentOffset.y")]
                                                       (prn y)
                                                       (reset! content-scroll-y y)))
                          :scroll-event-throttle   64
                          :content-container-style {:flex-grow        1
                                                    :background-color :grey
                                                    ;:padding-bottom @footer-height
                                                    }}

          page-content]]
        #_[rn/view {:style     {:border-width     1
                                :border-color     :red
                                :background-color :lightblue}
                    :on-layout (fn [event]
                                 (let [height (oops/oget event "nativeEvent.layout.height")]
                                   (reset! content-container-height height)))}
           page-content]


        #_(when keyboard-shown
            [rn/view {:style {:height           100
                              :background-color :orange}}])]


       #_[rn/view {:style     (cond-> {:position :absolute
                                     :left     0
                                     :right    0
                                     :bottom   0})
                 :on-layout (fn [event]
                              (let [height (oops/oget event "nativeEvent.layout.height")]
                                (reset! footer-height height)))}
        [rn/view {:style {:padding-vertical   16
                          :padding-horizontal 20
                          :background-color   (if show-background?
                                                ;"rgba(67, 90, 183, 1)"
                                                "rgba(67, 90, 183, 0.4352)"
                                                "rgba(67, 90, 183, 0.4352)")}}
         footer]]


       #_[floating-container/view
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
          ]

       ])
    (finally
     ;(oops/ocall show-listener "remove")
     )))

(defn view
  [props header page-content button-component]
  [:f> f-view props header page-content button-component])
