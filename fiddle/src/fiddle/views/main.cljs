(ns fiddle.views.main
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.button.view :as button]
            [re-frame.core :as re-frame]))

(defn btn [id label view-id]
  [button/primary-button
   {:disabled? (= id view-id)
    :style     {:margin-top 5}
    :on-press  #(re-frame/dispatch [:set :view-id id])}
   label])

(views/defview main []
  (views/letsubs [view-id [:view-id]
                  view [:view]]
    [react/view {:flex 1 :flex-direction :row :padding 10}
     [react/view {:padding-right 20}
      [btn :colors "Colors" view-id]
      [btn :icons "Icons" view-id]
      [btn :typography "Typography" view-id]
      [btn :list-items "List items" view-id]
      [btn :screens "Screens" view-id]]
     (when view
       [view])]))