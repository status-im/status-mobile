(ns status-im2.contexts.debug-component.events
  (:require
    [quo.core :as quo]
    [re-frame.core :as re-frame]))

(def ^:const debug-screen-name :dev-debug-component-sheet)

(when js/goog.DEBUG
  (re-frame/reg-event-fx :debug-component
   (fn [{:keys [db]} [[component-tag props & args]]]
     (let [view-id   (:view-id db)
           ;; NOTE: re-render on every evaluation e.g. reset component state without changes
           component (into [component-tag (assoc props :key (random-uuid))] args)
           navigate? (not= view-id debug-screen-name)]
       (cond-> {:db (assoc db :debug/component component)}
         navigate? (assoc :fx [[:dispatch [:navigate-to debug-screen-name]]]))))))

;; Usage example
(comment
  (re-frame/dispatch [:debug-component
                      [quo/slide-button
                       {:track-icon          :face-id
                        :track-text          ":debug-component example"
                        :customization-color :blue
                        :on-complete         identity}]]))
