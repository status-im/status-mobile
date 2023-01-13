(ns status-im2.contexts.chat.images-horizontal.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [utils.re-frame :as rf]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [oops.core :as oops]
            [status-im2.contexts.chat.images-horizontal.style :as style]
            [utils.datetime :as datetime]
            [react-native.linear-gradient :as linear-gradient]))

(def flat-list-ref (atom nil))
(def small-list-ref (atom nil))
(def small-image-size 40)

(defn image
  [message]
  (let [shared-element-id (rf/sub [:shared-element-id])
        window-width      (:width (rn/get-window))
        height            (* (:image-height message) (/ window-width (:image-width message)))]
    [fast-image/fast-image
     {:source    {:uri (:image (:content message))}
      :style     {:width         window-width
                  :height        height
                  :border-radius 12}
      :native-ID (when (= shared-element-id (:message-id message)) :shared-element)}]))

(defn get-item-layout
  [_ index]
  (let [window-width (:width (rn/get-window))]
    #js {:length window-width :offset (* window-width index) :index index}))

(defn get-small-item-layout
  [_ index]
  #js {:length small-image-size :offset (* (+ small-image-size 8) index) :index index})

(defn on-viewable-items-changed
  [e]
  (let [changed (-> e (oops/oget :changed) first)]
    (.scrollToIndex ^js @small-list-ref #js {:animated true :index (oops/oget changed :index)})
    (rf/dispatch [:chat.ui/update-shared-element-id (:message-id (oops/oget changed :item))])))

(defn top-view
  [{:keys [from timestamp]} insets]
  (let [display-name (first (rf/sub [:contacts/contact-two-names-by-identity from]))]
    [rn/view
     {:style (style/top-view-container (:top insets))}
     [rn/touchable-opacity
      {:active-opacity 1
       :on-press       #(rf/dispatch [:navigate-back])
       :style          style/close-container}
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
       [quo/icon :options {:size 20 :color colors/white}]]]]))



(defn small-image
  [item]
  [fast-image/fast-image
   {:source {:uri (:image (:content item))}
    :style  {:width         small-image-size
             :height        small-image-size
             :border-radius 10}}])

(defn bottom-view
  [messages insets]
  (let [text               (get-in (first messages) [:content :text])
        padding-horizontal (- (/ (:width (rn/get-window)) 2) (/ small-image-size 2))]
    [linear-gradient/linear-gradient
     {:colors [:black :transparent]
      :start  {:x 0 :y 1}
      :end    {:x 0 :y 0}
      :style  (style/gradient-container insets)}
     [rn/text
      {:style style/text-style} text]
     [rn/flat-list
      {:ref                     #(reset! small-list-ref %)
       :key-fn                  :message-id
       :data                    messages
       :render-fn               small-image
       :horizontal              true
       :get-item-layout         get-small-item-layout
       :separator               [rn/view {:style {:width 8}}]
       :content-container-style {:padding-vertical   12
                                 :padding-horizontal padding-horizontal}}]]))

(defn images-horizontal
  []
  (let [{:keys [messages index]} (rf/sub [:get-screen-params])
        ;; The initial value of data is the image that was pressed (and not the whole album) in order for
        ;; the transition animation to execute properly, otherwise it would animate towards outside the
        ;; screen (even if we have `initialScrollIndex` set).
        data                     (reagent/atom [(nth messages index)])]
    (reset! data messages)
    ;; We use setTimeout to enqueue `scrollToIndex` until the `data` has been updated.
    (js/setTimeout #(do
                      (.scrollToIndex ^js @flat-list-ref #js {:animated false :index index})
                      (.scrollToIndex ^js @small-list-ref #js {:animated false :index index}))
                   0)
    [safe-area/consumer
     (fn [insets]
       [rn/view
        {:style (style/container-view (:top insets))}
        [top-view (first messages) insets]
        [rn/flat-list
         {:ref                       #(reset! flat-list-ref %)
          :key-fn                    :message-id
          :data                      @data
          :render-fn                 image
          :horizontal                true
          :paging-enabled            true
          :get-item-layout           get-item-layout
          :viewability-config        {:view-area-coverage-percent-threshold 50}
          :on-viewable-items-changed on-viewable-items-changed
          :content-container-style   {:justify-content :center
                                      :align-items     :center}}]
        [bottom-view messages insets]])]))
