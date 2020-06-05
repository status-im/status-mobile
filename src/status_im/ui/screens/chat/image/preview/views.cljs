(ns status-im.ui.screens.chat.image.preview.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.ui.components.button :as button]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.screens.chat.sheets :as sheets]))

(defview preview-image []
  (letsubs [{:keys [content] :as message} [:get-screen-params]
            {:keys [width height]} [:dimensions/window]]
    [react/view {:flex 1 :background-color colors/black-persist}
     [react/safe-area-view {:style {:flex 1 :justify-content :flex-end}}
      [react/view {:flex 1 :align-items :center :justify-content :center}
       [react/image {:style       (merge {:width            width
                                          :height           (- height 200)
                                          :background-color :black})
                     :resize-mode :contain
                     :source      {:uri (:image content)}}]]
      [react/view {:flex-direction :row :padding-horizontal 8
                   :justify-content :space-between :align-items :center}
       [react/view {:width 64}]
       [button/button {:on-press   #(re-frame/dispatch [:navigate-back])
                       :type       :secondary
                       :label      :t/close
                       :text-style {:color colors/white-persist}}]
       [react/touchable-highlight
        {:on-press #(re-frame/dispatch [:bottom-sheet/show-sheet
                                        {:content (sheets/image-long-press message true)
                                         :height  64}])}
        [icons/icon :main-icons/more {:container-style {:width 24 :height 24
                                                        :margin 20}
                                      :color           colors/white-persist}]]]]]))
