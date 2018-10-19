(ns status-im.ui.components.dialog
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]))

(def dialogs (.-default rn-dependencies/dialogs))

(defn show [{:keys [title options cancel-text]}]
  (.. dialogs
      (showPicker title nil (clj->js {:items        (mapv #(select-keys % [:label])
                                                          options)
                                      :negativeText cancel-text
                                      :positiveText nil}))
      (then (fn [selected]
              (when-let [label (get-in (js->clj selected :keywordize-keys true)
                                       [:selectedItem :label])]
                (when-let [action (->> options
                                       (filter #(= label (:label %)))
                                       first
                                       :action)]
                  (action)))))))
