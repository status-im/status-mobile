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
            [status-im.chat.styles.dragdown :as ddst]
            [status-im.components.animation :as anim]
            [status-im.chat.suggestions-responder :as resp]))

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

(defn request-info [response-height]
  (let [orientation (subscribe [:get :orientation])
        kb-height (subscribe [:get :keyboard-height])
        pan-responder (resp/pan-responder response-height
                                          kb-height
                                          orientation
                                          :fix-response-height)
        command (subscribe [:get-chat-command])]
    (fn [response-height]
      (if (= :response (:type @command))
        [view (merge (drag/pan-handlers pan-responder)
                     {:style (st/request-info (:color @command))})
         [drag-icon]
         [view st/inner-container
          [command-icon nil]
          [info-container @command]
          [touchable-highlight {:on-press #(dispatch [:start-cancel-command])}
           [view st/cancel-container
            [icon :close-white st/cancel-icon]]]]]
        [view (merge (drag/pan-handlers pan-responder)
                     {:style ddst/drag-down-touchable})
         [icon :drag_down ddst/drag-down-icon]]))))

(defn container-animation-logic [{:keys [to-value val]}]
  (let [to-value @to-value]
    (anim/start (anim/spring val {:toValue to-value}))))

(defn container [response-height & children]
  (let [;; todo to-response-height, cur-response-height must be specific
        ;; for each chat
        to-response-height (subscribe [:animations :to-response-height])
        changed (subscribe [:animations :response-height-changed])
        context {:to-value to-response-height
                 :val      response-height}
        on-update #(container-animation-logic context)]
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
