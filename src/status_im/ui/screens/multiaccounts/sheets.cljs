(ns status-im.ui.screens.multiaccounts.sheets
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [i18n.i18n :as i18n]))

(defn actions-sheet
  []
  [quo/list-item
   {:theme               :accent
    :on-press            #(do (re-frame/dispatch [:bottom-sheet/hide])
                              (re-frame/dispatch [:generate-and-derive-addresses]))
    :icon                :main-icons/add
    :accessibility-label :generate-a-new-key
    :title               (i18n/label :t/generate-a-new-key)}])
