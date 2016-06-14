(ns status-im.chat.handlers.animation
  (:require [re-frame.core :refer [register-handler after dispatch]]
            [re-frame.middleware :refer [path]]
            [status-im.models.commands :as commands]
            [status-im.handlers.content-suggestions :refer [get-content-suggestions]]
            [status-im.chat.styles.response :refer [request-info-height response-height-normal animation-offset]]
            [status-im.chat.styles.response-suggestions :as response-suggestions-styles]
            [status-im.constants :refer [response-input-hiding-duration]]))

(def zero-height animation-offset)

(defn animation-handler
  ([name handler] (animation-handler name nil handler))
  ([name middleware handler]
   (register-handler name [(path :animations) middleware] handler)))

(animation-handler :finish-animate-cancel-command
  (fn [animations _]
    (assoc animations :commands-input-is-switching? false
                      ::cancel-command? false)))

(animation-handler :animate-cancel-command
  (fn [db _]
    (if-not (:commands-input-is-switching? db)
      (assoc db
        :commands-input-is-switching? true
        ::cancel-command? true
        :message-input-buttons-scale 1
        :message-input-offset 0
        :to-response-height zero-height
        :messages-offset 0)
      db)))

(defn check-finish-cancel-command [db _]
  (when (::cancel-command? db)
    (dispatch [:finish-animate-cancel-command])
    (dispatch [:cancel-command])))

(animation-handler :finish-animate-response-resize
  (after check-finish-cancel-command)
  (fn [db _]
    (let [fixed (:to-response-height db)]
      (assoc db :response-height-current fixed
                :response-resize? false))))

(animation-handler :set-response-height
  (fn [db [_ value]]
    (assoc db :response-height-current value)))

(animation-handler :animate-response-resize
  (fn [db _]
    (assoc db :response-resize? true)))

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
  (let [max-height (get-in db [:animations :response-height-max])
        mode (get-in db [:animations :response-height-mode])]
    (assoc-in db [:animations :to-response-height]
              (if (= mode :fit)
                (+ zero-height max-height)
                (get-response-height db)))))

(animation-handler :finish-show-response
  (fn [db _]
    (assoc db :commands-input-is-switching? false)))

(register-handler :animate-show-response
  (after #(dispatch [:animate-response-resize]))
  (fn [db _]
    (-> db
        (assoc-in [:animations :commands-input-is-switching?] true)
        (assoc-in [:animations :response-height-mode] :wrap)
        (assoc-in [:animations :response-height-current] zero-height)
        (assoc-in [:animations :message-input-buttons-scale] 0.1)
        (assoc-in [:animations :message-input-offset] -40)
        (assoc-in [:animations :messages-offset] request-info-height)
        (update-response-height))))

(animation-handler :set-response-max-height
  (fn [db [_ new-max-height]]
    (let [prev-max-height (:response-height-max db)]
      (if (not= new-max-height prev-max-height)
        (as-> db db
              (assoc db :response-height-max new-max-height)
              (if (= (+ zero-height prev-max-height) (:to-response-height db))
                (assoc db :to-response-height (+ zero-height new-max-height))
                db)
              (if (= (+ zero-height prev-max-height) (:response-height-current db))
                (assoc db :response-height-current (+ zero-height new-max-height))
                db))
        db))))

(animation-handler :on-drag-response
  (fn [db [_ dy]]
    (let [fixed (:to-response-height db)]
      (assoc db :response-height-current (- fixed dy)
                :response-resize? false))))

(defn animate-fix-reponse-height [db _]
  (when (and (commands/get-chat-command-to-msg-id db)
             (not (get-in db [:animations :commands-input-is-switching?])))
    (dispatch [:animate-response-resize])))

(register-handler :fix-response-height
  (after animate-fix-reponse-height)
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
            mode (cond
                   (or (<= current (+ zero-height delta))
                       (empty? suggestions)) :hide
                   (<= current (+ zero-height normal-height delta)) :wrap
                   :else :fit)
            new-fixed (case mode
                        :hide (+ zero-height request-info-height)
                        :wrap (get-response-height db)
                        :fit (+ zero-height max-height))]
        (-> db
            (assoc-in [:animations :to-response-height] new-fixed)
            (assoc-in [:animations :response-height-mode] mode)))
      db)))
