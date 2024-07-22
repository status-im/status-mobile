(ns legacy.status-im.ui.components.dialog
  (:require
    ["react-native-dialogs" :default dialogs]))

(defn show
  [{:keys [title options cancel-text on-cancel]}]
  (.. dialogs
      (showPicker title
                  nil
                  (clj->js {:items        (mapv #(select-keys % [:label])
                                                options)
                            :negativeText cancel-text
                            :positiveText nil}))
      (then (fn [selected]
              (let [result (js->clj selected :keywordize-keys true)]
                (if (not= (get result :action) "actionSelect")
                  (when on-cancel
                    (on-cancel))
                  (when-let [label (get-in result
                                           [:selectedItem :label])]
                    (when-let [action (->> options
                                           (filter #(= label (:label %)))
                                           first
                                           :action)]
                      (action)))))))))
