(ns status-im.subs.bottom-sheet
  (:require
    [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :bottom-sheet-sheets
 :<- [:bottom-sheet]
 :-> :sheets)
