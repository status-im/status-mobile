(ns status-im.ui.screens.extensions.add.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :get-extension-url
 (fn [db]
   (:extension-url db)))

(re-frame/reg-sub
 :get-staged-extension
 (fn [db]
   (:staged-extension db)))
