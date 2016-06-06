(ns status-im.chat.handlers.animation
  (:require [re-frame.core :refer [register-handler after dispatch]]
            [re-frame.middleware :refer [path]]
            [status-im.models.commands :as commands]
            [status-im.handlers.content-suggestions :refer [get-content-suggestions]]
            [status-im.chat.styles.message-input :refer [input-height]]
            [status-im.chat.styles.response :refer [request-info-height response-height-normal]]
            [status-im.chat.styles.response-suggestions :as response-suggestions-styles]
            [status-im.constants :refer [response-input-hiding-duration]]))

(def zero-height input-height)

(register-handler :finish-animate-cancel-command
  (fn [db _]
    (assoc-in db [:animations :commands-input-is-switching?] false)))

(register-handler :animate-cancel-command
  (path :animations)
  (fn [db _]
    (if-not (:commands-input-is-switching? db)
      (assoc db
        :commands-input-is-switching? true
        :message-input-buttons-scale 1
        :message-input-offset 0
        :to-response-height zero-height
        :messages-offset 0)
      db)))

(register-handler :finish-animate-response-resize
  (fn [db _]
    (let [fixed (get-in db [:animations :to-response-height])]
      (-> db
          (assoc-in [:animations :response-height-current] fixed)
          (assoc-in [:animations :response-resize?] false)))))

(register-handler :set-response-height
  (fn [db [_ value]]
    (assoc-in db [:animations :response-height-current] value)))

(register-handler :animate-response-resize
  (fn [db _]
    (assoc-in db [:animations :response-resize?] true)))

(defn get-response-height [db]
  (let [command (commands/get-chat-command db)
        text (commands/get-chat-command-content db)
        suggestions (get-content-suggestions command text)
        suggestions-height (reduce + 0 (map #(if (:header %)
                                              response-suggestions-styles/header-height
                                              response-suggestions-styles/suggestion-height)
                                            suggestions))]
    (+ zero-height
       (min response-height-normal (+ suggestions-height request-info-height)))))

(defn update-response-height [db]
  (assoc-in db [:animations :to-response-height] (get-response-height db)))

(register-handler :finish-show-response
  (after #(dispatch [:prepare-message-input]))
  (fn [db _]
    (assoc-in db [:animations :commands-input-is-switching?] false)))

(register-handler :animate-show-response
  (after #(dispatch [:animate-response-resize]))
  (fn [db _]
    (-> db
        (assoc-in [:animations :commands-input-is-switching?] true)
        (assoc-in [:animations :response-height-current] zero-height)
        (assoc-in [:animations :message-input-buttons-scale] 0.1)
        (assoc-in [:animations :message-input-offset] -40)
        (assoc-in [:animations :messages-offset] request-info-height)
        (update-response-height))))

(register-handler :set-response-max-height
  (fn [db [_ height]]
    (let [prev-height (get-in db [:animations :response-height-max])]
      (if (not= height prev-height)
        (let [db (assoc-in db [:animations :response-height-max] height)]
          (if (= prev-height (get-in db [:animations :to-response-height]))
            (-> db
                (assoc-in [:animations :to-response-height] height)
                (assoc-in [:animations :response-height-current] height))
            db))
        db))))

(register-handler :on-drag-response
  (fn [db [_ dy]]
    (let [fixed (get-in db [:animations :to-response-height])]
      (-> db
          (assoc-in [:animations :response-height-current] (- fixed dy))
          (assoc-in [:animations :response-resize?] false)))))

(register-handler :fix-response-height
  (fn [db _]
    (if (and (commands/get-chat-command-to-msg-id db)
             (not (get-in db [:animations :commands-input-is-switching?])))
      (let [current (get-in db [:animations :response-height-current])
            normal-height response-height-normal
            command (commands/get-chat-command db)
            text (commands/get-chat-command-content db)
            suggestions (get-content-suggestions command text)
            max-height (get-in db [:animations :response-height-max])
            delta (/ normal-height 2)
            new-fixed (cond
                        (or (<= current (+ zero-height delta))
                            (empty? suggestions)) (+ zero-height request-info-height)
                        (<= current (+ zero-height normal-height delta)) (get-response-height db)
                        :else max-height)]
        (dispatch [:animate-response-resize])
        (assoc-in db [:animations :to-response-height] new-fixed))
      db)))
