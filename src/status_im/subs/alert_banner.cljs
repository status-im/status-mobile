(ns status-im.subs.alert-banner
  (:require
    [re-frame.core :as re-frame]
    [status-im.constants :as constants]))

(re-frame/reg-sub
 :alert-banners/top-margin
 :<- [:alert-banners]
 :<- [:alert-banners/hide?]
 (fn [[banners hide-banners?]]
   (let [banners-count (count banners)]
     (if (and (pos? banners-count) (not hide-banners?))
       (+ (* constants/alert-banner-height banners-count) 10)
       0))))
