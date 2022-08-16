(ns status-im.ui.components.action-sheet
  (:require [status-im.i18n.i18n :as i18n]
            [status-im.utils.core :as utils]
            ["react-native" :refer (ActionSheetIOS)]))

(defn- callback [options on-cancel]
  (fn [index]
    (if (< index (count options))
      (when-let [handler (:action (nth options index))]
        (handler))
      (when on-cancel
        (on-cancel)))))

(defn- prepare-options [title message options]
  (let [destructive-opt-index (utils/first-index :destructive? options)] ;; TODO Can only be a single destructive?
    (clj->js (merge {:options           (conj (mapv :label options) (i18n/label :t/cancel))
                     :cancelButtonIndex (count options)}
                    (when destructive-opt-index
                      {:destructiveButtonIndex destructive-opt-index})
                    (when title {:title title})
                    (when message {:message message})))))

(defn show [{:keys [title message options on-cancel]}]
  (.showActionSheetWithOptions ActionSheetIOS
                               (prepare-options title message options)
                               (callback options on-cancel)))
