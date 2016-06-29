(ns status-im.chat.views.suggestions
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                text
                                                icon
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
            [status-im.components.react :as react]
            [status-im.chat.suggestions-responder :as resp]))

(defn set-command-input [command]
  (dispatch [:set-chat-command command]))

(defn suggestion-list-item
  [[command {:keys [description]
             name  :name
             :as   suggestion}]]
  (let [label (str "!" name)]
    [touchable-highlight
     {:onPress #(set-command-input command)}
     [view st/suggestion-container
      [view st/suggestion-sub-container
       [view (st/suggestion-background suggestion)
        [text {:style st/suggestion-text} label]]
       [text {:style st/value-text} label]
       [text {:style st/description-text} description]]]]))

(defn render-row [row _ _]
  (list-item [suggestion-list-item row]))


(defn suggestions-view []
  (let
    [suggestions (subscribe [:get-suggestions])]
    (r/create-class
      {:reagent-render
       (fn []
         [view (st/suggestions-container (count @suggestions))
          [list-view {:dataSource                (to-datasource @suggestions)
                      :enableEmptySections       true
                      :keyboardShouldPersistTaps true
                      :renderRow                 render-row}]])})))

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
       [icon :drag_down ddst/drag-down-icon]])))

(defn container-animation-logic [{:keys [to-value val]}]
  (let [to-value @to-value]
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
  (let [h (anim/create-value 0)]
    [container h
     [header h]
     [suggestions-view]]))
