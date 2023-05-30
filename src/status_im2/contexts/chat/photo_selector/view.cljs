(ns status-im2.contexts.chat.photo-selector.view
  (:require
    [react-native.gesture :as gesture]
    [react-native.safe-area :as safe-area]
    [react-native.platform :as platform]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [quo2.components.notifications.info-count :as info-count]
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]
    [reagent.core :as reagent]
    [status-im2.constants :as constants]
    [status-im2.contexts.chat.photo-selector.style :as style]
    [status-im2.contexts.chat.photo-selector.album-selector.view :as album-selector]
    utils.collection))

(defn show-toast
  []
  (rf/dispatch [:toasts/upsert
                {:id              :random-id
                 :icon            :info
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
  [selected-images close]
  (when (seq selected-images)
    [linear-gradient/linear-gradient
     {:colors [:black :transparent]
      :start  {:x 0 :y 1}
      :end    {:x 0 :y 0}
      :style  (style/gradient-container (safe-area/get-bottom))}
     [quo/button
      {:style               {:align-self        :stretch
                             :margin-horizontal 20
                             :margin-top        12}
       :on-press            #(on-press-confirm-selection selected-images close)
       :accessibility-label :confirm-selection}
      (i18n/label :t/confirm-selection)]]))

(defn clear-button
  [album? selected]
  (when (and (not album?) (seq @selected))
    [rn/view {:style style/clear-container}
     [quo/button
      {:type                :grey
       :size                32
       :accessibility-label :clear
       :on-press            #(reset! selected [])}
      (i18n/label :t/clear)]]))

(defn remove-selected
  [coll item]
  (vec (remove #(= (:uri item) (:uri %)) coll)))

(defn render-image
  [item index _ {:keys [window-width selected]}]
  (let [item-selected? (some #(= (:uri item) (:uri %)) @selected)]
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
        [info-count/info-count
         {:style               style/image-count
          :accessibility-label (str "count-" index)}
         (inc (utils.collection/first-index #(= (:uri item) (:uri %)) @selected))]])]))

(defn photo-selector
  [{:keys [scroll-enabled on-scroll close] :as sheet}]
  (rf/dispatch [:photo-selector/get-photos-for-selected-album 20])
  (rf/dispatch [:photo-selector/camera-roll-get-albums])
  (let [album?          (reagent/atom false)
        selected-images (reagent/atom (into [] (vals (rf/sub [:chats/sending-image]))))
        window-width    (:width (rn/get-window))]
    (fn []
      (let [camera-roll-photos (rf/sub [:camera-roll/photos])
            end-cursor         (rf/sub [:camera-roll/end-cursor])
            loading?           (rf/sub [:camera-roll/loading-more])
            has-next-page?     (rf/sub [:camera-roll/has-next-page])
            selected-album     (or (rf/sub [:camera-roll/selected-album]) (i18n/label :t/recent))]
        [rn/view {:style {:flex 1 :margin-top -20}}
         [rn/view {:style style/buttons-container}
          [quo/dropdown {:type :grey :size 32 :on-change #(swap! album? not) :selected @album?}
           selected-album]
          [clear-button @album? selected-images]]
         (if @album?
           [album-selector/album-selector sheet album? selected-album]
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
              :on-end-reached          #(when (and (not loading?) has-next-page?)
                                          (rf/dispatch [:photo-selector/camera-roll-loading-more
                                                        true])
                                          (rf/dispatch [:photo-selector/get-photos-for-selected-album 20
                                                        end-cursor]))}]
            [confirm-button @selected-images close]])]))))
