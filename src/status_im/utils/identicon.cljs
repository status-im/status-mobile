(ns status-im.utils.identicon
  (:require [native-module.core :as native-module]))

(def identicon (memoize native-module/identicon))
