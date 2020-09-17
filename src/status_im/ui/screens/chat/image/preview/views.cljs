(ns status-im.ui.screens.chat.image.preview.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [quo.core :as quo]
            [status-im.i18n :as i18n]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.screens.chat.sheets :as sheets]
            [status-im.ui.screens.chat.image.preview.image :refer [pinch-zoom]]
            [quo.components.safe-area :as safe-area]))

(defn preview-image []
  (let [dimensions (reagent/atom nil)]
    (fn []
      (let [{:keys [content] :as message} @(re-frame/subscribe [:get-screen-params])
            {screen-width  :width
             screen-height :height}       @(re-frame/subscribe [:dimensions/window])]
        (when-not @dimensions
          (react/image-get-size
           (:image content)
           (fn [width height]
             (let [k (/ width screen-width)]
               (reset! dimensions [screen-width (/ height k)])))))
        [safe-area/consumer
         (fn [insets]
           [react/view {:style {:flex 1}}

            [:> pinch-zoom {:source        {:uri (:image content)}
                            :screen-width  screen-width
                            :screen-height screen-height
                            :width         (first @dimensions)
                            :height        (second @dimensions)}]

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
              [quo/button {:on-press   #(re-frame/dispatch [:navigate-back])
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
                                             :color           colors/white-persist}]]]]])]))))
