(ns status-im.contexts.chat.messenger.photo-selector.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.linear-gradient :as linear-gradient]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.constants :as constants]
    [status-im.contexts.chat.messenger.photo-selector.album-selector.view :as album-selector]
    [status-im.contexts.chat.messenger.photo-selector.style :as style]
    utils.collection
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def min-scroll-to-blur 5)

(defn show-photo-limit-toast
  []
  (rf/dispatch [:toasts/upsert
                {:id              :random-id
                 :type            :negative
                 :container-style style/photo-limit-toast-container
                 :text            (i18n/label :t/hit-photos-limit
                                              {:max-photos constants/max-album-photos})}]))

(defn on-press-confirm-selection
  [selected close]
  (rf/dispatch [:chat.ui/clear-sending-images])
  (doseq [item selected]
    (rf/dispatch [:photo-selector/camera-roll-pick item]))
  (close))

(defn confirm-button
  [{:keys [selected-images sending-image close customization-color]}]
  (when (not= selected-images sending-image)
    [linear-gradient/linear-gradient
     {:colors [:black :transparent]
      :start  {:x 0 :y 1}
      :end    {:x 0 :y 0}
      :style  (style/gradient-container (safe-area/get-bottom))}
     [quo/button
      {:customization-color customization-color
       :container-style     {:align-self        :stretch
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
  (let [theme               (quo.theme/use-theme)
        customization-color (rf/sub [:profile/customization-color])
        item-selected?      (some #(= (:uri item) (:uri %)) @selected)]
    [rn/touchable-opacity
     {:on-press                (fn []
                                 (if item-selected?
                                   (swap! selected remove-selected item)
                                   (if (>= (count @selected) constants/max-album-photos)
                                     (show-photo-limit-toast)
                                     (swap! selected conj item))))
      :allow-multiple-presses? true
      :accessibility-label     (str "image-" index)}
     [rn/image
      {:source {:uri (:uri item)}
       :style  (style/image window-width index)}]
     (when item-selected?
       [:<>
        [rn/view {:style (style/overlay window-width theme)}]
        [quo/counter
         {:container-style     style/image-count
          :customization-color customization-color
          :accessibility-label (str "count-" index)}
         (inc (utils.collection/first-index #(= (:uri item) (:uri %)) @selected))]])]))

(defn photo-selector
  [{:keys [scroll-enabled? on-scroll current-scroll close] :as sheet}]
  (let [album?              (reagent/atom false)
        customization-color (rf/sub [:profile/customization-color])
        sending-image       (into [] (vals (rf/sub [:chats/sending-image])))
        selected-images     (reagent/atom sending-image)
        window-width        (:width (rn/get-window))]
    [:f>
     (fn []
       (let [camera-roll-photos  (rf/sub [:camera-roll/photos])
             end-cursor          (rf/sub [:camera-roll/end-cursor])
             loading?            (rf/sub [:camera-roll/loading-more])
             has-next-page?      (rf/sub [:camera-roll/has-next-page])
             selected-album      (or (rf/sub [:camera-roll/selected-album]) (i18n/label :t/recent))
             blur-active?        (> @current-scroll min-scroll-to-blur)
             window-height       (:height (rn/get-window))
             top                 (reanimated/use-shared-value window-height)
             show-blur?          (and (not @album?) blur-active?)
             dropdown-type       (if show-blur? :grey :ghost)
             dropdown-state      (if @album? :active :default)
             dropdown-background (when show-blur? :photo)
             dropdown-on-press   (fn []
                                   (if-not @album?
                                     (do
                                       (reset! album? true)
                                       (reanimated/animate top 0))
                                     (do
                                       (reanimated/animate top window-height)
                                       (js/setTimeout #(reset! album? false) 300))))]
         [rn/view {:style {:flex 1 :margin-top -20}}
          (when @album?
            [album-selector/album-selector sheet album? selected-album top])
          [:<>
           [gesture/flat-list
            {:key-fn                  #(hash (:uri %))
             :render-fn               render-image
             :render-data             {:window-width window-width :selected selected-images}
             :data                    camera-roll-photos
             :num-columns             3
             :content-container-style {:width          "100%"
                                       :padding-bottom (+ (safe-area/get-bottom) 100)
                                       :padding-top    64}
             :on-scroll               on-scroll
             :scroll-enabled          @scroll-enabled?
             :on-end-reached          (fn []
                                        (when (and (not loading?) has-next-page?)
                                          (rf/dispatch [:photo-selector/camera-roll-loading-more true])
                                          (rf/dispatch [:photo-selector/get-photos-for-selected-album
                                                        end-cursor])))}]
           [confirm-button
            {:close               close
             :customization-color customization-color
             :selected-images     @selected-images
             :sending-image       sending-image}]]
          [rn/view {:style style/buttons-container}
           [quo/dropdown
            {:type       dropdown-type
             :size       :size-32
             :state      dropdown-state
             :on-press   dropdown-on-press
             :background dropdown-background}
            selected-album]
           [clear-button @album? selected-images blur-active?]]]))]))
