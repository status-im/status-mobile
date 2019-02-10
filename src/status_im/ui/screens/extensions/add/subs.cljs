(ns status-im.ui.screens.extensions.add.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :get-manage-extension
 (fn [db]
   (:extensions/manage db)))

(re-frame/reg-sub
 :get-staged-extension
 (fn [db]
   (:extensions/staged-extension db)))
