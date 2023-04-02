(ns status-im2.contexts.chat.photo-selector.view
  (:require
    [react-native.gesture :as gesture]
    [react-native.platform :as platform]
    [status-im2.constants :as constants]
    [utils.i18n :as i18n]
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

(defn on-press-confirm-selection
  [selected]
  (rf/dispatch [:chat.ui/clear-sending-images])
  (doseq [item @selected]
    (rf/dispatch [:chat.ui/camera-roll-pick item]))
  (reset! selected [])
  (rf/dispatch [:navigate-back]))

(defn bottom-gradient
  [selected-images bottom-inset selected]
  (when (or (seq @selected) (seq selected-images))
    [linear-gradient/linear-gradient
     {:colors [:black :transparent]
      :start  {:x 0 :y 1}
      :end    {:x 0 :y 0}
      :style  (style/gradient-container bottom-inset)}
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
  [photos? selected-album]
  (fn []
    [rn/touchable-opacity
     {:style               (style/title-container)
      :active-opacity      1
      :accessibility-label :album-title
      :on-press            (fn []
                             ;; TODO: album-selector issue:
                             ;; https://github.com/status-im/status-mobile/issues/15398
                             (js/alert "currently disabled"))}
     ;(if photos?
     ;  (do
     ;    (reset! temporary-selected @selected)
     ;    (rf/dispatch [:open-modal :album-selector {:insets insets}]))
     ;  (rf/dispatch [:navigate-back]))

     [quo/text
      {:weight          :medium
       :ellipsize-mode  :tail
       :number-of-lines 1
       :style           {:max-width 150}}
      selected-album]
     [rn/view {:style (style/chevron-container)}
      [quo/icon (if photos? :i/chevron-down :i/chevron-up)
       {:color (colors/theme-colors colors/neutral-100 colors/white)}]]]))

(defn photo-selector
  [{:keys [scroll-enabled on-scroll]}]
<<<<<<< HEAD
  [:f>
   (let [{:keys [bottom-inset]} (rf/sub [:screen-params]) ; TODO:
                                                          ; https://github.com/status-im/status-mobile/issues/15535
         temporary-selected     (reagent/atom [])] ; used when switching albums
     (fn []
       (let [selected        (reagent/atom [])     ; currently selected
=======
  [safe-area/consumer
   (fn [insets]
     [:f>
      (let [temporary-selected (reagent/atom [])] ; used when switching albums
        (fn []
          (let [selected        (reagent/atom []) ; currently selected
                selected-images (rf/sub [:chats/sending-image]) ; already selected and dispatched
                selected-album  (or (rf/sub [:camera-roll/selected-album]) (i18n/label :t/recent))]
            (rn/use-effect
<<<<<<< HEAD
             (fn []
               (rf/dispatch [:chat.ui/camera-roll-get-photos 20 nil selected-album])
               (if (seq selected-images)
                 (reset! selected (vec (vals selected-images)))
                 (reset! selected @temporary-selected)))
             [selected-album])
=======
  []
  [:f>
   (let [{:keys [insets]}   (rf/sub [:get-screen-params])
         temporary-selected (reagent/atom [])] ; used when switching albums
     (fn []
       (let [selected        (reagent/atom []) ; currently selected
>>>>>>> 7c3fd5384 (feat: bottom sheet screen)
             selected-images (rf/sub [:chats/sending-image]) ; already selected and dispatched
             selected-album  (or (rf/sub [:camera-roll/selected-album]) (i18n/label :t/recent))]
         (rn/use-effect
<<<<<<< HEAD
          (fn []
            (rf/dispatch [:chat.ui/camera-roll-get-photos 20 nil selected-album])
            (if (seq selected-images)
              (reset! selected (vec (vals selected-images)))
              (reset! selected @temporary-selected)))
          [selected-album])
<<<<<<< HEAD
<<<<<<< HEAD
         [:f>
          (fn []
=======
=======
>>>>>>> 46b746724 (review)
<<<<<<< HEAD
=======
           (fn []
             (rf/dispatch [:chat.ui/camera-roll-get-photos 20 nil selected-album])
             (if (seq selected-images)
               (reset! selected (vec (vals selected-images)))
               (reset! selected @temporary-selected)))
           [selected-album])
>>>>>>> 51f87a6d5 (updates)
         [bottom-sheet-screen/view
          (fn [{:keys [scroll-enabled on-scroll]}]
<<<<<<< HEAD
=======
         [bottom-sheet-screen/consumer
          (fn [close scroll-enabled on-scroll]
>>>>>>> 52b8d487a (feat: bottom sheet screen)
<<<<<<< HEAD
>>>>>>> fb1ba49a5 (feat: bottom sheet screen)
<<<<<<< HEAD
>>>>>>> 7c3fd5384 (feat: bottom sheet screen)
=======
=======
=======
         [bottom-sheet-screen/view
<<<<<<< HEAD
          (fn [{:keys [close scroll-enabled on-scroll]}]
>>>>>>> 18f397b83 (review)
<<<<<<< HEAD
>>>>>>> e1051a659 (review)
<<<<<<< HEAD
>>>>>>> 46b746724 (review)
=======
=======
=======
          (fn [{:keys [scroll-enabled on-scroll]}]
>>>>>>> aaea14c93 (lint)
>>>>>>> 8a983f7e0 (lint)
<<<<<<< HEAD
>>>>>>> c430c406a (lint)
=======
=======
>>>>>>> 13502ac3c (updates)
>>>>>>> 51f87a6d5 (updates)
=======
              (fn []
                (rf/dispatch [:chat.ui/camera-roll-get-photos 20 nil selected-album])
                (if (seq selected-images)
                  (reset! selected (vec (vals selected-images)))
                  (reset! selected @temporary-selected)))
              [selected-album])
>>>>>>> 47bbed1b0 (rebase)
            (let [window-width       (:width (rn/get-window))
                  camera-roll-photos (rf/sub [:camera-roll/photos])
                  end-cursor         (rf/sub [:camera-roll/end-cursor])
                  loading?           (rf/sub [:camera-roll/loading-more])
                  has-next-page?     (rf/sub [:camera-roll/has-next-page])]
              [:<>
               [rn/view
                {:style style/buttons-container}
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
                [album-title true selected-album selected temporary-selected]
=======
=======
>>>>>>> b8eb56525 (remove close button)
<<<<<<< HEAD
=======
                [rn/touchable-opacity
                 {:active-opacity 1
                  :on-press       close
                  :style          (style/close-button-container)}
                 [quo/icon :i/close
                  {:size 20 :color (colors/theme-colors colors/black colors/white)}]]
>>>>>>> 52b8d487a (feat: bottom sheet screen)
=======
>>>>>>> af6996923 (remove close button)
=======
>>>>>>> 51f87a6d5 (updates)
                [album-title true selected-album selected temporary-selected insets]
>>>>>>> 7c3fd5384 (feat: bottom sheet screen)
                [clear-button selected]]
               [gesture/flat-list
                {:key-fn                  identity
                 :render-fn               image
                 :render-data             {:window-width window-width :selected selected}
                 :data                    camera-roll-photos
                 :num-columns             3
                 :content-container-style {:width          "100%"
                                           :padding-bottom (+ (:bottom bottom-inset) 100)
                                           :padding-top    64}
                 :on-scroll               on-scroll
                 :scroll-enabled          scroll-enabled
                 :on-end-reached          #(rf/dispatch [:camera-roll/on-end-reached end-cursor
                                                         selected-album loading?
                                                         has-next-page?])}]
               [bottom-gradient selected-images bottom-inset selected]]))])))])
