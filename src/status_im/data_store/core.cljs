(ns status-im.data-store.core
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-fx
 :data-store/tx
 (fn [transactions]))
