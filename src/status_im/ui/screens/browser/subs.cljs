(ns status-im.ui.screens.browser.subs
  (:require [re-frame.core :as re-frame]
            [status-im.browser.core :as browser]))

(re-frame/reg-sub
 :browsers
 (fn [db _]
   (:browser/browsers db)))

(re-frame/reg-sub
 :browser/browsers
 :<- [:browsers]
 (fn [browsers]
   (reduce (fn [acc [k browser]]
             (update acc k assoc :url (browser/get-current-url browser)))
           browsers
           browsers)))

(re-frame/reg-sub
 :browser/browsers-vals
 :<- [:browser/browsers]
 (fn [browsers]
   (sort-by :timestamp > (vals browsers))))

(re-frame/reg-sub
 :get-current-browser
 :<- [:get :browser/options]
 :<- [:browser/browsers]
 (fn [[options browsers]]
   (let [browser (get browsers (:browser-id options))]
     (assoc browser :secure? (browser/secure? browser options)))))
