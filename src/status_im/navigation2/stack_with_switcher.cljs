(ns status-im.navigation2.stack-with-switcher
  (:require [status-im.switcher.switcher :as switcher]))

(defn overlap-stack [comp view-id]
  [:<>
   [comp]
   [switcher/switcher view-id]])
