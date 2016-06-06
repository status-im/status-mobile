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
            [status-im.chat.styles.plain-input :refer [input-height]]
            [status-im.components.animation :as anim]))

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

(defn create-response-pan-responder []
  (drag/create-pan-responder
    {:on-move    (fn [e gesture]
                   (dispatch [:on-drag-response (.-dy gesture)]))
     :on-release (fn [e gesture]
                   (dispatch [:fix-response-height]))}))

(defn request-info []
  (let [pan-responder (create-response-pan-responder)
        command (subscribe [:get-chat-command])]
    (fn []
      [view (merge (drag/pan-handlers pan-responder)
                   {:style (st/request-info (:color @command))})
       [drag-icon]
       [view st/inner-container
        [command-icon nil]
        [info-container @command]
        [touchable-highlight {:on-press #(dispatch [:start-cancel-command])}
         [view st/cancel-container
          [icon :close-white st/cancel-icon]]]]])))

(defn container-animation-logic [{:keys [animation? to-value current-value val]}]
  (fn [_]
    (if @animation?
      (let [to-value @to-value]
        (anim/start (anim/spring val {:toValue to-value})
                    (fn [arg]
                      (when (.-finished arg)
                        (dispatch [:set-in [:animations :response-height-current] to-value])
                        (dispatch [:finish-animate-response-resize])
                        (when (= to-value input-height)
                          (dispatch [:finish-animate-cancel-command])
                          (dispatch [:cancel-command]))))))
      (anim/set-value val @current-value))))

(defn container [& children]
  (let [commands-input-is-switching? (subscribe [:get-in [:animations :commands-input-is-switching?]])
        response-resize? (subscribe [:get-in [:animations :response-resize?]])
        to-response-height (subscribe [:get-in [:animations :to-response-height]])
        cur-response-height (subscribe [:get-in [:animations :response-height-current]])
        response-height (anim/create-value (or @cur-response-height 0))
        context {:animation?    (reaction (or @commands-input-is-switching? @response-resize?))
                 :to-value      to-response-height
                 :current-value cur-response-height
                 :val           response-height}
        on-update (container-animation-logic context)]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn [& children]
         @to-response-height
         (into [animated-view {:style (st/response-view (if (or @commands-input-is-switching? @response-resize?)
                                                          response-height
                                                          (or @cur-response-height 0)))}]
               children))})))

(defn response-view []
  [container
   [request-info]
   [response-suggestions-view]
   [view st/input-placeholder]])
