(ns status-im.chat.handlers.animation
  (:require [re-frame.core :refer [after dispatch debug]]
            [status-im.utils.handlers :refer [register-handler]]
            [re-frame.middleware :refer [path]]
            [status-im.handlers.content-suggestions :refer [get-content-suggestions]]
            [status-im.chat.constants :refer [input-height request-info-height
                                              minimum-command-suggestions-height
                                              response-height-normal minimum-suggestion-height]]
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
    (assoc db :to-response-height input-height)))

(def response-height (+ input-height response-height-normal))

(defn update-response-height [db]
  (assoc-in db [:animations :to-response-height] response-height))

(register-handler :animate-command-suggestions
  (fn [{:keys [current-chat-id] :as db} _]
    (let [suggestions? (seq (get-in db [:command-suggestions current-chat-id]))
          current (get-in db [:animations :command-suggestions-height])
          height (if suggestions? middle-height 0.1)
          changed? (if (and suggestions? (not= 0.1 current))
                     identity inc)]
      (-> db
          (update :animations assoc :command-suggestions-height height)
          (update-in [:animations :commands-height-changed] changed?)))))

(defn get-minimum-height
  [{:keys [current-chat-id] :as db}]
  (let [path [:chats current-chat-id :command-input :command :type]
        type (get-in db path)]
    (if (= :response type)
      minimum-suggestion-height
      0.1)))

(register-handler :animate-show-response
  [(after #(dispatch [:command-edit-mode]))]
  (fn [{:keys [current-chat-id] :as db}]
    (let [suggestions? (seq (get-in db [:suggestions current-chat-id]))
          height (if suggestions?
                   middle-height
                   (get-minimum-height db))]
      (assoc-in db [:animations :to-response-height] height))))

(defn fix-height
  [height-key height-signal-key suggestions-key minimum]
  (fn [{:keys [current-chat-id] :as db} [_ vy current]]
    (let [max-height (get-in db [:layout-height])
          moving-down? (pos? vy)
          moving-up? (not moving-down?)
          under-middle-position? (<= current middle-height)
          over-middle-position? (not under-middle-position?)
          suggestions (get-in db [suggestions-key current-chat-id])
          new-fixed (cond (not suggestions)
                          (minimum db)

                          (and under-middle-position? moving-up?)
                          middle-height

                          (and over-middle-position? moving-down?)
                          middle-height

                          (and over-middle-position? moving-up?)
                          max-height

                          (and under-middle-position? moving-down?)
                          (minimum db))]
      (-> db
          (assoc-in [:animations height-key] new-fixed)
          (update-in [:animations height-signal-key] inc)))))

(register-handler :fix-commands-suggestions-height
  (fix-height :command-suggestions-height
              :commands-height-changed
              :command-suggestions
              (constantly minimum-command-suggestions-height)))

(register-handler :fix-response-height
  (fix-height :to-response-height
              :response-height-changed
              :suggestions
              get-minimum-height))
