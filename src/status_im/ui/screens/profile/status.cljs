(ns status-im.ui.screens.profile.status
  (:require [status-im.ui.screens.chat.message.message :as message]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.datetime :as datetime]
            [status-im.ui.screens.chat.message.gap :as gap]
            [status-im.constants :as constants]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.icons.vector-icons :as icons]))

(defonce messages-list-ref (atom nil))

(defn message-content-image [_ _]
  (let [dimensions (reagent/atom [260 260])]
    (fn [uri show-close?]
      (react/image-get-size
       uri
       (fn [width height]
         (let [k (/ (max width height) 260)]
           (reset! dimensions [(/ width k) (/ height k)]))))
      [react/view
       [react/view {:style {:width         (first @dimensions)
                            :height        (last @dimensions)
                            :border-width  1
                            :border-color  colors/black-transparent
                            :overflow      :hidden
                            :border-radius 16
                            :margin-top    8}}
        [react/image {:style       {:width (first @dimensions) :height (last @dimensions)}
                      :resize-mode :contain
                      :source      {:uri uri}}]]
       (when show-close?
         [react/touchable-highlight {:on-press            #(re-frame/dispatch [:chat.ui/cancel-sending-image])
                                     :accessibility-label :cancel-send-image
                                     :style               {:left (- (first @dimensions) 28) :top 12 :position :absolute}}
          [react/view {:width            24
                       :height           24
                       :background-color colors/black-persist
                       :border-radius    12
                       :align-items      :center
                       :justify-content  :center}
           [icons/icon :main-icons/close-circle {:color colors/white-persist}]]])])))

(defn image-message [{:keys [content] :as message}]
  [react/touchable-highlight {:on-press (fn [_]
                                          (when (:image content)
                                            (re-frame/dispatch [:navigate-to :image-preview
                                                                (assoc message :cant-be-replied true)]))
                                          (react/dismiss-keyboard!))}
   [message-content-image (:image content) false]])

(defn message-item [{:keys [content-type content from last-in-group? timestamp] :as message}]
  [react/view (merge {:padding-top 16 :padding-horizontal 16}
                     (when last-in-group?
                       {:padding-bottom      16
                        :border-bottom-width 1
                        :border-bottom-color colors/gray-lighter}))
   [react/view {:flex-direction :row :justify-content :space-between}
    [message/message-author-name from]
    [react/text {:style {:font-size 10 :color colors/gray}} (datetime/time-ago (datetime/to-date timestamp))]]
   (if (= content-type constants/content-type-image)
     [image-message message]
     [message/render-parsed-text (assoc message :outgoing false) (:parsed-text content)])])

(defn render-message [{:keys [type] :as message} idx]
  (if (= type :datemark)
    [react/view]
    (if (= type :gap)
      [gap/gap message idx messages-list-ref]
      ; message content
      [message-item message])))