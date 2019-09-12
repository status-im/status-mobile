(ns status-im.utils.identicon
  (:require
   [status-im.native-module.core :as native-module]))

(def identicon (memoize native-module/identicon))
