(ns status-im.ui.screens.browser.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub :browsers :browser/browsers)

(re-frame/reg-sub
 :get-current-browser
 :<- [:get :browser/options]
 :<- [:browsers]
 (fn [[options browsers]]
   (get browsers (:browser-id options))))
