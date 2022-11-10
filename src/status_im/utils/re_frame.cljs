(ns status-im.utils.re-frame
  (:require [re-frame.core :as re-frame]))

(def sub (comp deref re-frame/subscribe))

(def dispatch re-frame/dispatch)

