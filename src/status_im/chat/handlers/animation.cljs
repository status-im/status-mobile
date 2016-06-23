(ns status-im.chat.handlers.animation
  (:require [re-frame.core :refer [register-handler after dispatch debug]]
            [re-frame.middleware :refer [path]]
            [status-im.models.commands :as commands]
            [status-im.handlers.content-suggestions :refer [get-content-suggestions]]
            [status-im.chat.constants :refer [input-height request-info-height
                                              response-height-normal minimum-suggestion-height]]
            [status-im.chat.styles.response-suggestions :as response-suggestions-styles]
            [status-im.constants :refer [response-input-hiding-duration]]))

;; todo magic value
(def middle-height 270)

(defn animation-handler
  ([name handler] (animation-handler name nil handler))
  ([name middleware handler]
   (register-handler name [(path :animations) middleware] handler)))

(animation-handler :animate-cancel-command
  (after #(dispatch [:text-edit-mode]))
  (fn [db _]
    (assoc db
      :to-response-height input-height
      :messages-offset 0)))

(defn get-response-height
  [{:keys [current-chat-id] :as db}]
  (let [suggestions        (get-in db [:suggestions current-chat-id])
        suggestions-height (if suggestions middle-height 0)]
    (+ input-height
       (min response-height-normal (+ suggestions-height request-info-height)))))

(defn update-response-height [db]
  (assoc-in db [:animations :to-response-height] (get-response-height db)))

(register-handler :animate-show-response
  [(after #(dispatch [:command-edit-mode]))]
  (fn [db _]
    (-> db
        (assoc-in [:animations :messages-offset] request-info-height)
        (update-response-height))))

(animation-handler :set-response-max-height
  (fn [db [_ height]]
    (let [prev-height (:response-height-max db)]
      (if (not= height prev-height)
        (let [db (assoc db :response-height-max height)]
          (if (= prev-height (:to-response-height db))
            (assoc db :to-response-height height)
            db))
        db))))

(register-handler :fix-response-height
  (fn [{:keys [current-chat-id] :as db} [_ vy current]]
    (let [max-height             (get-in db [:animations :response-height-max])
          moving-down?           (pos? vy)
          moving-up?             (not moving-down?)
          under-middle-position? (<= current middle-height)
          over-middle-position?  (not under-middle-position?)
          suggestions            (get-in db [:suggestions current-chat-id])
          new-fixed              (cond (not suggestions)
                                       minimum-suggestion-height

                                       (and under-middle-position? moving-up?)
                                       middle-height

                                       (and over-middle-position? moving-down?)
                                       middle-height

                                       (and over-middle-position? moving-up?)
                                       max-height

                                       (and under-middle-position?
                                            moving-down?)
                                       minimum-suggestion-height)]
      (-> db
          (assoc-in [:animations :to-response-height] new-fixed)
          (update-in [:animations :response-height-changed] inc)))))
