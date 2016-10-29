(ns status-im.chat.handlers.animation
  (:require [re-frame.core :refer [after dispatch subscribe debug path]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.chat.constants :refer [input-height request-info-height
                                              suggestions-header-height
                                              minimum-command-suggestions-height
                                              response-height-normal minimum-suggestion-height]]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.constants :refer [response-input-hiding-duration]]
            [taoensso.timbre :as log]))

;; todo magic value
(def middle-height 270)

(defn animation-handler
  ([name handler] (animation-handler name nil handler))
  ([name middleware handler]
   (register-handler name [(path :animations) middleware] handler)))

(register-handler :animate-cancel-command
  (after #(dispatch [:text-edit-mode]))
  (fn [{:keys [current-chat-id] :as db} _]
    (assoc-in db [:animations :to-response-height current-chat-id] input-height)))

(def response-height (+ input-height response-height-normal))

(register-handler :animate-command-suggestions
  (fn [{chat-id :current-chat-id :as db} _]
    (let [suggestions? (seq (get-in db [:command-suggestions chat-id]))
          current (get-in db [:animations :command-suggestions-height chat-id])
          height (if suggestions? middle-height input-height)
          changed? (if (and suggestions?
                            (not (nil? current))
                            (not= input-height current))
                     identity inc)]
      (-> db
          (assoc-in [:animations :command-suggestions-height chat-id] height)
          (update-in [:animations :commands-height-changed] changed?)))))

(defn get-minimum-height
  [{:keys [current-chat-id] :as db}]
  (let [path [:chats current-chat-id :command-input :command :type]
        type (get-in db path)
        command? (= :command type)
        response? (not command?)
        errors (get-in db [:validation-errors current-chat-id])
        validation-errors? (seq errors)
        suggestion? (get-in db [:has-suggestions? current-chat-id])
        custom-errors (get-in db [:custom-validation-errors current-chat-id])
        custom-errors? (seq custom-errors)
        validation-errors?  (or validation-errors? custom-errors?)]
    (cond-> 0
            validation-errors? (+ request-info-height)
            response? (+ minimum-suggestion-height)
            command? (+ input-height)
            (and suggestion? command?) (+ suggestions-header-height)
            ;custom-errors? (+ suggestions-header-height)
            (and command? validation-errors?) (+ suggestions-header-height))))

(register-handler :animate-show-response
  ;[(after #(dispatch [:command-edit-mode]))]
  (fn [{:keys [current-chat-id] :as db}]
    (let [suggestions? (get-in db [:has-suggestions? current-chat-id])
          fullscreen?  (get-in db [:chats current-chat-id :command-input :command :fullscreen])
          max-height   (get-in db [:layout-height])
          height       (if suggestions?
                         (if fullscreen?
                           max-height
                           middle-height)
                         (get-minimum-height db))]
      (assoc-in db [:animations :to-response-height current-chat-id] height))))

(defn fix-height
  [height-key height-signal-key suggestions-key minimum]
  (fn [{:keys [current-chat-id] :as db} [_ vy current no-animation]]
    (let [input-margin           (subscribe [:input-margin])
          max-height             (- (get-in db [:layout-height])
                                    (get-in platform-specific [:component-styles :status-bar :default :height])
                                    @input-margin)
          moving-down? (pos? vy)
          moving-up? (not moving-down?)
          under-middle-position? (<= current middle-height)
          over-middle-position? (not under-middle-position?)
          suggestions (get-in db [suggestions-key current-chat-id])
          old-fixed (get-in db [:animations height-key current-chat-id])

          new-fixed (cond (not suggestions)
                          (minimum db)

                          (and (nil? vy) (nil? current)
                               (> old-fixed middle-height))
                          max-height

                          (and (nil? vy) (nil? current)
                               (< old-fixed middle-height))
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
          (assoc-in [:animations height-key current-chat-id] new-fixed)
          (update-in [:animations height-signal-key] inc)
          (assoc-in [:animate? current-chat-id] (not no-animation))))))

(defn commands-min-height
  [{:keys [current-chat-id] :as db}]
  (let [suggestions (get-in db [:command-suggestions current-chat-id])]
    (if (seq suggestions)
      minimum-command-suggestions-height
      input-height)))

(register-handler :fix-commands-suggestions-height
  (fix-height :command-suggestions-height
              :commands-height-changed
              :command-suggestions
              commands-min-height))

(register-handler :fix-response-height
  (fix-height :to-response-height
              :response-height-changed
              :has-suggestions?
              get-minimum-height))
