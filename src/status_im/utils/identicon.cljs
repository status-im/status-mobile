(ns status-im.utils.identicon
  (:require
   [re-frame.core :as re-frame]
   [status-im.native-module.core :as native-module]))

(def identicon (memoize native-module/identicon))

(def identicon-async native-module/identicon-async)

(re-frame/reg-fx
 :insert-identicons
 (fn [key-path-seq]
   (for [key-path key-path-seq]
     (let [public-key (first key-path)
           path-for-identicon (second key-path)]
       (identicon-async public-key #(re-frame/dispatch [:identicon-generated path-for-identicon %]))))))
