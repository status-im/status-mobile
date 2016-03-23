(ns messenger.utils.state
  (:require [om.next :as om]))

(defn from-state [component key]
  (-> (om/get-state component)
      (js->clj :keywordize-keys true)
      key))
