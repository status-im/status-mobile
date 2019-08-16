(ns fiddle.views.icons
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.colors :as colors]))

(views/defview icons []
  (views/letsubs [icons [:icons]]
    (if icons
      [react/view {:flex-direction :row :flex-wrap :wrap :flex 1 :background-color colors/gray-transparent-40}
       (for [icon icons]
         [react/view {:padding 20}
          [react/text {:srtle {:margin-bottom 5}} icon]
          [vector-icons/icon icon]])]
      [react/view
       [react/text "To see all icons please run `clj prepare.clj` command in terminal"]])))