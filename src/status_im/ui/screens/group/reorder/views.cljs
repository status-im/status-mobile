(ns status-im.ui.screens.group.reorder.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [status-im.ui.components.react :refer [view text list-item]]
            [status-im.ui.components.icons.vector-icons :as vi]
            [status-im.ui.components.sticky-button :refer [sticky-button]]
            [status-im.ui.components.status-bar :refer [status-bar]]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.sortable-list-view :refer [sortable-list-view sortable-item]]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.screens.group.styles :as styles]
            [status-im.i18n :refer [label label-pluralize]]))


(defn toolbar-view []
  [toolbar/simple-toolbar
   (label :t/reorder-groups)])

(defn group-item [{:keys [name contacts] :as group}]
  (let [cnt (count contacts)]
    [view styles/order-item-container
     [view styles/order-item-inner-container
      [text {:style styles/order-item-label}
       name]
      [text {:style styles/order-item-contacts}
       (str cnt " " (label-pluralize cnt :t/contact-s))]
      [view {:flex 1}]
      [view styles/order-item-icon
       [vi/icon :icons/grab]]]]))

(defn render-separator [last]
  (fn [_ row-id _]
    (list-item
      (if (= row-id last)
        ^{:key "bottom-shadow"}
        [common/bottom-shadow]
        ^{:key row-id}
        [view styles/order-item-separator-wrapper
         [view styles/order-item-separator]]))))

(defview reorder-groups []
  (letsubs [groups [:get-contact-groups]
            order  [:get :group/groups-order]]
    (let [this (reagent/current-component)]
      [view styles/reorder-groups-container
       [status-bar]
       [toolbar-view]
       [view styles/reorder-list-container
        [common/top-shadow]
        [sortable-list-view
         {:data             groups
          :order            order
          :on-row-moved     #(do (dispatch-sync [:change-contact-group-order (:from %) (:to %)])
                                 (.forceUpdate this))
          :render-row       (fn [row]
                             (sortable-item [group-item row]))
          :render-separator (render-separator (last order))}]]
       [sticky-button (label :t/save) #(do
                                         (dispatch [:save-contact-group-order])
                                         (dispatch [:navigate-to-clean :contact-list]))]])))
