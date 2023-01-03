(ns status-im.ui.screens.multiaccounts.sheets
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [i18n.i18n :as i18n]
            [status-im2.common.bottom-sheet.view :as bottom-sheet]))

(defn- hide-sheet-and-dispatch
  [event]
  (bottom-sheet/close-bottom-sheet-fn nil)
  (re-frame/dispatch event))

(defn actions-sheet
  []
  [quo/list-item
   {:theme               :accent
    :on-press            #(hide-sheet-and-dispatch [:generate-and-derive-addresses])
    :icon                :main-icons/add
    :accessibility-label :generate-a-new-key
    :title               (i18n/label :t/generate-a-new-key)}])
