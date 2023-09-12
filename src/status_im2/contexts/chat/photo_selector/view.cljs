(ns status-im2.contexts.chat.photo-selector.view
  (:require
    [react-native.gesture :as gesture]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [react-native.platform :as platform]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]
    [reagent.core :as reagent]
    [status-im2.constants :as constants]
    [status-im2.contexts.chat.photo-selector.style :as style]
    [status-im2.contexts.chat.photo-selector.album-selector.view :as album-selector]
    utils.collection))

(def min-scroll-to-blur 5)

(defn show-toast
  []
  (rf/dispatch [:toasts/upsert
                {:id              :random-id
                 :icon            :i/info
                 :icon-color      colors/danger-50-opa-40
                 :container-style {:top (when platform/ios? 20)}
                 :text            (i18n/label :t/only-6-images)}]))

(defn on-press-confirm-selection
  [selected close]
  (rf/dispatch [:chat.ui/clear-sending-images])
  (doseq [item selected]
    (rf/dispatch [:photo-selector/camera-roll-pick item]))
  (close))

(defn confirm-button
  [selected-images sending-image close]
  (when (not= selected-images sending-image)
    [linear-gradient/linear-gradient
     {:colors [:black :transparent]
      :start  {:x 0 :y 1}
      :end    {:x 0 :y 0}
      :style  (style/gradient-container (safe-area/get-bottom))}
     [quo/button
      {:container-style     {:align-self        :stretch
                             :margin-horizontal 20
                             :margin-top        12}
       :on-press            #(on-press-confirm-selection selected-images close)
       :accessibility-label :confirm-selection}
      (i18n/label :t/confirm-selection)]]))

(defn clear-button
  [album? selected blur-active?]
  (when (and (not album?) (seq @selected))
    [rn/view {:style style/clear-container}
     [quo/button
      {:type                :grey
       :size                32
       :accessibility-label :clear
       :on-press            #(reset! selected [])
       :background          (when blur-active? :photo)}
      (i18n/label :t/clear)]]))

(defn remove-selected
  [coll item]
  (vec (remove #(= (:uri item) (:uri %)) coll)))

(defn render-image
  [item index _ {:keys [window-width selected]}]
  (let [customization-color (rf/sub [:profile/customization-color])
        item-selected?      (some #(= (:uri item) (:uri %)) @selected)]
    [rn/touchable-opacity
     {:on-press            (fn []
                             (if item-selected?
                               (swap! selected remove-selected item)
                               (if (>= (count @selected) constants/max-album-photos)
                                 (show-toast)
                                 (swap! selected conj item))))
      :accessibility-label (str "image-" index)}
     [rn/image
      {:source {:uri (:uri item)}
       :style  (style/image window-width index)}]
     (when item-selected?
       [:<>
        [rn/view {:style (style/overlay window-width)}]
        [quo/counter
         {:container-style     style/image-count
          :customization-color customization-color
          :accessibility-label (str "count-" index)}
         (inc (utils.collection/first-index #(= (:uri item) (:uri %)) @selected))]])]))

(defn photo-selector
  [{:keys [scroll-enabled on-scroll current-scroll close] :as sheet}]
  (rf/dispatch [:photo-selector/get-photos-for-selected-album])
  (rf/dispatch [:photo-selector/camera-roll-get-albums])
  (let [album?          (reagent/atom false)
        sending-image   (into [] (vals (rf/sub [:chats/sending-image])))
        selected-images (reagent/atom sending-image)
        window-width    (:width (rn/get-window))]
    [:f>
     (fn []
       (let [camera-roll-photos (rf/sub [:camera-roll/photos])
             end-cursor         (rf/sub [:camera-roll/end-cursor])
             loading?           (rf/sub [:camera-roll/loading-more])
             has-next-page?     (rf/sub [:camera-roll/has-next-page])
             selected-album     (or (rf/sub [:camera-roll/selected-album]) (i18n/label :t/recent))
             blur-active?       (> @current-scroll min-scroll-to-blur)
             window-height      (:height (rn/get-window))
             top                (reanimated/use-shared-value window-height)]
         [rn/view {:style {:flex 1 :margin-top -20}}
          (when @album?
            [album-selector/album-selector sheet album? selected-album top])
          [:<>
           [gesture/flat-list
            {:key-fn                  identity
             :render-fn               render-image
             :render-data             {:window-width window-width :selected selected-images}
             :data                    camera-roll-photos
             :num-columns             3
             :content-container-style {:width          "100%"
                                       :padding-bottom (+ (safe-area/get-bottom) 100)
                                       :padding-top    64}
             :on-scroll               on-scroll
             :scroll-enabled          @scroll-enabled
             :on-end-reached          (fn []
                                        (when (and (not loading?) has-next-page?)
                                          (rf/dispatch [:photo-selector/camera-roll-loading-more true])
                                          (rf/dispatch [:photo-selector/get-photos-for-selected-album
                                                        end-cursor])))}]
           [confirm-button @selected-images sending-image close]]
          [rn/view {:style style/buttons-container}
           [quo/dropdown
            {:type                      :blurred
             :size                      32
             :on-change                 (fn []
                                          (if-not @album?
                                            (do
                                              (reset! album? true)
                                              (reanimated/animate top 0))
                                            (do
                                              (reanimated/animate top window-height)
                                              (js/setTimeout #(reset! album? false) 300))))
             :selected                  @album?
             :blur-active?              (and (not @album?) blur-active?)
             :override-background-color (when-not @album? :transparent)}
            selected-album]
           [clear-button @album? selected-images blur-active?]]]))]))
