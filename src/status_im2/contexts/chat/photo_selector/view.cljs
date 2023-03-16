(ns status-im2.contexts.chat.photo-selector.view
  (:require
    [react-native.gesture :as gesture]
    [react-native.navigation :as navigation]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im2.constants :as constants]
    [utils.i18n :as i18n]
    [oops.core :refer [oget]]
    [quo2.components.notifications.info-count :as info-count]
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]
    [reagent.core :as reagent]
    [status-im2.contexts.chat.photo-selector.style :as style]
    [status-im.utils.core :as utils]
    [quo.react]
    [utils.re-frame :as rf]))

(def ^:const drag-threshold 100)

(defn on-press-confirm-selection
  [selected]
  (rf/dispatch [:chat.ui/clear-sending-images])
  (doseq [item @selected]
    (rf/dispatch [:chat.ui/camera-roll-pick item]))
  (reset! selected [])
  (rf/dispatch [:navigate-back]))

(defn bottom-gradient
  [selected-images insets selected]
  (when (or (seq @selected) (seq selected-images))
    [linear-gradient/linear-gradient
     {:colors [:black :transparent]
      :start  {:x 0 :y 1}
      :end    {:x 0 :y 0}
      :style  (style/gradient-container insets)}
     [quo/button
      {:style               {:align-self        :stretch
                             :margin-horizontal 20
                             :margin-top        12}
       :on-press            #(on-press-confirm-selection selected)
       :accessibility-label :confirm-selection}
      (i18n/label :t/confirm-selection)]]))

(defn clear-button
  [selected]
  (when (seq @selected)
    [rn/touchable-opacity
     {:on-press            #(reset! selected [])
      :style               (style/clear-container)
      :accessibility-label :clear}
     [quo/text {:weight :medium} (i18n/label :t/clear)]]))

