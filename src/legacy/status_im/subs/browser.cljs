(ns legacy.status-im.subs.browser
  (:require
    [legacy.status-im.browser.core :as browser]
    [re-frame.core :as re-frame]))

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
   (reverse (vals browsers))))

(re-frame/reg-sub
 :get-current-browser
 :<- [:browser/options]
 :<- [:browser/browsers]
 (fn [[options browsers]]
   (let [browser (get browsers (:browser-id options))]
     (assoc browser :secure? (browser/secure? browser options)))))

(re-frame/reg-sub
 :bookmarks/active
 :<- [:bookmarks]
 (fn [bookmarks]
   (into {} (remove #(:removed (second %)) bookmarks))))
