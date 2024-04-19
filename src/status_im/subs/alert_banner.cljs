(ns status-im.subs.alert-banner
  (:require
    [re-frame.core :as re-frame]
    [status-im.constants :as constants]))

(re-frame/reg-sub
 :alert-banners/top-margin
 :<- [:alert-banners]
 (fn [banners]
   (let [banners-count (count banners)]
     (when (pos? banners-count)
       (+ (* constants/alert-banner-height banners-count) 8)))))
