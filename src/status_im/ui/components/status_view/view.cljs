(ns status-im.ui.components.status-view.view
  (:require [clojure.string :as string]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.utils.core :as utils.core]))

(defn tag-view [tag]
  [react/text {:style {:color components.styles/color-blue4-faded}
               :font  :medium}
   (str tag " ")])

(defn status-view [{:keys [style
                           non-tag-color
                           message-id
                           status
                           on-press
                           number-of-lines]
                    :or {message-id "msg"
                         non-tag-color components.styles/color-black}}]
  [react/text {:style           style
               :on-press        on-press
               :number-of-lines number-of-lines
               :font            :default}
   (for [[i status] (map-indexed vector (string/split status #" "))]
     (if (utils.core/hash-tag? status)
       ^{:key (str "item-" message-id "-" i)}
       [tag-view status]
       ^{:key (str "item-" message-id "-" i)}
       [react/text {:style {:color non-tag-color}}
        (str status " ")]))])
