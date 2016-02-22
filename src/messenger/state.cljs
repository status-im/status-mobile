(ns messenger.state
  (:require [om.next :as om]
            [re-natal.support :as sup]))

(defonce app-state (atom {}))

(defmulti read om/dispatch)
(defmethod read :default
  [{:keys [state]} k _]
  (let [st @state]
    (if-let [[_ v] (find st k)]
      {:value v}
      {:value :not-found})))

(defonce reconciler
  (om/reconciler
   {:state        app-state
    :parser       (om/parser {:read read})
    :root-render  sup/root-render
    :root-unmount sup/root-unmount}))
