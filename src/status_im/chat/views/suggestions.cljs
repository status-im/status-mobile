(ns status-im.chat.views.suggestions
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                scroll-view
                                                text
                                                icon
                                                image
                                                touchable-highlight
                                                list-view
                                                list-item
                                                animated-view]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.chat.styles.suggestions :as st]
            [status-im.chat.styles.dragdown :as ddst]
            [reagent.core :as r]
            [status-im.components.animation :as anim]
            [status-im.components.drag-drop :as drag]
            [status-im.chat.suggestions-responder :as resp]
            [status-im.chat.constants :as c]))

(defn set-command-input [command]
  (dispatch [:set-chat-command command]))

(defview request-item [{:keys [type message-id]}]
  [{:keys [color icon description] :as response} [:get-response type]]
  [touchable-highlight
   {:on-press #(dispatch [:set-response-chat-command message-id type])}
   [view st/request-container
    [view st/request-icon-container
     [view (st/request-icon-background color)
      [image {:source {:uri icon}
              :style  st/request-icon}]]]
    [view st/request-info-container
     [text {:style st/request-info-description} description]
     ;; todo stub
     [text {:style st/request-message-info}
      "By console, today at 14:50"]]]])

(defn render-request-row
  [{:keys [chat-id message-id] :as row} _ _]
  (list-item
    ^{:key [chat-id message-id]}
    [request-item row]))

(defn suggestion-list-item
  [[command {:keys [description]
             name  :name
             :as   suggestion}]]
  (let [label (str "!" name)]
    [touchable-highlight
     {:onPress #(set-command-input command)
      :style   st/suggestion-highlight}
     [view st/suggestion-container
      [view st/suggestion-sub-container
       [view st/command-description-container
        [text {:style st/value-text} label]
        [text {:style st/description-text} description]]
       [view st/command-label-container
        [view (st/suggestion-background suggestion)
         [text {:style st/suggestion-text} label]]]]]]))

(defn render-row [row _ _]
  (list-item [suggestion-list-item row]))

(defn title [s]
  [view st/title-container
   [text {:style st/title-text} s]])

(defview suggestions-view []
  [suggestions [:get-suggestions]
   requests [:get-requests]]
  [scroll-view {:keyboardShouldPersistTaps true}
   (when (seq requests) [title "Requests"])
   (when (seq requests)
     [view
      [list-view {:dataSource                (to-datasource requests)
                  :keyboardShouldPersistTaps true
                  :renderRow                 render-request-row}]])
   [title "Commands"]
   [view
    [list-view {:dataSource                (to-datasource suggestions)
                :keyboardShouldPersistTaps true
                :renderRow                 render-row}]]])

(defn header [h]
  (let [orientation (subscribe [:get :orientation])
        kb-height (subscribe [:get :keyboard-height])
        pan-responder (resp/pan-responder h
                                          kb-height
                                          orientation
                                          :fix-commands-suggestions-height)]
    (fn [_]
      [view
       (merge (drag/pan-handlers pan-responder)
              {:style ddst/drag-down-touchable})
       [view st/header-icon]])))

(defn container-animation-logic [{:keys [to-value val]}]
  (when-let [to-value @to-value]
    (anim/start (anim/spring val {:toValue to-value}))))

(defn container [h & elements]
  (let [;; todo to-response-height, cur-response-height must be specific
        ;; for each chat
        to-response-height (subscribe [:animations :command-suggestions-height])
        changed (subscribe [:animations :commands-height-changed])
        context {:to-value to-response-height
                 :val      h}
        on-update #(container-animation-logic context)]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn [h & elements]
         @changed
         (into [animated-view {:style (st/container h)}] elements))})))

(defn suggestion-container []
  (let [h (anim/create-value 10)]
    [container h
     [header h]
     [suggestions-view]
     [view {:height c/input-height}]]))
