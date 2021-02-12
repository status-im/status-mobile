(ns status-im.ui.screens.multiaccounts.sheets
  (:require [quo.core :as quo]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]))

(defn actions-sheet []
  [react/view
   [quo/list-item {:theme               :accent
                   :on-press            #(do (re-frame/dispatch [:bottom-sheet/hide])
                                             (re-frame/dispatch [:multiaccounts.create.ui/intro-wizard]))
                   :icon                :main-icons/add
                   :accessibility-label :generate-a-new-key
                   :title               (i18n/label :t/generate-a-new-key)}]])
