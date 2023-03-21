(ns status-im.ui.screens.multiaccounts.sheets
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [utils.i18n :as i18n]))

(defn- hide-sheet-and-dispatch
  [event]
  (re-frame/dispatch [:bottom-sheet/hide-old])
  (re-frame/dispatch event))

(defn actions-sheet
  []
  [quo/list-item
   {:theme               :accent
    :on-press            #(hide-sheet-and-dispatch [:navigate-to :intro])
    :icon                :main-icons/add
    :accessibility-label :generate-a-new-key
    :title               (i18n/label :t/generate-a-new-key)}])
