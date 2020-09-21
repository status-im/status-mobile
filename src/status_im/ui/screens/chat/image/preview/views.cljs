(ns status-im.ui.screens.chat.image.preview.views
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [quo.core :as quo]
            [quo.react-native :as rn]
            [status-im.i18n :as i18n]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.screens.chat.sheets :as sheets]
            [status-im.ui.screens.chat.image.preview.image :refer [pinch-zoom]]
            [quo.components.safe-area :as safe-area]))

(defn preview-image []
  (fn [{{:keys [content] :as message} :message
        visible                       :visible
        on-close                      :on-close
        dimensions                    :dimensions}]
    (let [{screen-width  :width
           screen-height :height} @(re-frame/subscribe [:dimensions/window])
          k                       (if  (>= (first dimensions) (second dimensions))
                                    (/ (first dimensions) screen-width)
                                    (/ (second dimensions) screen-height))]
      [rn/modal {:visible     visible
                 :transparent true}
       [safe-area/consumer
        (fn [insets]
          [react/view {:style {:flex 1}}
           [:> pinch-zoom {:uri           (:image content)
                           :on-close      on-close
                           :screen-width  screen-width
                           :screen-height screen-height
                           :width         (/ (first dimensions) k)
                           :height        (/ (second dimensions) k)}]
           [react/view {:style {:position       "absolute"
                                :left           0
                                :right          0
                                :bottom         0
                                :padding-bottom (:bottom insets)}}
            [react/view {:flex-direction     :row
                         :padding-horizontal 8
                         :justify-content    :space-between
                         :align-items        :center}
             [react/view {:width 64}]
             [quo/button {:on-press   on-close
                          :type       :secondary
                          :text-style {:color colors/white-persist}}
              (i18n/label :t/close)]

             [react/touchable-highlight
              {:on-press #(re-frame/dispatch [:bottom-sheet/show-sheet
                                              {:content (sheets/image-long-press message true)
                                               :height  64}])}
              [icons/icon :main-icons/more {:container-style {:width  24
                                                              :height 24
                                                              :margin 20}
                                            :color           colors/white-persist}]]]]])]])))
