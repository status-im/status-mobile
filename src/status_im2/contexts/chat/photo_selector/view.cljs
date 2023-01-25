(ns status-im2.contexts.chat.photo-selector.view
<<<<<<< HEAD
  (:require [utils.i18n :as i18n]
            [quo.components.safe-area :as safe-area]
            [quo2.components.notifications.info-count :as info-count]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.linear-gradient :as linear-gradient]
            [reagent.core :as reagent]
            [status-im2.contexts.chat.photo-selector.style :as style]
            [status-im.utils.core :as utils]
            [utils.re-frame :as rf]))

(def selected (reagent/atom []))
=======
  (:require
   [react-native.platform :as platform]
   [status-im2.constants :as constants]
   [utils.i18n :as i18n]
   [react-native.safe-area :as safe-area]
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
>>>>>>> e9cf18df1... feat: photo & album selector screens

(defn on-press-confirm-selection
  [selected]
  (rf/dispatch [:chat.ui/clear-sending-images])
  (doseq [item @selected]
    (rf/dispatch [:chat.ui/camera-roll-pick item]))
  (reset! selected [])
  (rf/dispatch [:navigate-back]))

(defn bottom-gradient
<<<<<<< HEAD
  [chat-id selected-images]
  [:f>
   (fn []
     (let [safe-area (safe-area/use-safe-area)]
       (when (or (seq @selected) selected-images)
         [linear-gradient/linear-gradient
          {:colors [:black :transparent]
           :start  {:x 0 :y 1}
           :end    {:x 0 :y 0}
           :style  (style/gradient-container safe-area)}
          [quo/button
           {:style               {:align-self        :stretch
                                  :margin-horizontal 20}
            :on-press            (fn []
                                   (rf/dispatch [:bottom-sheet/hide])
                                   (on-press-confirm-selection chat-id))
            :accessibility-label :confirm-selection}
           (i18n/label :t/confirm-selection)]])))])
=======
  [selected-images insets selected]
  (when (or (seq @selected) selected-images)
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
>>>>>>> e9cf18df1... feat: photo & album selector screens

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
  [photos? selected-album]
  [rn/touchable-opacity
   {:style               style/title-container
    :active-opacity      1
    :accessibility-label :album-title
    :on-press            #(rf/dispatch (if photos?
                                         [:open-modal :album-selector]
                                         [:navigate-back]))}
   [quo/text {:weight :medium} selected-album]
   [rn/view {:style (style/chevron-container)}
    [quo/icon (if photos? :i/chevron-down :i/chevron-up)
     {:color (colors/theme-colors colors/neutral-100 colors/white)}]]])

(defn photo-selector
  []
  (let [selected-images (rf/sub [:chats/sending-image])
        selected-album  (or (rf/sub [:camera-roll/selected-album]) (i18n/label :t/recent))
        selected        (reagent/atom [])]
    (rf/dispatch [:chat.ui/camera-roll-get-photos 20 nil selected-album])
    [:f>
     (fn []
       (rn/use-effect-once
        (fn []
          (if selected-images
            (reset! selected (vec (vals selected-images)))
            (reset! selected []))
          js/undefined))
<<<<<<< HEAD
       (let [{window-height :height window-width :width} (rn/use-window-dimensions)
             safe-area                                   (safe-area/use-safe-area)
             camera-roll-photos                          (rf/sub [:camera-roll/photos])
             end-cursor                                  (rf/sub [:camera-roll/end-cursor])
             loading?                                    (rf/sub [:camera-roll/loading-more])
             has-next-page?                              (rf/sub [:camera-roll/has-next-page])]
         [rn/view {:style {:height (- window-height (:top safe-area))}}
          [rn/touchable-opacity
           {:on-press #(js/alert "Camera: not implemented")
            :style    (style/camera-button-container)}
           [quo/icon :i/camera {:color (colors/theme-colors colors/neutral-100 colors/white)}]]
          [rn/view
           {:style style/title-container}
           [quo/text {:weight :medium} (i18n/label :t/recent)]
           [rn/view {:style (style/chevron-container)}
            [quo/icon :i/chevron-down {:color (colors/theme-colors colors/neutral-100 colors/white)}]]]
          [clear-button]
          [rn/flat-list
           {:key-fn                  identity
            :render-fn               image
            :render-data             {:window-width window-width}
            :data                    camera-roll-photos
            :num-columns             3
            :content-container-style {:width          "100%"
                                      :padding-bottom (+ (:bottom safe-area) 100)}
            :style                   {:border-radius 20}
            :on-end-reached          #(rf/dispatch [:camera-roll/on-end-reached end-cursor loading?
                                                    has-next-page?])}]
          [bottom-gradient chat-id selected-images]]))]))
=======
       [safe-area/consumer
        (fn [insets]
          (let [window-width       (:width (rn/get-window))
                camera-roll-photos (rf/sub [:camera-roll/photos])
                end-cursor         (rf/sub [:camera-roll/end-cursor])
                loading?           (rf/sub [:camera-roll/loading-more])
                has-next-page?     (rf/sub [:camera-roll/has-next-page])]
            [rn/view {:style {:flex 1}}
             [rn/view
              {:style style/buttons-container}
              [rn/touchable-opacity
               {:on-press            #(js/alert "Camera: not implemented")
                :style               (style/camera-button-container)
                :accessibility-label :camera-button}
               [quo/icon :i/camera {:color (colors/theme-colors colors/neutral-100 colors/white)}]]
              [album-title true selected-album]
              [clear-button selected]]
             [rn/flat-list
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
             [bottom-gradient selected-images insets selected]]))])]))

>>>>>>> e9cf18df1... feat: photo & album selector screens
