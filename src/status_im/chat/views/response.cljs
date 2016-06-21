(ns status-im.chat.views.response
  (:require-macros [reagent.ratom :refer [reaction]]
                   [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                animated-view
                                                icon
                                                image
                                                text
                                                text-input
                                                touchable-highlight]]
            [status-im.components.drag-drop :as drag]
            [status-im.chat.views.response-suggestions :refer [response-suggestions-view]]
            [status-im.chat.styles.response :as st]
            [status-im.chat.styles.message-input :refer [input-height]]
            [status-im.components.animation :as anim]
            [status-im.components.react :as react]))

(defn drag-icon []
  [view st/drag-container
   [icon :drag-white st/drag-icon]])

(defn command-icon []
  [view st/command-icon-container
   ;; TODO stub data: command icon
   [icon :dollar-green st/command-icon]])

(defn info-container [command]
  [view st/info-container
   [text {:style st/command-name}
    (:description command)]
   [text {:style st/message-info}
    ;; TODO stub data: request message info
    "By ???, MMM 1st at HH:mm"]])

(defn create-response-pan-responder [response-height]
  (drag/create-pan-responder
    {:on-move    (fn [_ gesture]
                   (when (> (Math/abs (.-dy gesture)) 10)
                     (let [to-value (- (:height (react/get-dimensions "window"))
                                       (.-moveY gesture))]
                       (anim/start
                         (anim/spring response-height {:toValue to-value})))))
     :on-release (fn [_ gesture]
                   (when (> (Math/abs (.-dy gesture)) 10)
                     (dispatch [:fix-response-height
                                (.-dy gesture)
                                (.-vy gesture)
                                (.-_value response-height)])))}))

(defn request-info [response-height]
  (let [pan-responder (create-response-pan-responder response-height)
        command       (subscribe [:get-chat-command])]
    (fn [response-height]
      [view (merge (drag/pan-handlers pan-responder)
                   {:style (st/request-info (:color @command))})
       [drag-icon]
       [view st/inner-container
        [command-icon nil]
        [info-container @command]
        [touchable-highlight {:on-press #(dispatch [:start-cancel-command])}
         [view st/cancel-container
          [icon :close-white st/cancel-icon]]]]])))

(defn container-animation-logic [{:keys [to-value val]}]
  (fn [_]
    (println :to @to-value)
    (let [to-value @to-value]
      (anim/start (anim/spring val {:toValue to-value})))))

(defn container [response-height & children]
  (let [;; todo to-response-height, cur-response-height must be specific
        ;; for each chat
        to-response-height  (subscribe [:animations :to-response-height])
        changed (subscribe [:animations :response-height-changed])
        context             {:to-value to-response-height
                             :val      response-height}
        on-update           (container-animation-logic context)]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn [response-height & children]
         @to-response-height @changed
         (into [animated-view {:style (st/response-view response-height)}]
               children))})))

(defn response-view []
  (let [response-height (anim/create-value 0)]
    [container response-height
     [request-info response-height]
     [response-suggestions-view]
     [view st/input-placeholder]]))
