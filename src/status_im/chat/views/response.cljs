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
            [status-im.chat.styles.response :as st]
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

;; todo bad name. Ideas?
(defn enough-dy [gesture]
  (> (Math/abs (.-dy gesture)) 10))

(defn on-move [response-height kb-height orientation]
  (fn [_ gesture]
    (when (enough-dy gesture)
      (let [w        (react/get-dimensions "window")
            ;; depending on orientation use height or width of screen
            prop     (if (= :portrait @orientation)
                       :height
                       :width)
            ;; subtract keyboard height to get "real height" of screen
            ;; then subtract gesture position to get suggestions height
            ;; todo maybe it is better to use margin-top instead height
            ;; it is not obvious
            to-value (- (prop w) @kb-height (.-moveY gesture))]
        (anim/start
          (anim/spring response-height {:toValue to-value}))))))

(defn on-release [response-height]
  (fn [_ gesture]
    (when (enough-dy gesture)
      (dispatch [:fix-response-height
                 (.-vy gesture)
                 ;; todo access to "private" property
                 ;; better to find another way...
                 (.-_value response-height)]))))

(defn pan-responder [response-height kb-height orientation]
  (drag/create-pan-responder
    {:on-move    (on-move response-height kb-height orientation)
     :on-release (on-release response-height)}))

(defn request-info [response-height]
  (let [orientation   (subscribe [:get :orientation])
        kb-height     (subscribe [:get :keyboard-height])
        pan-responder (pan-responder response-height kb-height orientation)
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
    (let [to-value @to-value]
      (anim/start (anim/spring val {:toValue to-value
                                    :tension 50
                                    :friction 10})))))

(defn container [response-height & children]
  (let [;; todo to-response-height, cur-response-height must be specific
        ;; for each chat
        to-response-height (subscribe [:animations :to-response-height])
        changed            (subscribe [:animations :response-height-changed])
        context            {:to-value to-response-height
                            :val      response-height}
        on-update          (container-animation-logic context)]
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

(defview placeholder []
  [suggestions [:get-content-suggestions]]
  (when (seq suggestions)
    [view st/input-placeholder]))

(defview response-suggestions-view []
  [suggestions [:get-content-suggestions]]
  (when (seq suggestions) suggestions))

(defn response-view []
  (let [response-height (anim/create-value 0)]
    [container response-height
     [request-info response-height]
     [response-suggestions-view]
     [placeholder]]))
