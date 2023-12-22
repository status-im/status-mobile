(ns status-im.contexts.preview-screens.quo-preview.component-preview.events
  (:require
    [quo.core :as quo]
    [re-frame.core :as re-frame]))

(def preview-screen-name :dev-component-preview)

(when js/goog.DEBUG
  (re-frame/reg-event-fx :dev/preview-component
   (fn [{:keys [db]} [[component-tag props & args]]]
     (let [view-id   (:view-id db)
           ;; NOTE: re-render on every evaluation e.g. reset component state without changes
           component (into [component-tag (assoc props :key (random-uuid))] args)
           navigate? (not= view-id preview-screen-name)]
       (cond-> {:db (assoc db :dev/previewed-component component)}
         navigate? (assoc :fx [[:dispatch [:navigate-to preview-screen-name]]]))))))

;; Usage example
(comment
  (re-frame/dispatch [:dev/preview-component
                      [quo/slide-button
                       {:track-icon          :face-id
                        :track-text          ":debug-component example"
                        :customization-color :blue
                        :on-complete         identity}]]))
