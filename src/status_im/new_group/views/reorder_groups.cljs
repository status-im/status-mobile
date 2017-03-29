(ns status-im.new-group.views.reorder-groups
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                text
                                                icon
                                                touchable-highlight
                                                list-item]]
            [status-im.components.sticky-button :refer [sticky-button]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar-new.view :refer [toolbar]]
            [status-im.components.sortable-list-view :refer [sortable-list-view sortable-item]]
            [status-im.components.common.common :as common]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.utils.platform :refer [android?]]
            [status-im.new-group.styles :as st]
            [status-im.i18n :refer [label label-pluralize]]
            [status-im.utils.platform :refer [platform-specific]]
            [reagent.core :as r]))

(defn toolbar-view []
  [toolbar {:actions [{:image :blank}]
            :title   (label :t/reorder-groups)}])

(defn group-item [{:keys [name contacts] :as group}]
  (let [cnt (count contacts)]
    [view st/order-item-container
     [view st/order-item-inner-container
      [text {:style st/order-item-label}
       name]
      [text {:style st/order-item-contacts}
       (str cnt " " (label-pluralize cnt :t/contact-s))]
      [view {:flex 1}]
      [view st/order-item-icon
       [icon :grab_gray]]]]))

(defn render-separator [last]
  (fn [_ row-id _]
    (list-item
      (if (= row-id last)
        ^{:key "bottom-shaddow"}
        [common/bottom-shaddow]
        ^{:key row-id}
        [view st/order-item-separator-wrapper
         [view st/order-item-separator]]))))

(defview reorder-groups []
  [groups [:get :contact-groups]
   order  [:get :groups-order]]
  (let [this (r/current-component)]
    [view st/reorder-groups-container
     [status-bar]
     [toolbar-view]
     [view st/reorder-list-container
      [common/top-shaddow]
      [sortable-list-view
       {:data             groups
        :order            order
        :on-row-moved     #(do (dispatch-sync [:change-group-order (:from %) (:to %)])
                               (.forceUpdate this))
        :render-row       (fn [row]
                           (sortable-item [group-item row]))
        :render-separator (render-separator (last order))}]]
     [sticky-button (label :t/save) #(dispatch [:save-group-order])]]))
