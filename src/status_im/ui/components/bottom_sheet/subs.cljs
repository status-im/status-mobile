(ns status-im.ui.components.bottom-sheet.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :bottom-sheet
 (fn [{:bottom-sheet/keys [show? view]}]
   {:show? show?
    :view view}))
