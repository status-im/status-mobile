(ns status-im2.setup.datetime
  (:require [re-frame.core :as re-frame]
            [utils.datetime :as datetime]))

(re-frame/reg-cofx
 :now
 (fn [coeffects _]
   (assoc coeffects :now (datetime/timestamp))))