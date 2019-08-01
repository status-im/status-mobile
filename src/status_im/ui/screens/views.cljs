(ns status-im.ui.screens.views
  (:require [status-im.ui.components.react :as react]
            [status-im.utils.navigation :as navigation]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.routing.core :as routing]))

(defn reg-root-key-sub [sub-name db-key]
  (re-frame/reg-sub sub-name (fn [db] (get db db-key))))

(reg-root-key-sub :view-id :view-id)
(reg-root-key-sub :dimensions/window :dimensions/window)

(defonce initial-view-id (atom nil))

(defn reset-component-on-mount [view-id component two-pane?]
  (when (and @initial-view-id
             (or
              js/goog.DEBUG
              (not @component)))
    (reset! component (routing/get-main-component
                       (if js/goog.DEBUG
                         @initial-view-id
                         @view-id)
                       two-pane?))))

(defn reset-component-on-update [view-id component two-pane?]
  (when (and @initial-view-id (not @component))
    (reset! component (routing/get-main-component
                       (if js/goog.DEBUG
                         @initial-view-id
                         @view-id)
                       two-pane?))))

(defn main []
  (let [view-id                 (re-frame/subscribe [:view-id])
        main-component          (atom nil)]
    (reagent/create-class
     {:component-will-mount
      (fn []
        (when-not @initial-view-id
          (reset! initial-view-id @view-id))
        (reset-component-on-mount view-id main-component false))
      :component-will-update
      (fn []
        (when-not @initial-view-id
          (reset! initial-view-id @view-id))

        (reset-component-on-update view-id main-component false)
        (react/dismiss-keyboard!))
      :reagent-render
      (fn []
        (when (and @view-id main-component)
          [react/view {:flex 1}
           [:> @main-component
            {:ref (fn [r] (navigation/set-navigator-ref r))}]]))})))