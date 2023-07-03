(ns status-im2.contexts.chat.lightbox.bottom-view
  (:require
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.lightbox.style :as style]
    [utils.re-frame :as rf]
    [status-im2.contexts.chat.lightbox.animations :as anim]
    [status-im2.contexts.chat.lightbox.constants :as c]
    [status-im2.contexts.chat.messages.content.text.view :as message-view]
    [status-im2.contexts.chat.lightbox.text-sheet.view :as text-sheet]))

(defn get-small-item-layout
  [_ index]
  #js
          {:length c/small-image-size
           :offset (* (+ c/small-image-size 8) index)
           :index  index})

(defn- f-small-image
  [item index _ {:keys [scroll-index props]}]
  (let [size       (if (= @scroll-index index) c/focused-image-size c/small-image-size)
        size-value (anim/use-val size)
        {:keys [scroll-index-lock? small-list-ref flat-list-ref]}
        props]
    (anim/animate size-value size)
    [rn/touchable-opacity
     {:active-opacity 1
      :on-press       (fn []
                        (rf/dispatch [:chat.ui/zoom-out-signal @scroll-index])
                        (reset! scroll-index-lock? true)
                        (js/setTimeout #(reset! scroll-index-lock? false) 500)
                        (js/setTimeout
                          (fn []
                            (reset! scroll-index index)
                            (.scrollToIndex ^js @small-list-ref
                                            #js {:animated true :index index})
                            (.scrollToIndex ^js @flat-list-ref
                                            #js {:animated true :index index}))
                          (if platform/ios? 50 150))
                        (rf/dispatch [:chat.ui/update-shared-element-id (:message-id item)]))}
     [reanimated/fast-image
      {:source {:uri (:image (:content item))}
       :style  (reanimated/apply-animations-to-style {:width  size-value
                                                      :height size-value}
                                                     {:border-radius 10})}]]))

(defn small-image
  [item index _ render-data]
  [:f> f-small-image item index _ render-data])


(defn bottom-view
  [messages index scroll-index insets animations derived item-width props]
  (let [padding-horizontal (- (/ item-width 2) (/ c/focused-image-size 2))]
    [reanimated/linear-gradient
     {:colors [colors/neutral-100-opa-100 colors/neutral-100-opa-50]
      :start  {:x 0 :y 1}
      :end    {:x 0 :y 0}
      :style  (style/gradient-container insets animations derived)}
     [text-sheet/view messages animations]
     [rn/flat-list
      {:ref                               #(reset! (:small-list-ref props) %)
       :key-fn                            :message-id
       :style                             {:height c/small-list-height}
       :data                              messages
       :render-fn                         small-image
       :render-data                       {:scroll-index scroll-index
                                           :props        props}
       :horizontal                        true
       :shows-horizontal-scroll-indicator false
       :get-item-layout                   get-small-item-layout
       :separator                         [rn/view {:style {:width 8}}]
       :initial-scroll-index              index
       :content-container-style           (style/content-container padding-horizontal)}]
     [rn/view {:style {:height (:bottom insets)
                       :position :absolute
                       :bottom 0
                       :left 0
                       :right 0}}]]))
