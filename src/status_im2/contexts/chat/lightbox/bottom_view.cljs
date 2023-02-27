(ns status-im2.contexts.chat.lightbox.bottom-view
  (:require
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.lightbox.style :as style]
    [utils.re-frame :as rf]
    [status-im2.contexts.chat.lightbox.common :as common]))

(def small-image-size 40)

(def focused-image-size 56)

(def small-list-height 80)

(defn get-small-item-layout
  [_ index]
  #js
   {:length small-image-size
    :offset (* (+ small-image-size 8) index)
    :index  index})

(defn small-image
  [item index _ {:keys [scroll-index]}]
  [:f>
   (fn []
     (let [size       (if (= @scroll-index index) focused-image-size small-image-size)
           size-value (common/use-val size)]
       (common/set-val-timing size-value size)
       [rn/touchable-opacity
        {:active-opacity 1
         :on-press       (fn []
                           (rf/dispatch [:chat.ui/zoom-out-signal @scroll-index])
                           (reset! common/scroll-index-lock? true)
                           (js/setTimeout #(reset! common/scroll-index-lock? false) 500)
                           (js/setTimeout
                            (fn []
                              (reset! scroll-index index)
                              (.scrollToIndex ^js @common/small-list-ref
                                              #js {:animated true :index index})
                              (.scrollToIndex ^js @common/flat-list-ref
                                              #js {:animated true :index index}))
                            (if platform/ios? 50 150))
                           (rf/dispatch [:chat.ui/update-shared-element-id (:message-id item)]))}
        [reanimated/fast-image
         {:source {:uri (:image (:content item))}
          :style  (reanimated/apply-animations-to-style {:width  size-value
                                                         :height size-value}
                                                        {:border-radius 10})}]]))])

(defn bottom-view
  [messages index scroll-index insets animations item-width]
  [:f>
   (fn []
     (let [text               (get-in (first messages) [:content :text])
           padding-horizontal (- (/ item-width 2) (/ focused-image-size 2))]
       [reanimated/linear-gradient
        {:colors [:black :transparent]
         :start  {:x 0 :y 1}
         :end    {:x 0 :y 0}
         :style  (style/gradient-container insets animations)}
        [rn/text
         {:style style/text-style} text]
        [rn/flat-list
         {:ref                     #(reset! common/small-list-ref %)
          :key-fn                  :message-id
          :style                   {:height small-list-height}
          :data                    messages
          :render-fn               small-image
          :render-data             {:scroll-index scroll-index}
          :horizontal              true
          :get-item-layout         get-small-item-layout
          :separator               [rn/view {:style {:width 8}}]
          :initial-scroll-index    index
          :content-container-style (style/content-container padding-horizontal)}]]))])
