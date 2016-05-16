(ns syng-im.discovery.views.popular-list-item
  (:require [syng-im.components.react :refer [view text image]]
            [syng-im.discovery.styles :as st]
            [reagent.core :as r]))

(defn popular-list-item
  [{:keys [name status]}]
  [view st/popular-list-item
   [view st/popular-list-item-name-container
    [text {:style st/popular-list-item-name} name]
    [text {:style         st/popular-list-item-status
           :numberOfLines 2} status]]
   [view st/popular-list-item-avatar-container
    [image {:style  st/popular-list-item-avatar
            :source {:uri :icon_avatar}}]]])
