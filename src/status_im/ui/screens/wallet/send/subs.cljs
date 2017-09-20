(ns status-im.ui.screens.wallet.send.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub :camera-dimensions
  (fn [db]
    (get-in db [:wallet :camera-dimensions])))

(re-frame/reg-sub :ios-camera-permitted?
  (fn [db]
    (get-in db [:wallet :ios-camera-permitted?])))
