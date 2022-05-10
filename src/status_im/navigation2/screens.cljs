(ns status-im.navigation2.screens
  (:require [status-im.navigation2.home-stack :as home-stack]))

(def screens [{:name      :home-stack
               :insets    {:top false}
               :component home-stack/home}])
