(ns ^:figwheel-hooks fiddle.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [fiddle.views.main :as main]
            fiddle.subs
            fiddle.events))

(defn mount-root []
  (reagent/render [main/main] (js/document.getElementById "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])
  (re-frame/dispatch [:load-icons])
  (mount-root))

(defn ^:after-load on-reload []
  (mount-root))