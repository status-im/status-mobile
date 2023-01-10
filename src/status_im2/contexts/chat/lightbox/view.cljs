(ns status-im2.contexts.chat.lightbox.view
  (:require
   [quo2.core :as quo]
   [quo2.foundations.colors :as colors]
   [react-native.core :as rn]
   [react-native.fast-image :as fast-image]
   [react-native.reanimated :as reanimated]
   [utils.re-frame :as rf]
   [react-native.safe-area :as safe-area]
   [reagent.core :as reagent]
   [status-im2.contexts.chat.lightbox.style :as style]
   [utils.datetime :as datetime]
   [react-native.gesture :as gesture]
   [oops.core :refer [oget]]))

(def flat-list-ref (atom nil))
(def small-list-ref (atom nil))
(def small-image-size 40)

(defn toggle-opacity
  [opacity-value border-value transparent?]
  (let [opacity (reanimated/get-shared-value opacity-value)]
    (reanimated/set-shared-value opacity-value (reanimated/with-timing (if (= opacity 1) 0 1)))
    (reanimated/set-shared-value border-value (reanimated/with-timing (if (= opacity 1) 0 12)))
    (reset! transparent? (not @transparent?))))

(defn image
  [message opacity-value border-value transparent?]
  (println "mmm" message)
  [:f>
   (fn []
     (let [shared-element-id (rf/sub [:shared-element-id])
           width             (:width (rn/get-window))
           height            (* (or (:image-height message) 1000)
                                (/ width (or (:image-width message) 1000)))]
       [gesture/tap-gesture-handler
        {:onHandlerStateChange (fn [e]
                                 (when (= (oget e "nativeEvent.state") (:active gesture/states))
                                   (toggle-opacity opacity-value border-value transparent?)))}
        [rn/view {:style {:flex-direction :row}}
         [reanimated/view
          {:style (reanimated/apply-animations-to-style
                   {:border-radius border-value}
                   {:overflow :hidden})}
          [fast-image/fast-image
           {:source    {:uri (:image (:content message))}
            :style     {:width  width
                        :height height}
            :native-ID (when (= shared-element-id (:message-id message)) :shared-element)}]]
         [rn/view {:style {:width 16}}]]]))])