(defn remove-selected
  [coll item]
  (vec (remove #(= (:uri item) (:uri %)) coll)))

(defn image
  [item index _ {:keys [window-width selected]}]
  [rn/touchable-opacity
   {:active-opacity      1
    :on-press            (fn []
                           (if (some #(= (:uri item) (:uri %)) @selected)
                             (swap! selected remove-selected item)
                             (if (>= (count @selected) constants/max-album-photos)
                               (rf/dispatch [:toasts/upsert
                                             {:id              :random-id
                                              :icon            :info
                                              :icon-color      colors/danger-50-opa-40
                                              :container-style {:top (when platform/ios? 20)}
                                              :text            (i18n/label :t/only-6-images)}])
                               (swap! selected conj item))))
    :accessibility-label (str "image-" index)}
   [rn/image
    {:source {:uri (:uri item)}
     :style  (style/image window-width index)}]
   (when (some #(= (:uri item) (:uri %)) @selected)
     [rn/view {:style (style/overlay window-width)}])
   (when (some #(= (:uri item) (:uri %)) @selected)
     [info-count/info-count
      {:style               style/image-count
       :accessibility-label (str "count-" index)}
      (inc (utils/first-index #(= (:uri item) (:uri %)) @selected))])])

(defn album-title
  [photos? selected-album selected temporary-selected]
  [rn/touchable-opacity
   {:style               (style/title-container)
    :active-opacity      1
    :accessibility-label :album-title
    :on-press            (fn []
                           (if photos?
                             (do
                               (reset! temporary-selected @selected)
                               (rf/dispatch [:open-modal :album-selector]))
                             (rf/dispatch [:navigate-back])))}
   [quo/text
    {:weight          :medium
     :ellipsize-mode  :tail
     :number-of-lines 1
     :style           {:max-width 150}}
    selected-album]
   [rn/view {:style (style/chevron-container)}
    [quo/icon (if photos? :i/chevron-down :i/chevron-up)
     {:color (colors/theme-colors colors/neutral-100 colors/white)}]]])

(defn drag-gesture
  [translate-y opacity]
  (->
    (gesture/gesture-pan)
    (gesture/enabled true)
    (gesture/max-pointers 1)
    (gesture/on-update (fn [e]
                         (let [translation (oget e "translationY")
                               progress    (Math/abs (/ translation drag-threshold))]
                           (reanimated/set-shared-value translate-y translation)
                           (reanimated/set-shared-value opacity (- 1 (/ progress 5)))
                           ;(anim/set-val (if x? pan-x pan-y) translation)
                           ;(anim/set-val opacity (- 1 progress))
                           ;(anim/set-val layout (* progress -20))
                           )))
    (gesture/on-end (fn [e]
                      (if (> (Math/abs (oget e "translationY")) drag-threshold)
                        (do
                          ;(anim/animate background-color "rgba(0,0,0,0)")
                          ;(anim/animate opacity 0)
                          (reanimated/set-shared-value opacity (reanimated/with-timing 0 (clj->js {:duration 100})))
                          (rf/dispatch [:navigate-back])
                          )
                        (do
                          (reanimated/set-shared-value opacity (reanimated/with-timing 1))
                          (reanimated/set-shared-value translate-y (reanimated/with-timing 0))
                          ;#(reset! set-full-height? true)
                          ;(anim/animate (if x? pan-x pan-y) 0)
                          ;(anim/animate opacity 1)
                          ;(anim/animate layout 0)
                          ))))))

(defn photo-selector
  []
  [:f>
   (let [{:keys [insets]} (rf/sub [:get-screen-params])
         temporary-selected (reagent/atom [])] ; used when switching albums
     (fn []
       (let [selected        (reagent/atom []) ; currently selected
             selected-images (rf/sub [:chats/sending-image]) ; already selected and dispatched
             selected-album  (or (rf/sub [:camera-roll/selected-album]) (i18n/label :t/recent))
             opacity        (reanimated/use-shared-value 0)
             translate-y     (reanimated/use-shared-value 0)]
         (rn/use-effect
           (fn []
             (reanimated/animate-shared-value-with-delay-default-easing opacity 1 300 (if platform/ios? 300 100))
             (rf/dispatch [:chat.ui/camera-roll-get-photos 20 nil selected-album])
             (if (seq selected-images)
               (reset! selected (vec (vals selected-images)))
               (reset! selected @temporary-selected)))
           [selected-album])
         (let [window-width       (:width (rn/get-window))
               camera-roll-photos (rf/sub [:camera-roll/photos])
               end-cursor         (rf/sub [:camera-roll/end-cursor])
               loading?           (rf/sub [:camera-roll/loading-more])
               has-next-page?     (rf/sub [:camera-roll/has-next-page])]
           [rn/view {:style {:flex        1
                             :padding-top (if platform/ios? (navigation/status-bar-height) (+ (navigation/status-bar-height) 20))}}
            [reanimated/view {:style (reanimated/apply-animations-to-style {:opacity opacity}
                                                                           {:background-color "rgba(9, 16, 28, 0.7)"
                                                                            :position :absolute
                                                                            :top      0
                                                                            :bottom   0
                                                                            :left     0
                                                                            :right    0})}]
            [gesture/gesture-detector {:gesture (drag-gesture translate-y opacity)}
             [reanimated/view {:style (reanimated/apply-animations-to-style
                                        {:transform [{:translateY translate-y}]}
                                        {:margin-top              0 :background-color :white
                                         :border-top-left-radius  20
                                         :border-top-right-radius 20
                                         :overflow                :hidden})}
              [rn/view
               {:style style/buttons-container}
               (when true
                 [rn/touchable-opacity
                  {:active-opacity 1
                   :on-press       (fn []
                                     (reanimated/set-shared-value opacity (reanimated/with-timing 0 (clj->js {:duration 100})))
                                     (rf/dispatch [:navigate-back]))
                   :style          (style/close-button-container)}
                  [quo/icon :i/close
                   {:size 20 :color (colors/theme-colors colors/black colors/white)}]])
               [album-title true selected-album selected temporary-selected]
               [clear-button selected]]
              [gesture/flat-list
               {:key-fn                  identity
                :render-fn               image
                :render-data             {:window-width window-width :selected selected}
                :data                    camera-roll-photos
                :num-columns             3
                :content-container-style {:width          "100%"
                                          :padding-bottom (+ (:bottom insets) 100)
                                          :padding-top    80}
                :on-end-reached          #(rf/dispatch [:camera-roll/on-end-reached end-cursor
                                                        selected-album loading?
                                                        has-next-page?])}]
              [bottom-gradient selected-images insets selected]]]
            ]))))])
