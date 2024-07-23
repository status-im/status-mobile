(ns legacy.status-im.browser.effects
  (:require [clojure.string :as string]
            [react-native.core :as react]
            [utils.re-frame :as rf]
            [utils.url :as url]))

(rf/reg-fx
 :linking/open-url
 (fn [url]
   (when (not (string/blank? url))
     (.openURL ^js react/linking (url/normalize-url url)))))