(defn get-item-layout
  [_ index]
  (let [window-width (:width (rn/get-window))]
    #js {:length window-width :offset (* (+ window-width 16) index) :index index}))

(defn get-small-item-layout
  [_ index]
  #js {:length small-image-size :offset (* (+ small-image-size 8) index) :index index})

(defn on-viewable-items-changed
  [e scroll-index]
  (let [changed (-> e (oget :changed) first)
        index   (oget changed :index)]
    (reset! scroll-index index)
    (.scrollToIndex ^js @small-list-ref #js {:animated true :index index})
    (rf/dispatch [:chat.ui/update-shared-element-id (:message-id (oget changed :item))])))

(defn top-view
  [{:keys [from timestamp]} insets opacity-value transparent?]
  [:f>
   (fn []
     (let [display-name (first (rf/sub [:contacts/contact-two-names-by-identity from]))]
       [reanimated/view
        {:style (style/top-view-container (:top insets) opacity-value)}
        [rn/touchable-opacity
         {:on-press #(when-not @transparent? (rf/dispatch [:navigate-back]))
          :style    style/close-container}
         [quo/icon :close {:size 20 :color colors/white}]]
        [rn/view {:style {:margin-left 12}}
         [quo/text
          {:weight :semi-bold
           :size   :paragraph-1
           :style  {:color colors/white}} display-name]
         [quo/text
          {:weight :medium
           :size   :paragraph-2
           :style  {:color colors/neutral-40}} (datetime/to-short-str timestamp)]]
        [rn/view {:style style/top-right-buttons}
         [rn/touchable-opacity
          {:active-opacity 1
           :on-press       #(js/alert "to be implemented")
           :style          (merge style/close-container {:margin-right 12})}
          [quo/icon :share {:size 20 :color colors/white}]]
         [rn/touchable-opacity
          {:active-opacity 1
           :on-press       #(js/alert "to be implemented")
           :style          style/close-container}
          [quo/icon :options {:size 20 :color colors/white}]]]]))])


(defn small-image
  [item index scroll-index]
  [:f>
   (fn []
     (let [size       (if (= @scroll-index index) 56 small-image-size)
           size-value (reanimated/use-shared-value size)]
       (reanimated/set-shared-value size-value (reanimated/with-timing size))
       [rn/touchable-opacity
        {:active-opacity 1
         :on-press       (fn []
                           (reset! scroll-index index)
                           (.scrollToIndex ^js @small-list-ref #js {:animated true :index index})
                           (.scrollToIndex ^js @flat-list-ref #js {:animated true :index index})
                         )}
        [reanimated/fast-image
         {:source {:uri (:image (:content item))}
          :style  (reanimated/apply-animations-to-style {:width  size-value
                                                         :height size-value}
                                                        {:border-radius 10})}]]))])

(defn bottom-view
  [messages index scroll-index insets opacity-value]
  [:f>
   (fn []
     (let [text               (get-in (first messages) [:content :text])
           padding-horizontal (- (/ (:width (rn/get-window)) 2) (/ small-image-size 2))]
       [reanimated/linear-gradient
        {:colors [:black :transparent]
         :start  {:x 0 :y 1}
         :end    {:x 0 :y 0}
         :style  (style/gradient-container insets opacity-value)}
        [rn/text
         {:style style/text-style} text]
        [rn/flat-list
         {:ref                     #(reset! small-list-ref %)
          :key-fn                  :message-id
          :style                   {:height 68}
          :data                    messages
          :render-fn               (fn [item index] [small-image item index scroll-index])
          :horizontal              true
          :get-item-layout         get-small-item-layout
          :separator               [rn/view {:style {:width 8}}]
          :initial-scroll-index    index
          :content-container-style {:padding-vertical   12
                                    :padding-horizontal padding-horizontal
                                    :align-items        :center
                                    :justify-content    :center}}]]))])


(defn lightbox
  []
  [:f>
   (fn []
     (let [{:keys [messages index]} (rf/sub [:get-screen-params])
           ;; The initial value of data is the image that was pressed (and not the whole album) in order
           ;; for
           ;; the transition animation to execute properly, otherwise it would animate towards outside
           ;; the
           ;; screen (even if we have `initialScrollIndex` set).
           data                     (reagent/atom [(nth messages index)])
           scroll-index             (reagent/atom index)
           transparent?             (reagent/atom false)
           opacity-value            (reanimated/use-shared-value 1)
           border-value             (reanimated/use-shared-value 12)
           window-width             (:width (rn/get-window))]
       (reset! data messages)
       [safe-area/consumer
        (fn [insets]
          [:f>
           (fn []
             ;; We use setTimeout to enqueue `scrollToIndex` until the `data` has been updated.
             (js/setTimeout #(.scrollToIndex ^js @flat-list-ref #js {:animated false :index index}) 0)
             [rn/view {:style style/container-view}
              [top-view (first messages) insets opacity-value transparent?]
              [rn/flat-list
               {:ref                       #(reset! flat-list-ref %)
                :key-fn                    :message-id
                :style                     {:width (+ window-width 16)}
                :data                      @data
                :render-fn                 (fn [item] [image item opacity-value border-value
                                                       transparent?])
                :horizontal                true
                :paging-enabled            true
                :get-item-layout           get-item-layout
                :viewability-config        {:view-area-coverage-percent-threshold 50}
                :on-viewable-items-changed (rn/use-callback (fn [e]
                                                              (on-viewable-items-changed e
                                                                                         scroll-index)))
                :content-container-style   {:justify-content :center
                                            :align-items     :center}}]
              [bottom-view messages index scroll-index insets opacity-value]])])]))])

