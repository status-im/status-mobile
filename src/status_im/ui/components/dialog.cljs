(ns status-im.ui.components.dialog
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]))

(defn- callback [options]
  (fn [index]
    (when (< index (count options))
      (when-let [handler (:action (nth options index))]
        (handler)))))

(defn- show [{:keys [title options cancel-text]}]
  (let [dialog (new rn-dependencies/dialogs)]
    (.set dialog (clj->js {:title         title
                           :negativeText  cancel-text
                           :items         (mapv :label options)
                           :itemsCallback (callback options)}))
    (.show dialog)))
