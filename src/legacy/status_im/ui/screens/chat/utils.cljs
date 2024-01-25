(ns legacy.status-im.ui.screens.chat.utils
  (:require
    [clojure.string :as string]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.react :as react]
    [utils.i18n :as i18n]))

(defn format-author-old
  ([contact] (format-author-old contact nil))
  ([{:keys [primary-name secondary-name nickname]} {:keys [modal profile? you?]}]
   (if (not (string/blank? secondary-name))
     [react/nested-text
      {:number-of-lines 2
       :style           {:color       (if modal colors/white-persist colors/blue)
                         :font-size   (if profile? 15 13)
                         :line-height (if profile? 22 18)
                         :font-weight "500"}}
      (subs primary-name 0 81)
      (when you?
        [{:style {:color colors/gray :font-weight "400" :font-size 13}}
         (str " " (i18n/label :t/You))])
      (when nickname
        [{:style {:color colors/gray :font-weight "400"}}
         (str " " (subs secondary-name 0 81))])]
     [react/text
      {:style {:color       (if modal colors/white-persist colors/gray)
               :font-size   (if profile? 15 12)
               :line-height (if profile? 22 18)
               :font-weight "400"}}
      primary-name])))
